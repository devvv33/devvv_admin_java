package com.devvv.commons.common.enums.status;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态枚举
 */
@Getter
@AllArgsConstructor
public enum EnableStatus implements IDEnum {

    Enable("E", "正常"),
    Disable("D", "禁用");

    private final String id;
    private final String desc;
}
