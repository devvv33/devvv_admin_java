package com.devvv.commons.core.config.cache.local;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Create by WangSJ on 2024/07/22
 * Redis订阅更新
 */
@Slf4j
public class LocalCachePublisher {

    private static RedisTemplate<String, String> redisTemplate;
    private static RedisTemplate getRedisTemplate() {
        if (redisTemplate == null) {
            redisTemplate = SpringUtil.getBean("sysSpringBootRedisTemplate");
        }
        return redisTemplate;
    }

    /**
     * 发布更新通知
     */
    public static void notifyReload(LocalCacheEnums cacheEnum, String... keys) {
        if (getRedisTemplate() == null) {
            log.warn("未配置 sysRedis 本地缓存变更通知发送失败！");
            return;
        }
        String msg = keys == null ? "[]" : JSONArray.toJSONString(keys);
        getRedisTemplate().convertAndSend(cacheEnum.name(), msg);
    }


}