package com.devvv.user.web.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.enums.status.UserStatus;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.common.utils.MyStrUtil;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.lock.BusiRedissonLockUtil;
import com.devvv.commons.token.utils.StpUserUtil;
import com.devvv.user.api.models.dto.UserPageQueryDTO;
import com.devvv.user.web.dao.entity.UUserBasic;
import com.devvv.user.web.dao.mapper.UUserBasicMapper;
import com.devvv.user.web.manager.UserInfoManager;
import com.devvv.user.web.models.form.UserIdForm;
import com.devvv.user.web.models.form.UserUpdateForm;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 后台用户 Service 实现类
 */
@Slf4j
@Service
public class UserInfoService {

    @Resource
    private UUserBasicMapper userBasicMapper;
    @Resource
    private UserInfoManager userInfoManager;

    /**
     * 查询用户
     */
    public UUserBasic getUser(UserIdForm form) {
        UUserBasic param = new UUserBasic();
        param.setUserId(form.getUserId());
        return userBasicMapper.selectByPrimaryKey(param);
    }

    /**
     * 创建用户
     */
    @Transactional
    public Long createUser(UserUpdateForm form) {
        MyStrUtil.trimStringFields(form);
        UUserBasic userBasic = new UUserBasic();
        userBasic.setUserId(form.getUserId());
        userBasic.setUserType(form.getUserType());
        userBasic.setNickname(form.getNickname());
        userBasic.setAvatar(form.getAvatar());
        userBasic.setGender(form.getGender());
        userBasic.setUserStatus(ObjectUtil.defaultIfNull(form.getUserStatus(), UserStatus.Enable));
        userBasic.setPackageType(form.getPackageType());
        userBasic.setCreateTime(new Date());
        userBasicMapper.insert(userBasic);
        return userBasic.getUserId();
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUser(UserUpdateForm form) {
        Assert.notNull(form.getUserId(), "用户id不能为空");
        MyStrUtil.trimStringFields(form);
        // 必须是管理员，或 当前用户自己修改
        if (BusiContextUtil.getAdminId() == null && ObjectUtil.notEqual(BusiContextUtil.getUserId(), form.getUserId())) {
            throw new BusiException("无此权限");
        }
        // 加锁
        BusiRedissonLockUtil.lockUserId(form.getUserId());
        UUserBasic existUser = userBasicMapper.selectByPrimaryKey(new UUserBasic(form.getUserId()));
        Assert.notNull(existUser, "用户不存在");

        // 更新用户信息
        UUserBasic updater = new UUserBasic();
        updater.setUserId(form.getUserId());
        updater.setNickname(form.getNickname());
        updater.setAvatar(form.getAvatar());
        updater.setGender(form.getGender());
        userBasicMapper.updateByPrimaryKeySelective(updater);
        // 更新session
        userInfoManager.updateUserSessionInfo(form.getUserId());
    }

    /**
     * 删除用户信息
     */
    @Transactional
    public void deleteUser(UserIdForm form) {
        // 必须是管理员，或 当前用户自己删除
        if (BusiContextUtil.getAdminId() == null && ObjectUtil.notEqual(BusiContextUtil.getUserId(), form.getUserId())) {
            throw new BusiException("无此权限");
        }

        UUserBasic deleter = new UUserBasic();
        deleter.setUserId(form.getUserId());
        userBasicMapper.deleteByPrimaryKey(deleter);

        // 退出登录
        StpUserUtil.logout(form.getUserId());
    }

    /**
     * 分页查询用户
     */
    public PageVO<UUserBasic> pageList(UserPageQueryDTO pageQuery) {
        Assert.notNull(pageQuery.getPageNum(), "pageNum 不能为空");
        Assert.notNull(pageQuery.getPageSize(), "pageSize 不能为空");
        Assert.isTrue(pageQuery.getPageSize() <= 1000, "pageSize 不能超过1000");
        // 分页
        PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
        List<UUserBasic> userBasicList = userBasicMapper.pageList(pageQuery);
        return PageVO.build(userBasicList);
    }
}
