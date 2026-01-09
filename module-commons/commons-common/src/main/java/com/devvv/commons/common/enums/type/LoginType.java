package com.devvv.commons.common.enums.type;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录类型
 */
@Getter
@AllArgsConstructor
public enum LoginType implements IDEnum {

    Mobile("M", "手机号登录"),
    Apple("A", "苹果登录"),
    Wechat("W", "微信登录"),
    ;

    private final String id;
    private final String desc;
}
