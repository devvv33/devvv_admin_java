package com.devvv.user.web.manager;

import cn.dev33.satoken.session.SaSession;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.devvv.commons.common.enums.status.UserStatus;
import com.devvv.commons.common.enums.user.UserType;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextHolder;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.session.UserSessionInfo;
import com.devvv.commons.core.utils.BusiTransactionUtil;
import com.devvv.commons.token.utils.StpUserUtil;
import com.devvv.user.web.dao.entity.UUserBasic;
import com.devvv.user.web.dao.entity.UUserMobile;
import com.devvv.user.web.dao.mapper.UUserBasicMapper;
import com.devvv.user.web.dao.mapper.UUserMobileMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Create by WangSJ on 2024/07/05
 */
@Slf4j
@Component
public class UserInfoManager {

    @Resource
    private UUserBasicMapper userBasicMapper;
    @Resource
    private UUserMobileMapper userMobileMapper;

    /**
     * 初始化用户
     */
    public UUserBasic initUser(String mobile) {
        // 初始化用户基本信息
        UUserBasic userBasic = UUserBasic.builder()
                .nickname(StrUtil.format("用户{}", mobile))
                .userType(UserType.Common)
                .userStatus(UserStatus.Enable)
                .packageType(BusiContextUtil.getContext().getClientInfo().getPackageType())
                .createTime(new Date())
                .build();
        userBasicMapper.insert(userBasic);

        // 初始化手机号
        UUserMobile userMobile = UUserMobile.builder()
                .userId(userBasic.getUserId())
                .mobile(mobile)
                .unsubscribe(false)
                .createTime(new Date())
                .build();
        userMobileMapper.insert(userMobile);
        return userBasic;
    }

    /**
     * 构建用户session
     */
    public UserSessionInfo buildUserSessionInfo(UUserBasic userBasic){
        UserSessionInfo userSessionInfo = new UserSessionInfo();
        userSessionInfo.setUserId(userBasic.getUserId());
        userSessionInfo.setUserType(userBasic.getUserType());
        userSessionInfo.setUserStatus(userBasic.getUserStatus());
        userSessionInfo.setNickname(userBasic.getNickname());
        userSessionInfo.setAvatar(userBasic.getAvatar());
        userSessionInfo.setGender(userBasic.getGender());
        userSessionInfo.setRegisterTime(userBasic.getRegisterTime());
        userSessionInfo.setVipExpireTime(userBasic.getVipExpireTime());
        return userSessionInfo;
    }

    /**
     * 更新用户Session
     */
    public void updateUserSessionInfo(Long userId) {
        UUserBasic userBasic = userBasicMapper.selectByPrimaryKey(new UUserBasic(userId));
        if (userBasic == null) {
            return;
        }
        // 此用户未登录，无需更新
        SaSession ss = StpUserUtil.getSessionByLoginId(userId, false);
        if (ss == null) {
            return;
        }
        // 构建sessionInfo
        UserSessionInfo userSessionInfo = buildUserSessionInfo(userBasic);
        // 继承登录时间
        Opt.ofNullable(ss.getModel(SaSession.USER, UserSessionInfo.class))
                .map(UserSessionInfo::getLoginTime)
                .ifPresent(userSessionInfo::setLoginTime);

        // 更新session框架中的用户信息
        BusiTransactionUtil.execAfterCommit(() -> ss.set(SaSession.USER, userSessionInfo));

        // 更新当前Context的用户信息
        BusiContext context = BusiContextHolder.getContext();
        if (context != null) {
            if (context.getUserId() == null || ObjectUtil.equal(context.getUserId(), userSessionInfo.getUserId())) {
                context.setUserId(userSessionInfo.getUserId());
                context.setUserSessionCopy(userSessionInfo);
            }
        }
    }

    /**
     * 获取用户基本信息
     */
    public UUserBasic getUserBasic(Long userId) {
        return userBasicMapper.selectByPrimaryKey(new UUserBasic(userId));
    }

    /**
     * 更新用户基本信息
     */
    public void updateUserBasic(UUserBasic update) {
        if (update == null) {
            return;
        }
        // 更新数据库
        userBasicMapper.updateByPrimaryKeySelective(update);
        // 更新session
        updateUserSessionInfo(update.getUserId());
    }
}
