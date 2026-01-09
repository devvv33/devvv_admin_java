package com.devvv.commons.common.enums.type;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 客户端类型
 */
@Getter
@AllArgsConstructor
public enum ClientType implements IDEnum {

    Cms("CM", "Cms网页端"),
    Web("WB", "Web网页端"),
    H5("H5", "AppH5"),

    AndroidApp("AA", "安卓客户端"),
    IOSApp("IA", "苹果客户端"),

    WxProgram("WP", "微信小程序"),
    DyProgram("DP", "抖音小程序"),
    KsProgram("KP", "快手小程序"),

    AndroidFastApp("AF", "安卓快应用"),
    ;

    private final String id;
    private final String desc;

}
