package com.devvv.commons.core.config.cache.table.manager;

import org.aspectj.lang.ProceedingJoinPoint;
import com.devvv.commons.core.config.cache.table.exception.DataProxyException;

/**
 * Create by WangSJ on 2023/08/09
 */
public class MethodDefaultDelegater<T> implements Delegater<T> {

    private final ProceedingJoinPoint joinPoint;

    public MethodDefaultDelegater(ProceedingJoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T execute() throws DataProxyException {
        try {
            Object o = joinPoint.proceed();
            if (o != null) {
                return (T) o;
            }
            return null;
        } catch (Throwable e) {
            throw DataProxyException.newIt(e);
        }
    }

}
