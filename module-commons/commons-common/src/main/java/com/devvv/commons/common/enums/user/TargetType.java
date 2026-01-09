package com.devvv.commons.common.enums.user;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by WangSJ on 2024/07/08
 */
@Getter
@AllArgsConstructor
public enum TargetType implements IDEnum {

    ACCOUNT("A","账号"),
    ID_CARD("C","身份证号"),
    MOBILE("M","手机号"),
    DEVICE("D","设备号"),
    ;

    private final String id;
    private final String desc;
}