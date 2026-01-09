package com.devvv.commons.common.enums.type;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 客户端打开类型枚举定义
 */
@Getter
@AllArgsConstructor
public enum ClientTouchType implements IDEnum {

    // 其他
    Url("Url", "网页地址","地址全路径"),
    Image("Image", "打开图片","图片地址，可以是绝对路径，也可以是相对路径"),
    NONE("NONE", "未定义，按照客户端约定处理", null),

    ;

    private final String id;
    private final String desc;
    // touchValue参数说明
    private final String paramsDesc;


}
