package com.devvv.commons.core.config.redis.mode;

/**
 * Create by WangSJ on 2023/07/05
 */
public enum DataMode {
    Incr,
    String,
    Hash,
    List,
    Set,
    SortedSet,
    Geo,
    Stream,
    Pubsub
}
