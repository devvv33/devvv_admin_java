package com.devvv.commons.core.config.datasource;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.common.utils.CommonUtil;
import com.devvv.commons.core.config.datasource.routing.RoutingContext;
import com.devvv.commons.core.config.datasource.routing.RoutingContextHolder;
import com.devvv.commons.core.config.datasource.transaction.MyProxyDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.*;

/**
 * Create by WangSJ on 2023/07/03
 */
@Slf4j
public class MultiDataSource extends AbstractRoutingDataSource {

    private final Set<DBType> ENABLE_DB_TYPES = new HashSet<>();

    /**
     * 初始化所有数据库连接
     */
    public MultiDataSource(DataSourceProperties dataSourceProperties) {
        Map<Object, Object> targetDataSources = new HashMap<>();

        // 启用的配置
        Set<String> enableTypes = Opt.ofBlankAble(dataSourceProperties.getEnableTypes())
                .map(CommonUtil::splitStringSet)
                .orElse(Collections.emptySet());

        for (DBType type : dataSourceProperties.getTypes().keySet()) {
            if (!enableTypes.contains(type.name())) {
                continue;
            }
            // 构建数据库连接
            DataSource myDataSource = createDruidDataSource(type, dataSourceProperties.getTypes().get(type), dataSourceProperties);
            // 代理数据源
            MyProxyDataSource delegatingDataSource = new MyProxyDataSource(myDataSource);
            targetDataSources.put(type, delegatingDataSource);
            ENABLE_DB_TYPES.add(type);
        }

        // 设置所有数据库连接
        this.setTargetDataSources(targetDataSources);
    }

    /**
     * Drudi链接池
     * 参考文档: https://github.com/alibaba/druid/wiki/DruidDataSource%E9%85%8D%E7%BD%AE
     */
    public static DataSource createDruidDataSource(DBType dbType, DataSourceProperties.Item itemConfig, DataSourceProperties baseConfig) {
        Properties druidCfg = new Properties();
        druidCfg.put("druid.name", StrUtil.format("DataSource-{}", dbType.name()));
        druidCfg.put("druid.url", itemConfig.getJdbcUrl());
        druidCfg.put("druid.username", ObjectUtil.defaultIfBlank(itemConfig.getUsername(), baseConfig.getUsername()));
        druidCfg.put("druid.password", ObjectUtil.defaultIfBlank(itemConfig.getPassword(), baseConfig.getPassword()));
        // 属性类型是逗号隔开的字符串，通过别名的方式配置扩展插件，插件别名列表请参考druid jar包中的 /META-INF/druid-filter.properties,常用的插件有：
        // 监控统计用的filter:stat
        // 日志用的filter:log4j
        // 防御sql注入的filter:wall
        druidCfg.put("druid.filters", "stat,wall");

        druidCfg.put("druid.maxActive", ObjectUtil.defaultIfNull(itemConfig.getMaxActive(), baseConfig.getMaxActive()));
        druidCfg.put("druid.initialSize", ObjectUtil.defaultIfNull(itemConfig.getInitSize(), baseConfig.getInitSize()));
        druidCfg.put("druid.maxWait", ObjectUtil.defaultIfNull(itemConfig.getConnectionTimeout(), baseConfig.getConnectionTimeout()));
        druidCfg.put("druid.minIdle", ObjectUtil.defaultIfNull(itemConfig.getMinIdle(), baseConfig.getMinIdle()));

        druidCfg.put("druid.timeBetweenEvictionRunsMillis", ObjectUtil.defaultIfNull(itemConfig.getTimeBetweenEvictionRunsMillis(), baseConfig.getTimeBetweenEvictionRunsMillis()));
        druidCfg.put("druid.minEvictableIdleTimeMillis", ObjectUtil.defaultIfNull(itemConfig.getIdleTimeout(), baseConfig.getIdleTimeout()));

        druidCfg.put("druid.testWhileIdle", ObjectUtil.defaultIfNull(itemConfig.getTestWhileIdle(), baseConfig.getTestWhileIdle()));
        druidCfg.put("druid.testOnBorrow", ObjectUtil.defaultIfNull(itemConfig.getTestOnBorrow(), baseConfig.getTestOnBorrow()));
        druidCfg.put("druid.testOnReturn", ObjectUtil.defaultIfNull(itemConfig.getTestOnReturn(), baseConfig.getTestOnReturn()));
        druidCfg.put("druid.validationQuery", ObjectUtil.defaultIfNull(itemConfig.getValidationQuery(), baseConfig.getValidationQuery()));
        druidCfg.put("druid.poolPreparedStatements", "true");
        druidCfg.put("druid.maxOpenPreparedStatements", "20");
        druidCfg.put("druid.asyncInit", "true");
        druidCfg.put("druid.phyTimeoutMillis", "25200000");           // 强制回收物理连接的最大超时时长，大于0的情况下才生效，当物理创建之后存活的时长超过该值时，该连接会强制销毁，便于重新创建新连接，建议可以配置成7小时的毫秒值，比如25200000，这样可以规避MySQL的8小时连接断开问题

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.configFromPropeties(druidCfg);
        dataSource.setRemoveAbandoned(false);               // 是否回收泄露的连接,默认不开启，建议只在测试环境设置未开启，利用测试环境发现业务代码中未正常关闭连接的情况
        dataSource.setLogAbandoned(true);                   // 在开启removeAbandoned为true的情况，可以开启该设置，druid在销毁未及时关闭的连接时，则会输出日志信息，便于定位连接泄露问题
        dataSource.setRemoveAbandonedTimeout(5 * 60);
        dataSource.setUseGlobalDataSourceStat(true);        // 合并多个DruidDataSource的监控数据

        log.warn("初始化MySQL: {} {}", StrUtil.fixLength(dbType.name(), ' ', 7), itemConfig.getJdbcUrl());
        return dataSource;
    }

    /**
     * 多数据源- DB选择器
     */
    @Override
    protected Object determineCurrentLookupKey() {
        RoutingContext context = RoutingContextHolder.getRoutingContext();
        if (context == null) {
            return DBType.sys;
        }
        DBType dbType = context.getDbType();
        // 按数据库名称匹配
        if (dbType != null && ENABLE_DB_TYPES.contains(dbType)) {
            log.debug("UseDB:{}", dbType);
            return dbType;
        }
        throw new IllegalArgumentException("找不到数据源:[" + dbType + "] " + context.getMapperClass());
    }
}
