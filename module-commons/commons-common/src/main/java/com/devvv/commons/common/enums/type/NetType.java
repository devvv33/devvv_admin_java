package com.devvv.commons.common.enums.type;


import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 网络类型枚举定义
 */
@Getter
@AllArgsConstructor
public enum NetType implements IDEnum {

    WIFI("WF", "Wifi"),
    G4("4G", "4G"),
    G3("3G", "3G"),
    G2("2G", "2G"),
    Unkown("UN", "未知");

    private final String id;
    private final String desc;

}
