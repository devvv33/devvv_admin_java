package com.devvv.commons.core.config.redis.condition;

import com.devvv.commons.common.enums.redis.RedisType;
import com.devvv.commons.core.config.redis.RedisConfig;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Create by WangSJ on 2023/07/05
 */
public class TableRedisCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return RedisConfig.contains(RedisType.table, context);
    }
}
