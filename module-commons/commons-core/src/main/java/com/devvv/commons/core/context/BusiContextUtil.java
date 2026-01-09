package com.devvv.commons.core.context;

import cn.hutool.core.lang.Opt;
import com.devvv.commons.core.session.AdminSessionInfo;
import com.devvv.commons.core.session.RoleVO;
import com.devvv.commons.core.session.UserSessionInfo;
import com.devvv.commons.core.utils.RoleUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * Create by WangSJ on 2024/06/28
 * 对于BusiContext的更易用封装
 */
@Slf4j
public class BusiContextUtil {

    public static Long getUserId() {
        return Opt.ofNullable(BusiContextHolder.getContext())
                .map(BusiContext::getUserId)
                .orElse(null);
    }

    public static String getUserName(){
        return Opt.ofNullable(BusiContextHolder.getContext())
                .map(BusiContext::getUserSessionCopy)
                .map(UserSessionInfo::getNickname)
                .orElse(null);
    }
    public static UserSessionInfo getUserSessionCopy(){
        return Opt.ofNullable(BusiContextHolder.getContext())
                .map(BusiContext::getUserSessionCopy)
                .orElse(null);
    }


    public static Long getAdminId() {
        return Opt.ofNullable(BusiContextHolder.getContext())
                .map(BusiContext::getAdminId)
                .orElse(null);
    }

    public static String getAdminName(){
        return Opt.ofNullable(BusiContextHolder.getContext())
                .map(BusiContext::getAdminSessionCopy)
                .map(AdminSessionInfo::getNickname)
                .orElse(null);
    }
    public static AdminSessionInfo getAdminSessionCopy(){
        return Opt.ofNullable(BusiContextHolder.getContext())
                .map(BusiContext::getAdminSessionCopy)
                .orElse(null);
    }

    /**
     * 为方便使用，此处在context为null时给个默认值
     * 注意：如果要对BusiContext修改则不可使用该方法，应使用{@link BusiContextHolder#getContext}
     */
    public static BusiContext getContext(){
        BusiContext context = BusiContextHolder.getContext();
        return context == null ? new BusiContext() : context;
    }

    /**
     * 终止发送mq消息
     */
    public static void breakSendMQ(){
        BusiContext context = BusiContextHolder.getContext();
        if (context != null) {
            context.setBreakSendMQ(true);
        }
    }

    public static boolean isSuperAdmin() {
        return Opt.ofNullable(BusiContextHolder.getContext())
                .map(BusiContext::getAdminSessionCopy)
                .map(AdminSessionInfo::getRoleList)
                .map(roleList -> roleList.stream().map(RoleVO::getRoleCode).anyMatch(RoleUtil::isSuperAdmin))
                .orElse(false);
    }

    public static List<String> getRoleCodes() {
        return Opt.ofNullable(BusiContextHolder.getContext())
                .map(BusiContext::getAdminSessionCopy)
                .map(AdminSessionInfo::getRoleList)
                .map(roleList -> roleList.stream().map(RoleVO::getRoleCode).toList())
                .orElse(Collections.emptyList());
    }
}
