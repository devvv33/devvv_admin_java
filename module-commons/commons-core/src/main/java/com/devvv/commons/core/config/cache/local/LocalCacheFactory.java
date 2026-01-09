package com.devvv.commons.core.config.cache.local;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create by WangSJ on 2024/07/22
 */
public class LocalCacheFactory {

    private static final ConcurrentHashMap<LocalCacheEnums, Object> MAPPING = new ConcurrentHashMap<>();
    private static final Set<LocalCacheEnums> NULL_VALUE = new HashSet<>();
    private static final Logger log = LoggerFactory.getLogger(LocalCacheFactory.class);

    /**
     * 获取缓存实例
     * @param cacheEnum     缓存枚举
     * @param create        当缓存未实例化时，是否初始化
     */
    public static <T extends LocalCache> T getInstance(LocalCacheEnums cacheEnum,boolean create) {
        if (!create) {
            return (T) MAPPING.get(cacheEnum);
        }
        Object instance = MAPPING.computeIfAbsent(cacheEnum, c -> {
            try {
                Class<T> clazz = ClassUtil.loadClass(c.getClassName());
                if (clazz == null) {
                    NULL_VALUE.add(cacheEnum);
                    return new Object();
                }
                // 优先从Spring容器中获取bean，获取不到再尝试实例化
                T localCache =  Opt.ofTry(() -> SpringUtil.getBean(clazz)).orElseGet(() -> ReflectUtil.newInstance(clazz));
                // 初始化
                log.warn("[本地缓存]- 初始化: {}", cacheEnum);
                localCache.init();
                return localCache;
            } catch (UtilException e) {
                if (!ExceptionUtil.isCausedBy(e, ClassNotFoundException.class)) {
                    log.error("[本地缓存]- 实例化失败！Enum:{} class:{}", cacheEnum, cacheEnum.getClassName(), e);
                }
                NULL_VALUE.add(cacheEnum);
                return new Object();
            }
        });
        if (NULL_VALUE.contains(cacheEnum)) {
            return null;
        }
        return (T) instance;
    }

}
