package com.devvv.commons.common.enums.type;


import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否成功
 */
@Getter
@AllArgsConstructor
public enum SuccessType implements IDEnum {

    Success("S", "成功"),
    Failure("F", "失败");

    private final String id;
    private final String desc;
}
