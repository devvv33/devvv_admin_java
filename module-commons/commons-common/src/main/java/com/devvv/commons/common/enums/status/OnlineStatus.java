package com.devvv.commons.common.enums.status;


import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 在线状态枚举定义
 *
 * @author Eason
 * @date 2020/9/19 17:22
 */
@Getter
@AllArgsConstructor
public enum OnlineStatus implements IDEnum {

    Online("O", "在线"),
    Offline("F", "离线");

    private final String id;
    private final String desc;
}
