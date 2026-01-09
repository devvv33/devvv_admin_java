package com.devvv.commons.common.utils.function;

/**
 * Create by WangSJ on 2025/04/10
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {

    /**
     * 定义一个 带异常的 Function 方法
     * 在 lambda中可以不用捕捉异常
     */
    R apply(T t) throws Exception;
}