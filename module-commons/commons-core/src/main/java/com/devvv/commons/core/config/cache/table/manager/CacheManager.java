package com.devvv.commons.core.config.cache.table.manager;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.devvv.commons.core.config.cache.table.cache.ICache;
import com.devvv.commons.core.config.cache.table.cache.buffer.BufferedCache;
import com.devvv.commons.core.config.cache.table.exception.CacheException;
import com.devvv.commons.core.config.cache.table.config.Serializer;
import com.devvv.commons.core.config.datasource.annotation.Table;

/**
 * Create by WangSJ on 2023/08/07
 *
 * 缓存的核心管理器实现
 */
public class CacheManager {

    /**
     * @see BufferedCache
     */
    protected ICache cache;
    private Serializer serializer;

    public CacheManager(ICache cache, Serializer serializer) {
        this.cache = cache;
        this.serializer = serializer;
    }

    public String save(String key, Table tableAnnotation, Object bean) throws CacheException {
        // 如果是null值，Redis中存储字符串null
        String value;
        if (bean == null && tableAnnotation.cacheNullValue()) {
            value = "null";
        } else if (bean == null) {
            return null;
        } else {
            value = seriazile(bean);
        }

        // 如果是带有有效期的，则适用setex
        if (tableAnnotation.cacheExpire() > 0) {
            cache.setex(key, tableAnnotation.cacheExpire(), value);
        } else {
            cache.set(key, value);
        }
        return key;
    }

    public Pair<String, Object> get(String key, Class<?> returnType) throws CacheException {
        // 从缓存链中获取缓存
        String value = cache.get(key);
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return Pair.of(value, deserialize(returnType, value));
    }

    public void delete(String key, Table tableAnnotation, String selectAllKey) throws CacheException {
        cache.del(key);
        if (tableAnnotation.useSelectAll()) {
            cache.del(selectAllKey);
        }
    }

    /**
     * 序列化
     */
    protected <T> T deserialize(Class<T> clazz, String value)throws CacheException{
        try {
            return serializer.deserializeAsObject(value, clazz);
        } catch (Exception e) {
            throw new CacheException(clazz.getSimpleName() +": " + value, e);
        }
    }
    protected String seriazile(Object bean) throws CacheException {
        try {
            return serializer.seriazileAsString(bean);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }
}
