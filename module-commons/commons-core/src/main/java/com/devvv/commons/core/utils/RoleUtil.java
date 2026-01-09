package com.devvv.commons.core.utils;

import java.util.Collection;

/**
 * Create by WangSJ on 2025/12/31
 */
public class RoleUtil {

    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_GUEST = "GUEST";


    public static boolean isSuperAdmin(String roleCode) {
        return ROLE_SUPER_ADMIN.equals(roleCode);
    }
    public static boolean hasSuperAdmin(Collection<String> roleList) {
        if (roleList == null || roleList.isEmpty()) {
            return false;
        }
        return roleList.contains(ROLE_SUPER_ADMIN);
    }

}
