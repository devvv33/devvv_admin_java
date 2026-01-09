package com.devvv.commons.core.lock;

import cn.hutool.core.lang.Assert;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.common.response.ErrorCode;
import com.devvv.commons.common.utils.function.ThrowingRunnable;
import com.devvv.commons.core.config.datasource.transaction.busi.BusiTransactionResourceManager;
import com.devvv.commons.core.config.redis.redisson.SysRedisson;
import com.devvv.commons.core.context.BusiContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingSupplier;

import java.util.concurrent.TimeUnit;

/**
 * Create by WangSJ on 2024/06/28
 */
@Slf4j
@Component
public class BusiRedissonLockUtil {

    private static SysRedisson sysRedisson;
    @Autowired(required = false)
    private void setSysRedisson(SysRedisson sysRedisson) {
        BusiRedissonLockUtil.sysRedisson = sysRedisson;
    }

    private static final String USER_LOCK_FLG = "UserLock";
    private static final String USER_LOCK_KEY_PREFIX = "lock:userId:";
    private static final String OBJECT_LOCK_FLG = "ObjectLock";
    private static final String OBJECT_LOCK_KEY_PREFIX = "lock:obj:";

    /**
     * 添加用户锁
     *  1、必须在事务环境中加锁
     *  2、必须有上下文环境{@link BusiContextHolder#getOrSetGlobalRequestId}，因为我们支持同一请求下 多个服务进程都可以对同一个用户加锁
     *  3、无需手动释放锁，事务结束后将会自动释放，基于{@link BusiContextHolder#getOrSetGlobalRequestId}
     */
    public static void lockUserId(Long userId) {
        Assert.notNull(userId, () -> new BusiException(ErrorCode.LOCK_ERROR, "用户id不能为空", "系统内部错误"));
        // 必须带事务加锁
        if (!BusiTransactionResourceManager.inTransaction()) {
            throw new BusiException(ErrorCode.LOCK_ERROR, "必须在事务环境下才能添加用户锁", "系统内部错误");
        }

        // 有事务，放到队列中，等待事务结束时执行
        if (BusiTransactionResourceManager.hasResource(USER_LOCK_FLG)) {
            throw new BusiException(ErrorCode.LOCK_ERROR, "当前事务已添加用户锁，无法重复加锁", "系统内部错误");
        }

        String lockKey = USER_LOCK_KEY_PREFIX + userId;
        RLock lock = sysRedisson.getMyGlobalRequestLock(lockKey);
        try {
            // 加锁
            if (lock.tryLock(30, TimeUnit.SECONDS)) {
                // 将锁添加到事务管理中
                BusiTransactionResourceManager.bindResource(USER_LOCK_FLG, new LockTransactionResource(lock, lockKey));
                return;
            }
        } catch (Exception e) {
            lock.unlock();  // 如果发生异常，释放锁
            log.error("[分布式锁]- 用户锁 加锁失败！userId:{}", userId, e);
        }
        throw new BusiException(ErrorCode.LOCK_ERROR, "分布式用户锁加锁失败！", "系统内部错误");
    }


    /**
     * 添加对象锁
     *  1、必须在事务环境中加锁
     *  2、必须有上下文环境{@link BusiContextHolder#getOrSetGlobalRequestId}，因为我们支持同一请求下 多个服务都可以对同一个用户加锁
     *  3、无需手动释放锁，事务结束后将会自动释放，基于{@link BusiContextHolder#getOrSetGlobalRequestId}
     */
    public static void lockObject(String key) {
        Assert.notBlank(key, () -> new BusiException(ErrorCode.LOCK_ERROR, "key不能为空", "系统内部错误"));
        // 必须带事务加锁
        if (!BusiTransactionResourceManager.inTransaction()) {
            throw new BusiException(ErrorCode.LOCK_ERROR, "必须在事务环境下才能添加对象锁", "系统内部错误");
        }

        // 有事务，放到队列中，等待事务结束时执行
        if (BusiTransactionResourceManager.hasResource(OBJECT_LOCK_FLG)) {
            throw new BusiException(ErrorCode.LOCK_ERROR, "当前事务已添加对象锁，无法重复加锁", "系统内部错误");
        }

        String lockKey = OBJECT_LOCK_KEY_PREFIX + key;
        RLock lock = sysRedisson.getMyGlobalRequestLock(lockKey);
        try {
            // 加锁
            if (lock.tryLock(30, TimeUnit.SECONDS)) {
                // 将锁添加到事务管理中
                BusiTransactionResourceManager.bindResource(OBJECT_LOCK_FLG, new LockTransactionResource(lock, lockKey));
                return;
            }
        } catch (Exception e) {
            lock.unlock();  // 如果发生异常，释放锁
            log.error("[分布式锁]- 对象锁 加锁失败！lockKey:{}", lockKey, e);
        }
        throw new BusiException(ErrorCode.LOCK_ERROR, "分布式对象锁加锁失败!", "系统内部错误");
    }


    /**
     * 简单的尝试加锁后执行任务
     * 不依赖事务
     * 方法执行完毕即释放锁
     * 有任务执行时直接抛出异常
     */
    public static <T> TryLockResult<T> tryLockExec(String lockKey, ThrowingSupplier<T> supplier) {
        TryLockResult<T> result = new TryLockResult();
        result.setLockKey(lockKey);
        RLock lock = sysRedisson.getLock(lockKey);
        if (!lock.tryLock()) {
            result.setLocked(true);
            return result;
        }
        try {
            T t = supplier.get();
            result.setResult(t);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return result;
    }
    public static <T> TryLockResult<T> tryLockExec(String lockKey, ThrowingRunnable runnable) {
        TryLockResult<T> result = new TryLockResult();
        result.setLockKey(lockKey);
        RLock lock = sysRedisson.getLock(lockKey);
        if (!lock.tryLock()) {
            result.setLocked(true);
            return result;
        }
        try {
            runnable.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return result;
    }
}
