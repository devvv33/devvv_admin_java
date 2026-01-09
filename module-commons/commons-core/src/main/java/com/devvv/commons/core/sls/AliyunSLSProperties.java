package com.devvv.commons.core.sls;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * Create by WangSJ on 2024/07/10
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "aliyun.sls")
public class AliyunSLSProperties {

    private String endpoint;
    private String project;
    private String accessKeyId;
    private String accessKeySecret;

    private String apiLogStore;
    private String logbackStore;
    private String busiStore;
    private int resultMaxLength = 500;
}
