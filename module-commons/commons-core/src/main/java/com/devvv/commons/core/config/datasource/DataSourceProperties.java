package com.devvv.commons.core.config.datasource;

import com.devvv.commons.common.enums.DBType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.Map;

/**
 * Create by WangSJ on 2025/02/20
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "data-source")
public class DataSourceProperties {
    // 基础配置
    private String driverClassName;
    private String username;
    private String password;
    private Integer initSize;
    private Integer maxActive;
    private Integer minIdle;
    private Long connectionTimeout;
    private String validationQuery;
    private Boolean testWhileIdle;
    private Boolean testOnBorrow;
    private Boolean testOnReturn;
    private Long timeBetweenEvictionRunsMillis;
    private Long idleTimeout;

    // 多数据源配置
    private Map<DBType, Item> types;
    // 正式启用的配置
    private String enableTypes;

    /**
     * 每个数据源的单独配置
     */
    @Data
    public static class Item {
        // 基本连接属性 （可覆盖基础配置）
        private String driverClassName;
        private String username;
        private String password;
        private Integer initSize;
        private Integer maxActive;
        private Integer minIdle;
        private Long connectionTimeout;
        private String validationQuery;
        private Boolean testWhileIdle;
        private Boolean testOnBorrow;
        private Boolean testOnReturn;
        private Long timeBetweenEvictionRunsMillis;
        private Long idleTimeout;

        private String jdbcUrl;
    }
}
