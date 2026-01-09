package com.devvv.commons.common.enums.user;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by WangSJ on 2023/04/14
 */
@Getter
@AllArgsConstructor
public enum UserType implements IDEnum {
    Guest("G","游客") ,
    Common("C","普通用户") ,
    Test("T","测试账号，无意义，可以被删除的") ,
    ;


    private final String id;
    private final String desc;
}
