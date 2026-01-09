package com.devvv.commons.core.config.redis;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ObjectUtil;
import com.devvv.commons.common.enums.redis.RedisType;
import com.devvv.commons.common.utils.CommonUtil;
import lombok.Data;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.ConditionContext;

import java.util.Collections;

/**
 * Create by WangSJ on 2023/07/05
 */
public class RedisConfig {

    public static boolean contains(RedisType redisType, ConditionContext context) {
        return Opt.ofBlankAble(context.getEnvironment().getProperty("redis.enable-types"))
                .map(CommonUtil::splitStringList)
                .orElse(Collections.emptyList())
                .contains(redisType.name());
    }

    @Data
    public static class RedisPoolConfig extends GenericObjectPoolConfig{
        private String host;
        private int port;
        private int database;
        private String password;
        private long timeout;
    }

    /**
     * 根据redis类型，获取连接配置
     */
    public static RedisPoolConfig getPoolConfig(RedisType redisType, RedisProperties redisProperties) {
        RedisProperties.Item item = redisProperties.getTypes().get(redisType);
        Assert.notNull(item, "Redis配置缺失: {}", redisType);

        RedisPoolConfig poolConfig = new RedisPoolConfig();
        poolConfig.setHost(ObjectUtil.defaultIfBlank(item.getHost(), redisProperties.getHost()));
        poolConfig.setPort(ObjectUtil.defaultIfNull(item.getPort(), redisProperties.getPort()));
        poolConfig.setDatabase(ObjectUtil.defaultIfNull(item.getDatabase(), ObjectUtil.defaultIfNull(redisProperties.getDatabase(), 0)));
        poolConfig.setPassword(ObjectUtil.defaultIfNull(item.getPassword(), redisProperties.getPassword()));
        poolConfig.setTimeout(ObjectUtil.defaultIfNull(item.getTimeout(), redisProperties.getTimeout()));

        poolConfig.setMinIdle(ObjectUtil.defaultIfNull(item.getMinIdle(), ObjectUtil.defaultIfNull(redisProperties.getMinIdle(), 3)));
        poolConfig.setMaxIdle(ObjectUtil.defaultIfNull(item.getMaxIdle(), ObjectUtil.defaultIfNull(redisProperties.getMaxIdle(), 5)));
        poolConfig.setMaxTotal(ObjectUtil.defaultIfNull(item.getMaxTotal(), ObjectUtil.defaultIfNull(redisProperties.getMaxTotal(), 5)));
        poolConfig.setMaxWaitMillis(ObjectUtil.defaultIfNull(item.getMaxWaitMillis(), ObjectUtil.defaultIfNull(redisProperties.getMaxWaitMillis(), -1L)));

        poolConfig.setTestOnBorrow(ObjectUtil.defaultIfNull(item.getTestOnBorrow(), ObjectUtil.defaultIfNull(redisProperties.getTestOnBorrow(), false)));
        poolConfig.setTestOnCreate(ObjectUtil.defaultIfNull(item.getTestOnCreate(), ObjectUtil.defaultIfNull(redisProperties.getTestOnCreate(), false)));
        poolConfig.setTestOnReturn(ObjectUtil.defaultIfNull(item.getTestOnReturn(), ObjectUtil.defaultIfNull(redisProperties.getTestOnReturn(), false)));
        poolConfig.setTestWhileIdle(ObjectUtil.defaultIfNull(item.getTestWhileIdle(), ObjectUtil.defaultIfNull(redisProperties.getTestWhileIdle(), false)));
        return poolConfig;
    }
}
