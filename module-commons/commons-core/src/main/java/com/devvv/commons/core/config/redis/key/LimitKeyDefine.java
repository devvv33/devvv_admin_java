package com.devvv.commons.core.config.redis.key;

import com.devvv.commons.core.config.redis.mode.DataMode;
import com.devvv.commons.core.config.redis.mode.KeyMode;
import com.devvv.commons.core.config.redis.mode.TTLMode;
import lombok.Getter;

/**
 * Create by WangSJ on 2024/02/04
 */
@Getter
public enum LimitKeyDefine implements KeyDefine {
    // Api 重复请求检查，防止重放攻击
    Api(KeyMode.Params, DataMode.String, 60),
    // MQ重复消费检查
    MQ(KeyMode.Params, DataMode.String, 600),
    ;


    private final KeyMode keyMode;
    private final DataMode dataType;
    private final TTLMode ttlMode;
    private final Integer seconds;

    LimitKeyDefine(KeyMode keyMode, DataMode dataType, Integer seconds) {
        this.keyMode = keyMode;
        this.dataType = dataType;
        this.seconds = seconds;
        this.ttlMode = TTLMode.NOW_ADD;
    }

    LimitKeyDefine(KeyMode keyMode, DataMode dataType, TTLMode mode) {
        this.keyMode = keyMode;
        this.dataType = dataType;
        this.ttlMode = mode;
        this.seconds = null;

        if (ttlMode == TTLMode.NOW_ADD) {
            throw new IllegalArgumentException("请使用带有seconds参数的构造方法！");
        }
    }
}
