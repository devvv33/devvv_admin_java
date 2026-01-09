package com.devvv.commons.common.enums.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Redis类型枚举
 */
@Getter
@AllArgsConstructor
public enum RedisType {

    sys("sys", "系统缓存"),
    limit("limit", "阈值限定"),
    session("session", "Session缓存"),
    table("table", "表缓存"),
    user("user", "用户缓存"),
    biz("biz", "通用业务缓存"),
    log("log", "日志缓存"),
    ;

    private final String id;
    private final String desc;
}
