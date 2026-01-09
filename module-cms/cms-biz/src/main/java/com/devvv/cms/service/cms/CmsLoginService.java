package com.devvv.cms.service.cms;

import cn.dev33.satoken.session.SaSession;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import com.devvv.cms.dao.cms.entity.CmsAdminUser;
import com.devvv.cms.dao.cms.entity.CmsMenu;
import com.devvv.cms.dao.cms.entity.CmsRole;
import com.devvv.cms.dao.cms.mapper.CmsMenuMapper;
import com.devvv.cms.dao.cms.mapper.CmsRoleMapper;
import com.devvv.cms.manager.AdminUserManager;
import com.devvv.cms.manager.MenuManager;
import com.devvv.cms.models.convert.SysConvert;
import com.devvv.cms.models.form.LoginByMobileForm;
import com.devvv.cms.models.form.LoginByUsernameForm;
import com.devvv.cms.models.vo.CmsLoginInfoVO;
import com.devvv.commons.common.enums.status.EnableStatus;
import com.devvv.commons.common.utils.PasswordUtil;
import com.devvv.commons.core.context.BusiContextHolder;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.session.AdminSessionInfo;
import com.devvv.commons.core.session.RoleVO;
import com.devvv.commons.token.utils.StpAdminUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Create by WangSJ on 2024/06/25
 */
@Slf4j
@Service
public class CmsLoginService {

    @Resource
    private AdminUserManager adminUserManager;
    @Resource
    private CmsRoleMapper roleMapper;
    @Resource
    private CmsMenuMapper menuMapper;

    /**
     * 获取当前登录用户信息
     */
    public CmsLoginInfoVO getLoginInfo() {
        return CmsLoginInfoVO.builder()
                .tokenInfo(StpAdminUtil.getTokenInfo())
                .adminSessionInfo(BusiContextUtil.getAdminSessionCopy())
                .build();
    }

    public CmsLoginInfoVO loginByUsername(@Valid LoginByUsernameForm form) {
        CmsAdminUser admin = adminUserManager.getByUsername(form.getUsername());
        Assert.notNull(admin, "用户名或密码错误");
        Assert.isTrue(PasswordUtil.verifyPassword(admin.getPassword(), form.getPassword()), "用户名或密码错误");
        Assert.isTrue(admin.getStatus() == EnableStatus.Enable, "用户已被禁用，请联系管理员");

        // 登录
        StpAdminUtil.login(admin.getAdminId());

        // 登录后处理
        afterLongSuccess(admin);
        // 返回Token信息
        return getLoginInfo();
    }
    /**
     * 获取验证码
     */
    public void getSmsCode(LoginByMobileForm form) {
        // 频率校验（极验）

        // 发短信

        // 存入Redis
    }

    /**
     * 手机号登录
     */
    public CmsLoginInfoVO loginByMobile(LoginByMobileForm form) {
        CmsAdminUser admin = adminUserManager.getByMobile(form.getMobile());
        Assert.notNull(admin, "用户不存在");
        Assert.isTrue(admin.getStatus() == EnableStatus.Enable, "用户已被禁用，请联系管理员");

        // 登录
        StpAdminUtil.login(admin.getAdminId());

        // 登录后处理
        afterLongSuccess(admin);
        // 返回Token信息
        return getLoginInfo();
    }

    /**
     * 登录后处理
     */
    private AdminSessionInfo afterLongSuccess(CmsAdminUser admin) {
        Date loginTime = new Date();
        // 更新最后登录时间
        adminUserManager.updateLastLoginTime(admin.getAdminId(), loginTime, BusiContextUtil.getContext().getClientIp());

        // 补充详情
        AdminSessionInfo session = new AdminSessionInfo();
        session.setAdminId(admin.getAdminId());
        session.setUsername(admin.getUsername());
        session.setNickname(admin.getNickname());
        session.setAvatar(admin.getAvatar());
        session.setMobile(admin.getMobile());
        session.setLoginTime(loginTime);

        // 角色相关
        List<CmsRole> roleList = ObjUtil.defaultIfNull(admin.getRoleList(), () -> roleMapper.listByAdminId(admin.getAdminId()));
        List<RoleVO> roleVOList = SysConvert.INSTANCT.cmsRole_roleVO(roleList);
        session.setRoleList(roleVOList);

        // 将Session写入当前用户上下文
        BusiContextHolder.getContext().setAdminId(session.getAdminId());
        BusiContextHolder.getContext().setAdminSessionCopy(session);

        // 将Session写入登录框架中
        SaSession tokenSession = StpAdminUtil.getSessionByLoginId(admin.getAdminId());
        tokenSession.set(SaSession.USER, session);

        // 角色
        List<String> roleCodes = roleVOList.stream().map(RoleVO::getRoleCode).toList();
        tokenSession.set(SaSession.ROLE_LIST, roleCodes);

        // 权限
        List<CmsMenu> menuList = MenuManager.getInstance().listUserMenus(admin.getAdminId(), roleCodes);
        List<String> myPermissionList = MenuManager.convertResource2Permission(menuList);
        tokenSession.set(SaSession.PERMISSION_LIST, myPermissionList);
        return session;
    }

}
