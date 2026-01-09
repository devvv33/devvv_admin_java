package com.devvv.commons.core.config.redis.mode;

/**
 * Create by WangSJ on 2023/07/05
 */
public enum TTLMode {

    /**
     * 永久，就是不设置过期时间
     */
    NONE,
    /**
     * 当前时间增加指定的秒数
     * 比如当前时间是2018-6-4 22:00:00 ，增加 3 * 3600 秒
     * 那么失效时间在：2018-6-5 01:00:00
     */
    NOW_ADD,
    /**
     * 月底失效
     * 通常设置再次月1号的0点0分0秒失效
     */
    END_OF_MONTH,

    /**
     * 今天即将失效
     * 通常设置再次日的0点0分0秒失效
     */
    END_OF_DAY,

    /**
     * 本周过后失效
     * 通常设置再次周一的0点0分0秒失效
     */
    END_OF_WEEK,

    /**
     * 动态过期时间，由开发人员控制，通常会存在多个过期时间，
     * 比如根据平台类型缓存有效期也不一致的这种
     */
    DYNAMIC,
}
