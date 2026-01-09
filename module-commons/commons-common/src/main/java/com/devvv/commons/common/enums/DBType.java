package com.devvv.commons.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据库类型枚举
 */
@Getter
@AllArgsConstructor
public enum DBType implements IDEnum {

    sys("sys", "配置数据库"),
    user("user", "用户数据库"),
    busi("busi", "业务数据库"),
    cms("cms", "CMS数据库"),
    query("query", "分析库"),

    ;
    /**
     * 数据库唯一标识
     */
    private final String id;
    private final String desc;
}
