package com.devvv.commons.core.config.cache.local;

import java.util.List;

/**
 * Create by WangSJ on 2024/07/22
 * 本地缓存
 */
public interface LocalCache {

    /**
     * 通知更新
     */
    public static void notifyReload(LocalCacheEnums cacheEnum, String... keys) {
        LocalCachePublisher.notifyReload(cacheEnum, keys);
    }

    /**
     * 缓存初始化
     */
    void init();

    /**
     * 根据key，重新加载缓存
     */
    void reload(List<String> keys);
}
