package com.devvv.commons.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 包体类型定义
 */
@Getter
@AllArgsConstructor
public enum PackageType {

    BF("边锋剧场", "cfg/logo/BF/256.png"),
    ;

    private final String desc;
    private final String logo;

}
