package com.devvv.commons.common.enums.type;


import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 排序类型
 */
@Getter
@AllArgsConstructor
public enum OrderType implements IDEnum {

    Asc("Asc", "升序"),
    Desc("Desc", "倒序");

    private final String id;
    private final String desc;
}
