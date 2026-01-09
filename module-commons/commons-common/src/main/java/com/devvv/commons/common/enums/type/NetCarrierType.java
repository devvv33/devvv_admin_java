package com.devvv.commons.common.enums.type;


import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 网络运营商枚举定义
 */
@Getter
@AllArgsConstructor
public enum NetCarrierType implements IDEnum {

    CMCC("M", "中国移动"),
    CUCC("U", "中国联通"),
    CTCC("T", "中国电信");

    private final String id;
    private final String desc;
}
