package com.devvv.cms.config;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import com.devvv.commons.token.utils.StpAdminUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Create by WangSJ on 2024/06/25
 * 鉴权
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    // 返回一个账号所拥有的权限码集合
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        SaSession session = StpAdminUtil.getSessionByLoginId(loginId);
        return session.getModel(SaSession.PERMISSION_LIST, List.class, Collections.emptyList());
    }

    // 返回一个账号所拥有的角色标识集合
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SaSession session = StpAdminUtil.getSessionByLoginId(loginId);
        return session.getModel(SaSession.ROLE_LIST, List.class, Collections.emptyList());
    }

}
