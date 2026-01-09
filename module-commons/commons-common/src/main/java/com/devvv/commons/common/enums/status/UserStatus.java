package com.devvv.commons.common.enums.status;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by WangSJ on 2024/07/05
 * 用户状态
 */
@Getter
@AllArgsConstructor
public enum UserStatus implements IDEnum {

    Enable("E", "正常"),
    Disable("D", "禁用"),
    ;

    private final String id;
    private final String desc;
}
