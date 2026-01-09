package com.devvv.commons.core.config.redis.template;

import com.devvv.commons.core.config.redis.key.SysKeyDefine;
import com.devvv.commons.core.config.redis.key.KeyDefine;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Create by WangSJ on 2023/07/06
 */
public class SysRedisTemplate extends AbstractRedisTemplate {

    public SysRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    public Class<? extends KeyDefine> useRedisKeyDefinition() {
        return SysKeyDefine.class;
    }
}
