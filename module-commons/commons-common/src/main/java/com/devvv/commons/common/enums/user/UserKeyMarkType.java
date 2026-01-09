package com.devvv.commons.common.enums.user;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务标识定义
 */
@Getter
@AllArgsConstructor
public enum UserKeyMarkType {
    // 修改资料
    UserFirstBaseInfo("用户首次完善基本信息"),

    ;

    private final String desc;
}
