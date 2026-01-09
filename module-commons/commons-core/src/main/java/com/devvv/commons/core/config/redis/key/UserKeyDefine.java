package com.devvv.commons.core.config.redis.key;

import com.devvv.commons.core.config.redis.mode.DataMode;
import com.devvv.commons.core.config.redis.mode.KeyMode;
import com.devvv.commons.core.config.redis.mode.TTLMode;
import lombok.Getter;

/**
 * Create by WangSJ on 2024/07/05
 */
@Getter
public enum UserKeyDefine implements KeyDefine {

    // 短信验证码登录校验-同一ip5分钟内次数限制
    LoginSmsCodeCheckIP(KeyMode.Params, DataMode.Incr, 5 * 60),
    // 短信验证码登录校验-同一手机号1分钟内次数限制
    LoginSmsCodeCheckMobile(KeyMode.Params, DataMode.String, 60),
    LoginSmsCode(KeyMode.Params, DataMode.String, 5 * 60),
    ;


    private final KeyMode keyMode;
    private final DataMode dataType;
    private final TTLMode ttlMode;
    private final Integer seconds;

    UserKeyDefine(KeyMode keyMode, DataMode dataType, Integer seconds) {
        this.keyMode = keyMode;
        this.dataType = dataType;
        this.seconds = seconds;
        this.ttlMode = TTLMode.NOW_ADD;
    }
    UserKeyDefine(KeyMode keyMode, DataMode dataType, TTLMode mode) {
        this.keyMode = keyMode;
        this.dataType = dataType;
        this.ttlMode = mode;
        this.seconds = null;

        if (ttlMode == TTLMode.NOW_ADD) {
            throw new IllegalArgumentException("请使用带有seconds参数的构造方法！");
        }
    }
}
