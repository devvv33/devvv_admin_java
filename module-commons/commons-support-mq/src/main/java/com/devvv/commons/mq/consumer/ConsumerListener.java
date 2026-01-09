package com.devvv.commons.mq.consumer;

import com.devvv.commons.common.enums.mq.MQTags;
import com.devvv.commons.common.enums.mq.MQTopic;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Create by WangSJ on 2024/07/01
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConsumerListener {


    @AliasFor("tags")
    MQTags[] value() default {MQTags.All};

    /**
     * 消费主题
     */
    MQTopic topic() default MQTopic.DEFAULT;

    /**
     * 消费标签
     */
    @AliasFor("value")
    MQTags[] tags() default {MQTags.All};

    /**
     * 消费组
     * 同一个group下的消费者，必须订阅相同的Topic和tags
     */
    String groupId();


    /**
     * 启用线程数
     */
    int threadNum() default 3;

}
