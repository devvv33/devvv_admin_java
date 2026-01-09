package com.devvv.commons.core.config.datasource.transaction;


import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Create by WangSJ on 2023/07/05
 *
 * 本代理，主要重写 getConnection 逻辑
 * 保证在事物环境下，获取到的连接是 MyProxyConnection
 */
public class MyProxyDataSource implements DataSource {

    private DataSource targetDataSource;

    /**
     * 创建一个新的数据源.
     * @param targetDataSource the target DataSource
     */
    public MyProxyDataSource(DataSource targetDataSource) {
        this.targetDataSource = targetDataSource;
    }


    @Override
    public Connection getConnection() throws SQLException {
        return getConnection0(null, null);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection0(username, password);
    }

    private Connection getConnection0(String username, String password)throws SQLException{
        return MyDataSourceUtils.getConnection(this.targetDataSource, username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return this.targetDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.targetDataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.targetDataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return this.targetDataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.targetDataSource.getParentLogger();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return targetDataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || targetDataSource.isWrapperFor(iface));
    }
}
