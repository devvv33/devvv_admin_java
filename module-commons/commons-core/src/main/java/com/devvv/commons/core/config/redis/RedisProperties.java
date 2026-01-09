package com.devvv.commons.core.config.redis;

import com.devvv.commons.common.enums.redis.RedisType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.Map;

/**
 * Create by WangSJ on 2025/02/20
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {
    private String host;
    private Integer port;
    private String password;
    private Integer database;

    private Integer minIdle;
    private Integer maxIdle;
    private Integer maxTotal;
    private Long timeout;
    private Long maxWaitMillis;
    private Boolean testOnBorrow;
    private Boolean testOnCreate;
    private Boolean testOnReturn;
    private Boolean testWhileIdle;


    // 多数据源配置
    private Map<RedisType, Item> types;

    /**
     * 每个数据源的单独配置
     */
    @Data
    public static class Item {
        // 基本连接属性 （可覆盖基础配置）
        private String host;
        private Integer port;
        private String password;
        private Integer database;

        private Integer minIdle;
        private Integer maxIdle;
        private Integer maxTotal;
        private Long timeout;
        private Long maxWaitMillis;
        private Boolean testOnBorrow;
        private Boolean testOnCreate;
        private Boolean testOnReturn;
        private Boolean testWhileIdle;
        private String jdbcUrl;
    }
}
