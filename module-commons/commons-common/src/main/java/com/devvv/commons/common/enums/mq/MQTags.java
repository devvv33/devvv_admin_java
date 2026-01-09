package com.devvv.commons.common.enums.mq;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by WangSJ on 2024/07/02
 */
@Getter
@AllArgsConstructor
public enum MQTags implements IDEnum {
    All("*", "所有消息"),
    Login( "Login","用户登录"),
    User( "User","用户相关"),
    ;

    private final String id;
    private final String desc;
}
