package com.devvv.commons.core.config.cache.table.manager;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.devvv.commons.core.config.cache.table.exception.CacheException;
import com.devvv.commons.core.config.cache.table.exception.DataProxyException;
import com.devvv.commons.core.config.datasource.annotation.Table;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Create by WangSJ on 2023/08/07
 *
 * 数据代理实现了持久化操作类与缓存的管理器的集成， 该类始终依赖CacheManager完成对缓存的维护， 并依赖
 * <code>Delegater</code>完成数据的持久化
 */
@Slf4j
public class CacheExecutor {

    /**
     * 为Target构造代理实例，使它具备缓存的管理
     */
    private CacheManager cacheManager;
    public CacheExecutor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void insert(Table tableAnnotation, String key, String selectAllKey, Object bean) throws DataProxyException {
        try {
            log.debug("[表缓存]- 删除表缓存 {}", key);
            cacheManager.delete(key, tableAnnotation, selectAllKey);
        } catch (CacheException e) {
            log.error("[表缓存]- 处理异常！", e);
        }
    }

    public Integer updateByPrimaryKey(Table tableAnnotation, String key, String selectAllKey, Delegater<Integer> target) throws DataProxyException {
        Integer rows = target.execute();
        try {
            if (rows > 0) {
                log.debug("[表缓存]- 删除表缓存 {}", key);
                cacheManager.delete(key, tableAnnotation, selectAllKey);
            }
        } catch (CacheException e) {
            log.error("[表缓存]- 处理异常！", e);
        }
        return rows;
    }

    public Integer deleteByPrimaryKey(Table tableAnnotation, String key, String selectAllKey, Delegater<Integer> target) throws DataProxyException {
        Integer rows = target.execute();
        try {
            if (rows > 0) {
                log.debug("[表缓存]- 删除表缓存 {}", key);
                cacheManager.delete(key, tableAnnotation, selectAllKey);
            }
        } catch (CacheException e) {
            log.error("[表缓存]- 处理异常！", e);
        }
        return rows;
    }

    public Object selectByPrimaryKey(Table tableAnnotation, String key, Class returnType, Delegater mapper) throws DataProxyException {
        Pair<String, Object> cacheResult = null;
        try {
            // 1、 先从缓存查询
            cacheResult = cacheManager.get(key, returnType);
        } catch (CacheException e) {
            log.error("[表缓存]- 处理异常！", e);
        }
        // 如果配置了允许存储null值，且redis中不为null，解析出来的结果为null
        // 此时直接返回null
        if (cacheResult != null && cacheResult.getValue() == null && cacheResult.getKey() != null && tableAnnotation.cacheNullValue()) {
            return null;
        }

        Object result;
        if (cacheResult == null || (result = cacheResult.getValue()) == null) {
            // 2、 从数据库查询
            result = mapper.execute();
            // 3、 写入到缓存中
            // 如果配置了允许存储null值，尝试序列化null到缓存中
            if (result != null || tableAnnotation.cacheNullValue()) {
                try {
                    cacheManager.save(key, tableAnnotation, result);
                } catch (CacheException e) {
                    log.error("[表缓存]- 处理异常！", e);
                }
            }
        }
        return result;
    }

    public Object selectAll(Table tableAnnotation, String selectAllKey, Class<?> returnType, MethodDefaultDelegater<List<Object>> mapper) throws DataProxyException {
        List<? extends Object> result;
        try {
            // 1、 先从缓存查询
            Pair<String, Object> cacheResult = cacheManager.get(selectAllKey, String.class);
            if (cacheResult != null && StrUtil.isNotBlank(cacheResult.getKey())) {
                String str = cacheResult.getKey();
                result = Opt.ofTry(() -> JSONArray.parseArray(str, returnType)).orElse(null);
                if (result != null) {
                    return result;
                }
            }
        } catch (CacheException e) {
            log.error("[表缓存]- 处理异常！", e);
        }

        // 2、 从数据库查询
        result = mapper.execute();
        // 3、 写入到缓存中
        // 如果配置了允许存储null值，尝试序列化null到缓存中
        if (result != null || tableAnnotation.cacheNullValue()) {
            try {
                cacheManager.save(selectAllKey, tableAnnotation, result);
            } catch (CacheException e) {
                log.error("[表缓存]- 处理异常！", e);
            }
        }
        return result;
    }
}
