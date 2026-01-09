package com.devvv.commons.core.config.redis.template;

import com.devvv.commons.core.config.redis.key.KeyDefine;
import com.devvv.commons.core.config.redis.key.LimitKeyDefine;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Create by WangSJ on 2023/07/06
 */
public class LimitRedisTemplate extends AbstractRedisTemplate {

    public LimitRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /**
     * 获取SpringBoot的RedisTemplat
     */
    public RedisTemplate<String, String> getSpringBootRedisTemplate() {
        return super.redisTemplate;
    }

    @Override
    public Class<? extends KeyDefine> useRedisKeyDefinition() {
        return LimitKeyDefine.class;
    }
}
