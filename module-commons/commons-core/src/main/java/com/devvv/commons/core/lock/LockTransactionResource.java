package com.devvv.commons.core.lock;

import com.devvv.commons.core.config.datasource.transaction.busi.BusiTransactionResource;
import com.devvv.commons.core.context.BusiContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;

/**
 * Create by WangSJ on 2024/06/28
 *
 * 分布式锁， 事务协同资源
 */
@Slf4j
public class LockTransactionResource implements BusiTransactionResource {

    private final RLock lock;
    private final Object key;

    public LockTransactionResource(RLock lock, String key) {
        this.lock = lock;
        this.key = key;
        if (log.isDebugEnabled()) {
            log.debug("加锁: {} {}", key, BusiContextUtil.getContext().getGlobalRequestId());
        }
    }

    // 分布式锁 要在事务其他行为都执行完毕后 再执行
    @Override
    public int order() {
        return 999;
    }

    @Override
    public void begin() throws Throwable {
    }

    @Override
    public void commit() throws Throwable {
        lock.unlock();
        if (log.isDebugEnabled()) {
            log.debug("释放锁-commit: {} {}", key, BusiContextUtil.getContext().getGlobalRequestId());
        }
    }

    @Override
    public void rollback() throws Throwable {
        lock.unlock();
        if (log.isDebugEnabled()) {
            log.debug("释放锁-rollback: {} {}", key,BusiContextUtil.getContext().getGlobalRequestId());
        }
    }
}
