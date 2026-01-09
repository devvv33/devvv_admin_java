package com.devvv.commons.core.config.datasource.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Create by WangSJ on 2023/07/05
 */
@Slf4j
public class MyDataSourceUtils {

    /**
     * 从数据源获取一个连接，该连接是否新连接，取决于连接池厂商是否会缓存Connection的策略
     *
     * @param dataSource
     * @return
     * @throws CannotGetJdbcConnectionException
     */
    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        try {
            return doGetConnection(dataSource, null, null);
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Could not get JDBC Connection", ex);
        }
    }
    public static Connection getConnection(DataSource dataSource, String user, String pwd) throws CannotGetJdbcConnectionException {
        try {
            return doGetConnection(dataSource, user, pwd);
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Could not get JDBC Connection", ex);
        }
    }

    /**
     * 获取一个连接池，将连接池绑定到当前线程变量，在调用该方法的同时，
     * 需要显示调用 {@link #releaseConnection(Connection, DataSource)}
     *
     * @param dataSource
     * @return 获取代理后的链接
     * @throws SQLException
     */
    public static Connection doGetConnection(DataSource dataSource, String user, String password) throws SQLException {
        Assert.notNull(dataSource, "No DataSource specified");
        // 优先从当前上下文 获取链接
        ConnectionHolder holder = getCurrentConnectionHolder(dataSource, user, password);
        if(holder == null){
            Connection conn = null;
            if(StringUtils.isEmpty(user)){
                conn = dataSource.getConnection();
            }else{
                conn = dataSource.getConnection(user, password);
            }
            // 代理Connection，并封装成holder对象，存入当前线程上下文
            holder = new ConnectionHolder(new MyProxyConnection(conn), user, password);
            if (MultiDataSourceSynchronizationManager.isSynchronizationActive()) {
                MultiDataSourceSynchronizationManager.registerSynchronization(dataSource, holder);
                // 开启事务
                doBegin(conn, dataSource);
                prepareConnectionForTransaction(conn);
            }
        }
        // 增加引用计数
        holder.requested();
        if (log.isTraceEnabled()) {
            log.trace("获取连接 {} {}", holder.getReferenceCount(), holder.getConn());
        }
        return holder.getConn();
    }

    /**
     * 从当前线程上下文获取链接
     */
    public static ConnectionHolder getCurrentConnectionHolder(DataSource dataSource, String user, String password){
        if(!MultiDataSourceSynchronizationManager.isSynchronizationActive()){
            return null;
        }
        // 从当前线程上下文获取链接
        ConnectionHolder holder = MultiDataSourceSynchronizationManager.getSynchronizations().get(dataSource);
        if(holder != null && holder.equalUserAndPwd(user, password)){
            return holder;
        }
        return null;
    }

    /**
     * 开启事务
     */
    public static void doBegin(Connection con, DataSource dataSource) {
        try {
            if (con.getAutoCommit()) {
                // if (log.isDebugEnabled()) {
                //     log.debug("Switching JDBC Connection [" + con + "] to manual commit");
                // }
                con.setAutoCommit(false);
            }
        } catch (Throwable ex) {
            MyDataSourceUtils.releaseConnection(con);
            throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", ex);
        }
    }

    public static Integer prepareConnectionForTransaction(Connection con) throws SQLException {
        Assert.notNull(con, "No Connection specified");
        TransactionDefinition definition = MultiDataSourceSynchronizationManager.getCurrentTransactionDefinition();
        if (definition != null && definition.isReadOnly()) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Setting JDBC Connection [" + con + "] read-only");
                }
                con.setReadOnly(true);
            } catch (SQLException ex) {
                Throwable exToCheck = ex;
                while (exToCheck != null) {
                    if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
                        throw ex;
                    }
                    exToCheck = exToCheck.getCause();
                }
                log.debug("Could not set JDBC Connection read-only", ex);
            } catch (RuntimeException ex) {
                Throwable exToCheck = ex;
                while (exToCheck != null) {
                    if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
                        throw ex;
                    }
                    exToCheck = exToCheck.getCause();
                }
                log.debug("Could not set JDBC Connection read-only", ex);
            }
        }

        // Apply specific isolation level, if any.
        Integer previousIsolationLevel = null;
        if (definition != null && definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
            if (log.isDebugEnabled()) {
                log.debug("Changing isolation level of JDBC Connection [" + con + "] to " + definition.getIsolationLevel());
            }
            int currentIsolation = con.getTransactionIsolation();
            if (currentIsolation != definition.getIsolationLevel()) {
                previousIsolationLevel = currentIsolation;
                con.setTransactionIsolation(definition.getIsolationLevel());
            }
        }

        return previousIsolationLevel;
    }


    public static void releaseConnection(Connection con) {
        try {
            doCloseConnection(con);
        } catch (SQLException ex) {
            log.debug("Could not close JDBC Connection", ex);
        } catch (Throwable ex) {
            log.debug("Unexpected exception on closing JDBC Connection", ex);
        }
    }

    public static void doCloseConnection(Connection con) throws SQLException {
        log.info("关闭连接....");
        con.close();
    }

}
