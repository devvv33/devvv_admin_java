package com.devvv.commons.common.enums.mq;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by WangSJ on 2024/07/01
 */
@Getter
@AllArgsConstructor
public enum MQTopic implements IDEnum {

    DEFAULT("DEFAULT", "默认"),
    User("USER", "用户模块"),
    ;

    private final String id;
    private final String desc;
}
