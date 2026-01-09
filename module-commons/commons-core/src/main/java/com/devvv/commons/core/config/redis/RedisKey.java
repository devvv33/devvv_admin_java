package com.devvv.commons.core.config.redis;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.devvv.commons.core.config.redis.key.KeyDefine;
import com.devvv.commons.core.config.redis.mode.KeyMode;
import com.devvv.commons.core.config.redis.mode.TTLMode;

import java.util.Date;

/**
 * Create by WangSJ on 2023/07/05
 */
public class RedisKey {

    static final String KEY_TPL = "%s:%s";
    static final String KEY_PARAM_SPLIT = "_";

    private KeyDefine define;
    private Object[] params;
    private String key;
    private Integer seconds;

    /**
     * 创建RedisKey生成器
     */
    public static RedisKey create(KeyDefine define, Object... params) {
        RedisKey builder = new RedisKey();
        builder.define = define;
        builder.params = params;
        return builder;
    }

    /**
     * 完成Key的最终拼接
     */
    public String toKey() {
        if (key != null) {
            return key;
        }
        String keyPrefix = define.getKeyPrefix();

        // 如果Key是固定的
        if (define.getKeyMode() == KeyMode.Fixed) {
            return keyPrefix;
        }

        // 如果Key是带有参数的，则验证是否传入了参数
        if (define.getKeyMode() == KeyMode.Params && params.length == 0) {
            throw new IllegalArgumentException("使用了带参数的KeyMode，却没有传入相关的参数！");
        }

        String paramString = StrUtil.join(KEY_PARAM_SPLIT, params);
        key = String.format(KEY_TPL, keyPrefix, paramString);
        return key;
    }

    /**
     * 根据规则生成过期时间
     */
    public Integer toSeconds() {
        switch (define.getTtlMode()) {
            case NONE:
                seconds = null;
                break;
            case NOW_ADD:
                if (define.getSeconds() == null) {
                    String msg = StrUtil.format("缓存{}设置了TTLMode，但没有设置过期时间！", define.name());
                    throw new IllegalArgumentException(msg);
                }
                seconds = define.getSeconds();
                break;
            case END_OF_DAY:
                seconds = (int) DateUtil.between(new Date(), DateUtil.endOfDay(new Date()), DateUnit.SECOND);
                break;
            case END_OF_WEEK:
                seconds = (int) DateUtil.between(new Date(), DateUtil.endOfWeek(new Date()), DateUnit.SECOND);
                break;
            case END_OF_MONTH:
                seconds = (int) DateUtil.between(new Date(), DateUtil.endOfMonth(new Date()), DateUnit.SECOND);
                break;
            case DYNAMIC:
                if (seconds == null) {
                    throw new IllegalArgumentException(StrUtil.format("缓存{}TLL模式已设置DYNAMIC，但TTL的值却为NULL", define.name()));
                }
                break;
            default:
                seconds = null;
                break;
        }
        if (seconds == null) {
            return null;
        }
        // 避免有效期为负数
        return Math.max(seconds, 0);
    }

    public TTLMode getTTLMode() {
        return define.getTtlMode();
    }

    public RedisKey setTTL(Integer secondsValue) {
        if (define.getTtlMode() != TTLMode.DYNAMIC) {
            throw new IllegalArgumentException("只有当缓存有效期模式为DYNAMIC时，才能进行设置!");
        }
        this.seconds = secondsValue;
        return this;
    }

    public KeyDefine getKeyDefine() {
        return this.define;
    }
}
