package com.devvv.commons.core.config.redis.template;

import com.devvv.commons.core.config.redis.key.KeyDefine;
import com.devvv.commons.core.config.redis.key.LogKeyDefine;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Create by WangSJ on 2023/07/06
 */
public class LogRedisTemplate extends AbstractRedisTemplate {

    public LogRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    public Class<? extends KeyDefine> useRedisKeyDefinition() {
        return LogKeyDefine.class;
    }
}
