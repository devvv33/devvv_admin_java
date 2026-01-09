package com.devvv.commons.core.config.datasource;

import com.devvv.commons.core.config.datasource.sharding.TableShardPlugin;
import com.devvv.commons.core.config.datasource.transaction.MultiDataSourceTransactionFactory;
import com.devvv.commons.core.config.datasource.transaction.busi.BusiTransactionManager;
import com.devvv.commons.core.config.datasource.typehandler.IDEnumTypeHandler;
import com.devvv.commons.core.config.datasource.typehandler.IntIDEnumTypeHandler;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;

/**
 * Create by WangSJ on 2023/07/03
 */
@Configuration
@ConditionalOnProperty(name = "dataSource.driverClassName")
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfig {

    @Autowired
    private DataSourceProperties dataSourceProperties;

    /**
     * 多数据源
     */
    @Bean
    public MultiDataSource dataSource() {
        return new MultiDataSource(dataSourceProperties);
    }

    /**
     * 分表
     */
    @Bean
    public TableShardPlugin tableShardPlugin() {
        return new TableShardPlugin();
    }


    /**
     * 多数据源的事务创建工厂
     * 接管Spring的数据库连接管理，不提交事务，由资源管理统一提交事务
     */
    @Bean
    public MultiDataSourceTransactionFactory transactionFactory() {
        return new MultiDataSourceTransactionFactory();
    }

    /**
     * 带分布式锁的事务管理器
     * 可同时管理事务资源
     */
    @Bean
    public BusiTransactionManager transactionManager() {
        return new BusiTransactionManager();
    }

    /**
     * SqlSessionFactory
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, MultiDataSourceTransactionFactory transactionFactory, TableShardPlugin tableShardPlugin) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);

        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        Resource[] mappers = resourceResolver.getResources("classpath*:mapper/*/*.xml");
        bean.setMapperLocations(mappers);

        bean.setVfs(SpringBootVFS.class);
        bean.setTypeAliasesPackage("com.devvv.**.dao.entity");
        bean.setTypeHandlers(new IDEnumTypeHandler(), new IntIDEnumTypeHandler());
        bean.setTransactionFactory(transactionFactory);
        bean.setPlugins(tableShardPlugin);

        SqlSessionFactory ssf = bean.getObject();
        ssf.getConfiguration().setJdbcTypeForNull(JdbcType.NULL);
        ssf.getConfiguration().setMapUnderscoreToCamelCase(true);
        ssf.getConfiguration().setCacheEnabled(false);
        ssf.getConfiguration().setLocalCacheScope(LocalCacheScope.STATEMENT);
        return ssf;
    }
}
