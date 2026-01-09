package com.devvv.commons.mq;

import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;

/**
 * Create by WangSJ on 2024/06/25
 */
@ComponentScan("com.devvv.commons.mq")
public class MQModuleImport {

    static{
        // 让RocketMQ的日志使用项目中的slf4j，因为其默认输出的日志文件不会自动切割，会造成日志文件过大，占用磁盘空间
        // 参看: com.aliyun.openservices.ons.api.impl.util.ClientLoggerUtil
        System.setProperty("rocketmq.client.logUseSlf4j", "true");
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("com.aliyun.openservices")).setLevel(Level.WARN);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("RocketmqClient")).setLevel(Level.ERROR);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("RocketmqRemoting")).setLevel(Level.ERROR);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("RocketmqCommon")).setLevel(Level.ERROR);
    }
}
