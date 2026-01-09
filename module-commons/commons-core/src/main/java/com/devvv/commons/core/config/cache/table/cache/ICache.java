package com.devvv.commons.core.config.cache.table.cache;

import com.devvv.commons.core.config.cache.table.exception.CacheException;

import java.util.List;
import java.util.Map;

/**
 * Create by WangSJ on 2023/08/07
 *
 * Cache相关操作的顶层接口
 *
 * @see RedisCache
 */
public interface ICache {

    boolean set(String cacheKey, String value) throws CacheException;

    boolean setex(String cacheKey, int seconds, String value) throws CacheException;

    boolean del(String cacheKey) throws CacheException;

    String get(String cacheKey) throws CacheException;

    List<String> gets(String... cacheKey) throws CacheException;

    boolean sets(Map<String, String> keyValueMap) throws CacheException;

    /**
     * 失效时间设置
     * @param key
     * @param seconds 毫秒
     * @return
     * @throws CacheException
     */
    boolean expire(String key, Integer seconds)throws CacheException;

}
