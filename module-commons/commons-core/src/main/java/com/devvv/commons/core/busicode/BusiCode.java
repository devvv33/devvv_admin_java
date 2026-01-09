package com.devvv.commons.core.busicode;

import com.devvv.commons.common.enums.mq.MQTags;
import com.devvv.commons.common.enums.mq.MQTopic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Create by WangSJ on 2024/07/01
 *
 * 业务标识，对业务进行标记区分
 * 可扩展支持: 转发MQ消息等
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BusiCode {

    /**
     * 业务代码
     */
    BusiCodeDefine value() default BusiCodeDefine.None;


    /**
     * 转发业务消息通知
     */
    MQTopic sendMQTopic() default MQTopic.DEFAULT;
    MQTags[] sendMQTags() default {};
}
