package com.devvv.commons.core.config.cache.local;

import com.devvv.commons.core.config.redis.condition.SysRedisCondition;
import com.devvv.commons.core.utils.BusiThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Arrays;
import java.util.List;

/**
 * Create by WangSJ on 2024/07/22
 */
@Slf4j
@Configuration
@Conditional(SysRedisCondition.class)
public class LocalCacheAutoConfiguration {

    /**
     * redis 发布订阅  注册监听者
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListener(@Qualifier("sysJedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        List<ChannelTopic> topicList = Arrays.stream(LocalCacheEnums.values())
                .map(LocalCacheEnums::name)
                .map(ChannelTopic::new)
                .toList();

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 定义监听类 与 监听的Topic 可以一对多
        container.addMessageListener(new LocalCacheListener(), topicList);
        // 虚拟线程池
        container.setTaskExecutor(BusiThreadPoolUtil.DEFAULT_VIRTUAL_THREAD);
        return container;
    }
}
