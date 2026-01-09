package com.devvv.commons.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Create by WangSJ on 2024/07/01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMQProperties {

    private String nameServerAddr;
    private String accessKey;
    private String accessSecret;

    private String env;
}
