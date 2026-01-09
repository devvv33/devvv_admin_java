package com.devvv.commons.core.config.redis.key;

import com.devvv.commons.core.config.redis.mode.KeyMode;
import com.devvv.commons.core.config.redis.mode.TTLMode;

/**
 * Create by WangSJ on 2023/07/05
 */
public interface KeyDefine {

    String name();
    default String getKeyPrefix(){
        return name();
    }

    KeyMode getKeyMode();

    TTLMode getTtlMode();

    Integer getSeconds();
}
