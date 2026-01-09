package com.devvv.commons.core.lock;

import cn.hutool.core.util.StrUtil;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.common.response.ErrorCode;

import java.util.function.Function;

/**
 * Create by WangSJ on 2025/04/10
 */

public class TryLockResult<T>{
    private String lockKey;
    private T result;
    private boolean locked;

    public void setLockKey(String lockKey){
        this.lockKey = lockKey;
    }
    public String getLockKey(){
        return lockKey;
    }
    public void setLocked(boolean locked){
        this.locked = locked;
    }
    public boolean isLocked() {
        return locked;
    }
    public void setResult(T result){
        this.result = result;
    }

    /**
     * 如果加锁失败，抛出指定异常
     * 加锁成功，则返回执行后的结果
     */
    public T ifLockedThrow(RuntimeException e) {
        if (locked) {
            throw e;
        }
        return result;
    }

    public T ifLockedThrow(Function<String, RuntimeException> function) {
        if (locked) {
            throw function.apply(lockKey);
        }
        return result;
    }

    public T ifLockedThrow(String msg) {
        if (locked) {
            throw new BusiException(ErrorCode.LOCK_ERROR, StrUtil.format("分布式锁加锁失败: [{}]", lockKey), msg);
        }
        return result;
    }

    public T ifLockedThrow() {
        return ifLockedThrow("任务正在进行中，请稍后再试");
    }
}