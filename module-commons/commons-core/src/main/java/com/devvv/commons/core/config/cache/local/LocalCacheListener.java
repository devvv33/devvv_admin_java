package com.devvv.commons.core.config.cache.local;

import cn.hutool.core.util.EnumUtil;
import com.alibaba.fastjson2.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

/**
 * Create by WangSJ on 2024/07/22
 */
@Slf4j
public class LocalCacheListener implements MessageListener {

    private final RedisSerializer<String> stringSerializer = StringRedisSerializer.UTF_8;

    /**
     * 接受变更监听
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = stringSerializer.deserialize(message.getChannel());
        LocalCacheEnums cacheEnum = EnumUtil.getBy(LocalCacheEnums::name, channel);
        if (cacheEnum == null) {
            log.warn("[Redis发布订阅]- 配置变更监听 监听到了未知的Topic:{}", channel);
            return;
        }
        String keys = stringSerializer.deserialize(message.getBody());
        List<String> keyList = JSONArray.parseArray(keys, String.class);

        // 获取对应的配置类
        LocalCache localCache = LocalCacheFactory.getInstance(cacheEnum, false);
        if (localCache == null) {
            return;
        }
        // 更新
        localCache.reload(keyList);
    }
}
