package com.devvv.commons.core.config.cache.local;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by WangSJ on 2024/07/22
 */
@Getter
@AllArgsConstructor
public enum LocalCacheEnums {

    /**
     * 本地缓存定义
     * 实现类必须实现{@link LocalCache}接口
     */
    SettingManager("系统配置", "com.devvv.commons.manager.sys.manager.SettingManager"),
    AdminUserManager("后台用户", "com.devvv.cms.manager.AdminUserManager"),
    MenuManager("后台用户", "com.devvv.cms.manager.MenuManager"),
    ;

    private final String desc;
    private final String className;

}
