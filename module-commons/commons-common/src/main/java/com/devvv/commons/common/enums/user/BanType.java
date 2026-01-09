package com.devvv.commons.common.enums.user;

import lombok.Getter;

/**
 * Create by WangSJ on 2024/07/08
 */
@Getter
public enum BanType {

    BAN_LOGIN("封禁登录"),
    UN_BAN_LOGIN("解除-封禁登录", BAN_LOGIN),

    ;

    private final String desc;
    private final boolean isUnban;      // 是否为解封类型
    private final BanType banTypeRef;   // 当枚举为解封类型时，对应的封禁类型

    BanType(String desc) {
        this.desc = desc;
        this.isUnban = false;
        this.banTypeRef = null;
    }

    BanType(String desc, BanType banTypeRef) {
        this.desc = desc;
        this.isUnban = true;
        this.banTypeRef = banTypeRef;
    }
}
