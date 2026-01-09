package com.devvv.commons.core.config.cache.table.cache;

import com.devvv.commons.core.config.cache.table.exception.CacheException;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Create by WangSJ on 2023/08/07
 */
public class RedisCache implements ICache {

    private RedisTemplate<String,String> redisTemplate ;

    public RedisCache(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean set(String cacheKey, String value) throws CacheException {
        redisTemplate.boundValueOps(cacheKey).set(value);
        return true;
    }

    public boolean setex(String cacheKey, int seconds, String value){
        redisTemplate.boundValueOps(cacheKey).set(value, seconds, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public List<String> gets(String...cacheKey) {
        return redisTemplate.opsForValue().multiGet(Arrays.asList(cacheKey));
    }

    @Override
    public boolean sets(Map<String,String> keyValueMaps) {
        redisTemplate.opsForValue().multiSet(keyValueMaps);
        return true;
    }

    @Override
    public boolean del(String cacheKey) throws CacheException {
        return Boolean.TRUE.equals(redisTemplate.delete(cacheKey));
    }

    @Override
    public String get(String cacheKey) throws CacheException {
        return redisTemplate.opsForValue().get(cacheKey);
    }

    @Override
    public boolean expire(String key, Integer seconds) throws CacheException {
        return Boolean.TRUE.equals(redisTemplate.expire(key, seconds, TimeUnit.SECONDS));
    }
}
