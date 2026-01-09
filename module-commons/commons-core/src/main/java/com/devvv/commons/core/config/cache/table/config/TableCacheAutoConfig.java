package com.devvv.commons.core.config.cache.table.config;

import com.devvv.commons.core.config.cache.table.cache.ICache;
import com.devvv.commons.core.config.cache.table.cache.RedisCache;
import com.devvv.commons.core.config.cache.table.cache.buffer.BufferedCache;
import com.devvv.commons.core.config.cache.table.manager.CacheExecutor;
import com.devvv.commons.core.config.cache.table.manager.CacheManager;
import com.devvv.commons.core.config.redis.condition.TableRedisCondition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Create by WangSJ on 2023/08/07
 */
@AutoConfiguration
@Conditional(TableRedisCondition.class)
public class TableCacheAutoConfig {


    /**
     * 缓存实现类，链路
     */
    @Bean
    public ICache iCache(@Qualifier("tableSpringBootRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        ICache cache = new RedisCache(redisTemplate);
        ICache bufferedCache = new BufferedCache(cache, redisTemplate);
        return bufferedCache;
    }

    /**
     * 表缓存执行器
     */
    @Bean
    public CacheExecutor dataProxy(ICache cache) {
        // 序列化器
        Serializer serializer = new FastJsonSerializable();
        CacheManager cm = new CacheManager(cache, serializer);
        return  new CacheExecutor(cm);
    }

    /**
     * 表缓存切面拦截器
     */
    @Bean
    public TableCacheAspect tableCacheAspect() {
        return new TableCacheAspect();
    }

}
