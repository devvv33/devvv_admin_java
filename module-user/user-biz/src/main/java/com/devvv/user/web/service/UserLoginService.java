package com.devvv.user.web.service;

import cn.dev33.satoken.session.SaSession;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.devvv.commons.common.enums.mq.MQTags;
import com.devvv.commons.common.enums.mq.MQTopic;
import com.devvv.commons.common.enums.type.ClientType;
import com.devvv.commons.common.enums.user.BanType;
import com.devvv.commons.common.enums.user.GenderType;
import com.devvv.commons.common.enums.user.TargetType;
import com.devvv.commons.core.busicode.BusiCode;
import com.devvv.commons.core.busicode.BusiCodeDefine;
import com.devvv.commons.core.config.redis.RedisKey;
import com.devvv.commons.core.config.redis.key.UserKeyDefine;
import com.devvv.commons.core.config.redis.template.UserRedisTemplate;
import com.devvv.commons.core.context.BusiContextHolder;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.lock.BusiRedissonLockUtil;
import com.devvv.commons.core.session.UserSessionInfo;
import com.devvv.commons.core.utils.BusiThreadPoolUtil;
import com.devvv.commons.token.utils.StpUserUtil;
import com.devvv.user.web.dao.entity.UUserBasic;
import com.devvv.user.web.dao.entity.UUserMobile;
import com.devvv.user.web.dao.mapper.UUserMobileMapper;
import com.devvv.user.web.manager.BanManager;
import com.devvv.user.web.manager.UserInfoManager;
import com.devvv.user.web.models.form.LoginByMobileForm;
import com.devvv.user.web.models.form.RegisterForm;
import com.devvv.user.web.models.vo.UserLoginVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Create by WangSJ on 2024/07/05
 */
@Slf4j
@Service
public class UserLoginService {

    @Resource
    private UserRedisTemplate userRedisTemplate;
    @Resource
    private UserInfoManager userInfoManager;
    @Resource
    private UUserMobileMapper userMobileMapper;
    @Resource
    private BanManager banManager;

    private static final String DEFAULT_AVATAR_MALE = "test/config/avatar/default_avatar_male.png";
    private static final String DEFAULT_AVATAR_FEMALE = "test/config/avatar/default_avatar_female.png";

    /**
     * 手机号登录-发送短信验证码
     */
    public void sendSmsCode(LoginByMobileForm form) {
        Validator.validateMobile(form.getMobile(), "手机号格式错误");
        // 同一ip5分钟内次数限制
        String clientIp = BusiContextUtil.getContext().getClientIp();
        if (!Ipv4Util.isInnerIP(clientIp)) {
            Assert.isTrue(userRedisTemplate.incrementAndGet(RedisKey.create(UserKeyDefine.LoginSmsCodeCheckIP, clientIp)) <= 5, "操作频繁");
        }
        // 60秒内重复发送检测
        Assert.isTrue(userRedisTemplate.setIfAbsent(RedisKey.create(UserKeyDefine.LoginSmsCodeCheckMobile, form.getMobile()), "T"), "操作频繁");

        int code = RandomUtil.randomInt(1000, 9999);
        // todo: 发送短信

        // 记录验证码
        userRedisTemplate.set(RedisKey.create(UserKeyDefine.LoginSmsCode, form.getMobile()), String.valueOf(code));
    }

    /**
     * 手机号登录
     */
    @BusiCode(value = BusiCodeDefine.LoginByMobile, sendMQTopic = MQTopic.User, sendMQTags = MQTags.Login)
    @Transactional
    public UserLoginVO loginByMobile(LoginByMobileForm form) {
        Assert.notBlank(form.getCode(), "验证码错误");

        // String serverCode = userRedisTemplate.getAndDelete(RedisKey.create(UserKeyDefine.LoginSmsCode, form.getMobile()));
        // Assert.isTrue(StrUtil.equals(form.getCode().trim(), serverCode), "验证码错误");

        // 根据手机号查询用户id
        UUserMobile userMobile = userMobileMapper.getByMobile(form.getMobile());
        UUserBasic userBasic;
        if (userMobile == null) {
            userBasic = userInfoManager.initUser(form.getMobile());
        } else {
            userBasic = userInfoManager.getUserBasic(userMobile.getUserId());
            Assert.notNull(userBasic, "用户不存在");
        }

        // 封禁校验
        // 这里我们放到异步去获取TBan对象，是因为TBan是基于表缓存的：如果缓存未命中将会从数据库查询，并补充到缓存中；
        // 但是如果后边校验时，再抛出封禁异常，则会将回填缓存的操作回滚掉，从而导致永远无法回填缓存；
        // 这原本是表缓存的设计理念（事务异常结束时，将会丢弃所有对表缓存的修改），但是此处我们不希望在异常时进行回滚，故此在异步查询，脱离当前事务，不受当前事务回滚影响
        banManager.checkBan(Opt.ofTry(BusiThreadPoolUtil.executeDefaultPool(() -> banManager.getBan(TargetType.ACCOUNT, userBasic.getUserId(), BanType.BAN_LOGIN))::get).orElse(null));
        banManager.checkBan(Opt.ofTry(BusiThreadPoolUtil.executeDefaultPool(() -> banManager.getBan(TargetType.MOBILE, form.getMobile(), BanType.BAN_LOGIN))::get).orElse(null));

        // 登录流程
        ClientType clientType = BusiContextUtil.getContext().getClientInfo().getClientType();
        StpUserUtil.login(userBasic.getUserId(), clientType.getId());

        // 登录后处理
        afterLongSuccess(userBasic);
        return getLoginInfo();
    }


    /**
     * 获取当前登录用户信息
     */
    public UserLoginVO getLoginInfo() {
        UserLoginVO vo = new UserLoginVO();
        vo.setTokenInfo(StpUserUtil.getTokenInfo());
        vo.setUserSessionInfo(BusiContextUtil.getUserSessionCopy());
        return vo;
    }

    /**
     * 登录后处理
     */
    private UserSessionInfo afterLongSuccess(UUserBasic userBasic) {
        // 补充详情
        UserSessionInfo sessionInfo = userInfoManager.buildUserSessionInfo(userBasic);
        sessionInfo.setLoginTime(new Date());

        // 将Session写入登录框架中
        SaSession ss = StpUserUtil.getSessionByLoginId(userBasic.getUserId());
        ss.set(SaSession.USER, sessionInfo);

        // 将Session写入当前用户上下文
        BusiContextHolder.getContext().setUserId(sessionInfo.getUserId());
        BusiContextHolder.getContext().setUserSessionCopy(sessionInfo);
        return sessionInfo;
    }

    /**
     * 完善注册
     */
    @BusiCode(value = BusiCodeDefine.Register, sendMQTopic = MQTopic.User, sendMQTags = MQTags.Login)
    @Transactional
    public UserLoginVO register(@Valid RegisterForm form) {
        Long userId = BusiContextUtil.getUserId();

        // 加锁
        BusiRedissonLockUtil.lockUserId(userId);

        UUserBasic userBasic = userInfoManager.getUserBasic(userId);
        Assert.notNull(userBasic, "用户不存在");
        Assert.isNull(userBasic.getRegisterTime(), "用户已注册");

        // 头像
        String avatar = StrUtil.blankToDefault(form.getAvatar(), form.getGender() == GenderType.Female ? DEFAULT_AVATAR_FEMALE : DEFAULT_AVATAR_MALE);

        UUserBasic update = UUserBasic.builder()
                .userId(userId)
                .nickname(form.getNickname().trim())
                .avatar(avatar)
                .gender(form.getGender())
                .registerTime(new DateTime())
                .build();
        userInfoManager.updateUserBasic(update);
        return getLoginInfo();
    }

    /**
     * 上线
     */
    @BusiCode(value = BusiCodeDefine.Online, sendMQTopic = MQTopic.User, sendMQTags = MQTags.Login)
    public UserLoginVO online() {
        // 暂无业务，只做转发即可
        return getLoginInfo();
    }

    /**
     * 退出登录
     */
    @BusiCode(value = BusiCodeDefine.Logout, sendMQTopic = MQTopic.User, sendMQTags = MQTags.Login)
    public void logout() {
        // session框架中直接退出登录
        StpUserUtil.logout();

        // 其他业务处理
        Long userId = BusiContextUtil.getUserId();
        if (userId == null) {
            // ...
        }
    }
}
