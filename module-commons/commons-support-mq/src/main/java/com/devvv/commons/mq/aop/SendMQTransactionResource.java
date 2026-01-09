package com.devvv.commons.mq.aop;

import com.devvv.commons.core.config.datasource.transaction.busi.BusiTransactionResource;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by WangSJ on 2024/07/02
 */
@Slf4j
public class SendMQTransactionResource implements BusiTransactionResource {

    private List<Runnable> list = new ArrayList<>();

    public void addTask(Runnable task) {
        list.add(task);
    }


    @Override
    public int order() {
        return 99999;
    }

    @Override
    public void begin() throws Throwable {

    }

    @Override
    public void commit() throws Throwable {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Runnable task : list) {
            try {
                task.run();
            } catch (Exception e) {
                log.error("[MQ-Producer]- commit 提交失败！", e);
            }
        }
    }

    @Override
    public void rollback() throws Throwable {
        if (list == null || list.isEmpty()) {
            return;
        }
        list.clear();
    }
}
