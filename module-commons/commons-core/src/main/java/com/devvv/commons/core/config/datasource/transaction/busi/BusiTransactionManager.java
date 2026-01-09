package com.devvv.commons.core.config.datasource.transaction.busi;

import com.devvv.commons.core.config.datasource.transaction.MultiDatasourceTransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import java.util.Comparator;
import java.util.Map;

/**
 * Create by WangSJ on 2023/07/05
 *
 * 给事务上下文绑定资源
 * 使这些资源可以在事务整体 begin/commit/rollback 后被调用
 */
@Slf4j
public class BusiTransactionManager extends MultiDatasourceTransactionManager {

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        if (log.isTraceEnabled()) {
            log.trace("事务资源初始化...");
        }
        TransactionStatus status = super.getTransaction(definition);
        try {
            BusiTransactionResourceManager.initTransactionResource();
            triggerBegin(status);
        } catch (Throwable e) {
            throw new CannotCreateTransactionException("Unable to open the transaction", e);
        }
        return status;
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        try {
            super.commit(status);
            // 提交所有事务资源
            triggerCommit(status);
        } catch (TransactionException te) {
            triggerRollback(status);
            throw te;
        } finally {
            // finally将保证cleanup被调用
            cleanup();
        }
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        try {
            TransactionException rollbackException = null;
            try {
                super.rollback(status);
            } catch (TransactionException te) {
                rollbackException = te;
            }

            // 保证触发资源回滚
            triggerRollback(status);

            if (rollbackException != null) {
                throw rollbackException;
            }
        } finally {
            cleanup();
        }
    }

    /**
     * 触发事务开始方法
     */
    protected void triggerBegin(TransactionStatus status)throws Throwable{
        Map<Object, BusiTransactionResource> trs = BusiTransactionResourceManager.getResourceMap();
        for (BusiTransactionResource tr : trs.values()) {
            tr.begin();
        }
    }

    /**
     * 触发资源提交，使得每一个资源实例都触发提交，
     * 如果一旦出现异常，简单的记录日志，并继续下一资源的提交任务
     *
     * @param status
     */
    protected void triggerCommit(TransactionStatus status){
        if (log.isTraceEnabled()) {
            log.trace("事务资源-commit");
        }
        BusiTransactionResourceManager.getResourceMap()
                .entrySet().stream()
                .sorted(Comparator.comparingInt(v -> v.getValue().order()))
                .forEach((entry)->{
                    Object key = entry.getKey();
                    BusiTransactionResource res = entry.getValue();
                    try {
                        res.commit();
                    } catch (Throwable e) {
                        log.error("[事务任务]- commit 执行异常！ key:{} res:{}",key,res.getClass().getSimpleName(), e);
                    }
                });
    }

    /**
     * 触发资源回滚，使得每一个资源实例都触发回滚
     * @param status
     */
    protected void triggerRollback(TransactionStatus status){
        if (log.isTraceEnabled()) {
            log.trace("事务资源-rollback");
        }
        BusiTransactionResourceManager.getResourceMap()
                .entrySet().stream()
                .sorted(Comparator.comparingInt(v -> v.getValue().order()))
                .forEach((entry)->{
                    Object key = entry.getKey();
                    BusiTransactionResource res = entry.getValue();
                    try {
                        res.rollback();
                    } catch (Throwable e) {
                        log.error("[事务任务]- rollback 执行异常！ key:{} res:{}",key,res.getClass().getSimpleName(), e);
                    }
                });
    }

    /**
     * 事务资源的清理任务
     * 清理线程变量
     */
    protected void cleanup(){
        BusiTransactionResourceManager.clear();
    }

}
