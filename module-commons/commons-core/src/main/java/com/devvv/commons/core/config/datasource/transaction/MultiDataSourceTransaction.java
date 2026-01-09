package com.devvv.commons.core.config.datasource.transaction;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionUtils;
import org.mybatis.spring.transaction.SpringManagedTransaction;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Create by WangSJ on 2023/07/03
 *
 * 接管MyBatis Spring 事务，如果使用了多数据源的事务则由Spring拦截器统一管理事务
 * <code>openTransaction\commit\rollback</code>
 *
 * 如果在无事务的情况下，则会每次操作数据库都打开新的连接，自动commit/rollbak， 在返回结果之前关闭连
 * 接，效率较低。
 *
 * 建议：使用Spring代理所有有关数据库的事务，包括只读事务，
 * 这样每次操作一个数据源只会打开一个连接 同库操作多次时只会使用第一次打开的连接。
 *
 * @see DataSourceUtils#getConnection(DataSource)
 *
 * @see TransactionInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
 * @see MultiDataSourceSynchronizationManager
 * @see SqlSessionFactoryBean
 * @see SqlSessionUtils#getSqlSession(org.apache.ibatis.session.SqlSessionFactory,
 *      org.apache.ibatis.session.ExecutorType,
 *      org.springframework.dao.support.PersistenceExceptionTranslator)
 */
public class MultiDataSourceTransaction extends SpringManagedTransaction {

    private final DataSource dataSource;
    private Connection nonTransactionConnection;

    public MultiDataSourceTransaction(DataSource dataSource) {
        super(dataSource);
        this.dataSource = dataSource;
    }

    /**
     * 获取连接
     *
     * @see DataSourceUtils#getConnection(DataSource)
     */
    public Connection getConnection() throws SQLException {
        // 如果开启了事务，直接从数据源中获取连接
        //   数据源代理中，也是从事务线程资源中，获取的连接，且支持在事务结束时统一关闭所有连接
        if (hasRoutingDataSourceTransactionManager()) {
            return openConnection();
        }
        // 检查，当前查询是否已经获取过连接，如果获取过，则使用已获取的连接
        if (this.nonTransactionConnection != null) {
            return this.nonTransactionConnection;
        }
        // 打开新连接
        return this.nonTransactionConnection = openConnection();

    }

    private Connection openConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public void commit() throws SQLException {
        if (hasRoutingDataSourceTransactionManager()) {
            // Does nothing
        } else {
            super.commit();
        }
    }

    public void rollback() throws SQLException {
        if (hasRoutingDataSourceTransactionManager()) {
            // Does nothing
        } else {
            super.rollback();
        }
    }

    public void close() throws SQLException {
        if (nonTransactionConnection == null) {
            // Does nothing
        } else {
            DataSourceUtils.releaseConnection(this.nonTransactionConnection, null);
        }
    }

    /**
     * 判断 是否在自定义的事务资源管理环境下
     */
    protected boolean hasRoutingDataSourceTransactionManager(){
        return MultiDataSourceSynchronizationManager.isSynchronizationActive();
    }
}
