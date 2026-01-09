package com.devvv.commons.core.config.redis.key;

import lombok.Getter;
import com.devvv.commons.core.config.redis.mode.DataMode;
import com.devvv.commons.core.config.redis.mode.KeyMode;
import com.devvv.commons.core.config.redis.mode.TTLMode;

/**
 * Create by WangSJ on 2023/07/17
 */
@Getter
public enum BusiKeyDefine implements KeyDefine {
    ;


    private final KeyMode keyMode;
    private final DataMode dataType;
    private final TTLMode ttlMode;
    private final Integer seconds;

    BusiKeyDefine(KeyMode keyMode, DataMode dataType, Integer seconds) {
        this.keyMode = keyMode;
        this.dataType = dataType;
        this.seconds = seconds;
        this.ttlMode = TTLMode.NOW_ADD;
    }

    BusiKeyDefine(KeyMode keyMode, DataMode dataType, TTLMode mode) {
        this.keyMode = keyMode;
        this.dataType = dataType;
        this.ttlMode = mode;
        this.seconds = null;

        if (ttlMode == TTLMode.NOW_ADD) {
            throw new IllegalArgumentException("请使用带有seconds参数的构造方法！");
        }
    }
}
