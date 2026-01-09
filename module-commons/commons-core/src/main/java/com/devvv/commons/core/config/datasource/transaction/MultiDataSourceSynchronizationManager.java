package com.devvv.commons.core.config.datasource.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * Create by WangSJ on 2023/07/03
 * <p>
 * 多数据源管理器
 * 用于维护同一事务下，所有的数据库连接
 */
@Slf4j
public class MultiDataSourceSynchronizationManager {

    private static final ThreadLocal<LinkedHashMap<DataSource, ConnectionHolder>> synchronizations = new NamedThreadLocal<>("Transactional synchronizations");
    private static final ThreadLocal<TransactionDefinition> definitions = new NamedThreadLocal<>("Transactional definition");

    public static TransactionDefinition getCurrentTransactionDefinition() {
        if (!isSynchronizationActive()) {
            throw new IllegalStateException("Transaction synchronization is not active");
        }
        return definitions.get();
    }

    public static boolean isSynchronizationActive() {
        return (synchronizations.get() != null);
    }

    public static void initSynchronization(TransactionDefinition definition) throws IllegalStateException {
        if (isSynchronizationActive()) {
            throw new IllegalStateException("当前事务已存在，不可开启新的事务！");
        }
        synchronizations.set(new LinkedHashMap<>());
        definitions.set(definition);

        // 初始化Spring同步事务管理器，这使得Mybatis让Spring接管事务时，有一个判断标准，
        // 不会重复打开Session，也不会直接关闭Session
        // 具体请参看 org.mybatis.spring.SqlSessionUtils#getSqlSession
        // org.mybatis.spring.SqlSessionTemplate.SqlSessionInterceptor#invoke
        TransactionSynchronizationManager.initSynchronization();
    }

    public static void registerSynchronization(DataSource dataSource, ConnectionHolder connection) throws IllegalStateException {
        Assert.notNull(connection, "connection must not be null");
        if (!isSynchronizationActive()) {
            throw new IllegalStateException("事务未开启！");
        }
        synchronizations.get().put(dataSource, connection);
    }

    public static Map<DataSource, ConnectionHolder> getSynchronizations() throws IllegalStateException {
        Map<DataSource, ConnectionHolder> synchs = synchronizations.get();
        if (synchs == null) {
            throw new IllegalStateException("事务未开启！");
        }
        if (synchs.isEmpty()) {
            return Collections.emptyMap();
        }
        return synchs;
    }

    public static void clearSynchronization() throws IllegalStateException {
        if (!isSynchronizationActive()) {
            throw new IllegalStateException("Cannot deactivate transaction synchronization - not active");
        }

        try {
            Map<DataSource, ConnectionHolder> synchs = synchronizations.get();
            List<SQLException> exceptions = new ArrayList<SQLException>();
            for (ConnectionHolder connection : synchs.values()) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    exceptions.add(e);
                }
            }

            if (!exceptions.isEmpty()) {
                for (SQLException sqlException : exceptions) {
                    log.error("Close the connection error", sqlException);
                }
            }
        } finally {
            synchronizations.remove();
            definitions.remove();
            // Clean up the Spring transaction synch
            TransactionSynchronizationManager.clear();
        }
    }

    public static void invokeAfterCompletion(int completionStatus) {
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        TransactionSynchronizationUtils.invokeAfterCompletion(synchronizations, completionStatus);
    }
}
