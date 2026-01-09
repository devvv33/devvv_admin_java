package com.devvv.commons.core.utils;

import com.devvv.commons.core.config.datasource.transaction.busi.BusiTransactionResource;
import com.devvv.commons.core.config.datasource.transaction.busi.BusiTransactionResourceManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by WangSJ on 2024/06/28
 */
@Slf4j
public class BusiTransactionUtil {

    private static final String DEFAULT_TASK_FLG = "DefaultAfterCommitTask";


    /**
     * 提交一个任务
     *  如果当前环境没有事务，则立即执行
     *  如果当前环境有事务，则等待事务commit后再执行
     */
    public static void execAfterCommit(Runnable task){
        // 无事务时，即时执行
        if (!BusiTransactionResourceManager.inTransaction()) {
            task.run();
            return;
        }
        // 有事务，放到队列中，等待事务结束时执行
        if (!BusiTransactionResourceManager.hasResource(DEFAULT_TASK_FLG)) {
            BusiTransactionResourceManager.bindResource(DEFAULT_TASK_FLG, new DefaultTransactionResource());
        }
        DefaultTransactionResource buffer = BusiTransactionResourceManager.getResource(DEFAULT_TASK_FLG);
        buffer.addTask(task);
    }

    /**
     * 事务提交后执行
     */
    public static class DefaultTransactionResource implements BusiTransactionResource {

        private final List<Runnable> cache = new ArrayList<>();
        public void addTask(Runnable task){
            cache.add(task);
        }

        @Override
        public void begin() throws Throwable {
        }

        @Override
        public void commit() throws Throwable {
            for (Runnable task : cache) {
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("[事务任务]- commit DefaultAfterCommitTask 任务执行失败！", e);
                }
            }
        }

        @Override
        public void rollback() throws Throwable {
            if (log.isDebugEnabled()) {
                log.debug("[事务任务]- rollback DefaultAfterCommitTask 事务任务取消执行:{}", cache.size());
            }
            cache.clear();
        }
    }

}
