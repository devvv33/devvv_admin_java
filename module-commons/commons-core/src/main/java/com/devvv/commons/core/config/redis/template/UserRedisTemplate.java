package com.devvv.commons.core.config.redis.template;

import com.devvv.commons.core.config.redis.key.UserKeyDefine;
import com.devvv.commons.core.config.redis.key.KeyDefine;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Create by WangSJ on 2023/07/06
 */
public class UserRedisTemplate extends AbstractRedisTemplate {

    public UserRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    public Class<? extends KeyDefine> useRedisKeyDefinition() {
        return UserKeyDefine.class;
    }
}
