package com.devvv.commons.core.config.redis.key;

import com.devvv.commons.core.config.redis.mode.DataMode;
import com.devvv.commons.core.config.redis.mode.KeyMode;
import com.devvv.commons.core.config.redis.mode.TTLMode;
import lombok.Getter;

/**
 * Create by WangSJ on 2024/02/04
 */
@Getter
public enum SysKeyDefine implements KeyDefine {
    // 默认雪花算法的workerId
    SnowflakeIdSeq(KeyMode.Params, DataMode.String, 90),
    TEST(KeyMode.Fixed, DataMode.String, 3600),
    ;


    private final KeyMode keyMode;
    private final DataMode dataType;
    private final TTLMode ttlMode;
    private final Integer seconds;

    SysKeyDefine(KeyMode keyMode, DataMode dataType, Integer seconds) {
        this.keyMode = keyMode;
        this.dataType = dataType;
        this.seconds = seconds;
        this.ttlMode = TTLMode.NOW_ADD;
    }

    SysKeyDefine(KeyMode keyMode, DataMode dataType, TTLMode mode) {
        this.keyMode = keyMode;
        this.dataType = dataType;
        this.ttlMode = mode;
        this.seconds = null;

        if (ttlMode == TTLMode.NOW_ADD) {
            throw new IllegalArgumentException("请使用带有seconds参数的构造方法！");
        }
    }
}
