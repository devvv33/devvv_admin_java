package com.devvv.commons.core.config.datasource.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.*;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.Map;

/**
 * Create by WangSJ on 2023/07/05
 *
 * 多数据源连接的事务管理器
 * 当事务在 commit/rollback 时，所有数据源都被执行
 */
@Slf4j
public class MultiDatasourceTransactionManager implements PlatformTransactionManager {

    /**
     * 根据指定的传播行为，返回当前活动的事务或创建新事务。
     * 请注意，隔离级别或超时等参数将仅应用于新事务，因此在参与活动事务时会被忽略。
     * 此外，并非每个事务管理器都支持所有事务定义设置：当遇到不支持的设置时，正确的事务管理器实现应该抛出异常。
     * 上述规则的一个例外是只读标志，如果不支持显式只读模式，则应忽略该标志。从本质上讲，只读标志只是潜在优化的一个提示。
     *
     * 参数：
     * definition–TransactionDefinition实例（默认情况下可以为null），描述传播行为、隔离级别、超时等。
     * @return 表示新事务或当前事务的事务状态对象
     * @throws TransactionException–在查找、创建或系统错误的情况下
     */
    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        MultiDataSourceSynchronizationManager.initSynchronization(definition);
        return newTransactionStatus(definition, new TransactionObject());
    }

    /**
     * Create a rae TransactionStatus instance for the given arguments.
     */
    protected DefaultTransactionStatus newTransactionStatus(TransactionDefinition definition, Object transaction) {
        // Cache debug flag to avoid repeated checks.
        boolean debugEnabled = log.isDebugEnabled();

        return new DefaultTransactionStatus(transaction, true, true, definition.isReadOnly(), debugEnabled, null);
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        if(log.isDebugEnabled()){
            log.debug("Commit all transactions.");
        }
        DefaultTransactionStatus defStatus = (DefaultTransactionStatus)status;
        try {
            TransactionSynchronizationUtils.triggerBeforeCommit(defStatus.isReadOnly());
            TransactionSynchronizationUtils.triggerBeforeCompletion();
            Map<DataSource, ConnectionHolder> connSet = MultiDataSourceSynchronizationManager.getSynchronizations();
            boolean commit = true;
            Exception commitException = null;
            ConnectionHolder commitExceptionConnection = null;

            for (ConnectionHolder connection : connSet.values()) {
                if (commit) {
                    try {
                        connection.commit();
                    } catch (Exception ex) {
                        commit = false;
                        commitException = ex;
                        commitExceptionConnection = connection;
                    }
                } else {
                    try {
                        connection.rollback();
                    } catch (Exception ex) {
                        log.warn("Rollback exception (after commit) (" + connection + ") " + ex.getMessage(), ex);
                    }
                }
            }

            if (commitException != null) {
                boolean firstTransactionManagerFailed = (commitExceptionConnection == getLastConnectionHolder(connSet));
                int transactionState = firstTransactionManagerFailed ? HeuristicCompletionException.STATE_ROLLED_BACK : HeuristicCompletionException.STATE_MIXED;
                throw new HeuristicCompletionException(transactionState, commitException);
            }
        } finally {
            MultiDataSourceSynchronizationManager.invokeAfterCompletion(TransactionSynchronization.STATUS_COMMITTED);
            // 提交我们自己管理的所有连接
            MultiDataSourceSynchronizationManager.clearSynchronization();
        }
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        if(log.isDebugEnabled()){
            log.debug("Rollback all transactions.");
        }
        try {
            TransactionSynchronizationUtils.triggerBeforeCompletion();
            Map<DataSource, ConnectionHolder> connSet = MultiDataSourceSynchronizationManager.getSynchronizations();
            Exception rollbackException = null;
            ConnectionHolder rollbackExceptionConnection = null;

            for (ConnectionHolder connection : connSet.values()) {
                try {
                    connection.rollback();
                } catch (Exception ex) {
                    if (rollbackException == null) {
                        rollbackException = ex;
                        rollbackExceptionConnection = connection;
                    } else {
                        log.warn("Rollback exception (" + rollbackExceptionConnection + ") " + ex.getMessage(), ex);
                    }
                }
            }

            if (rollbackException != null) {
                throw new UnexpectedRollbackException("Rollback exception, originated at (" + rollbackExceptionConnection + ") " + rollbackException.getMessage(), rollbackException);
            }
        } finally {
            MultiDataSourceSynchronizationManager.invokeAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            // 提交我们自己管理的所有连接
            MultiDataSourceSynchronizationManager.clearSynchronization();
        }
    }

    private ConnectionHolder getLastConnectionHolder(Map<DataSource, ConnectionHolder> connSet) {
        if(!connSet.isEmpty()){
            int lastConnectionIndex = connSet.size() - 1;
            Iterator<ConnectionHolder> ite = connSet.values().iterator();
            for (int i = 0; ite.hasNext(); i++) {
                ConnectionHolder conn = ite.next();
                if(i == lastConnectionIndex){
                    return conn;
                }
            }
        }
        return null;
    }

    public static class TransactionObject{
        // Empty Object
    }

}
