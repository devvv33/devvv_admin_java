package com.devvv.commons.common.utils.function;

/**
 * Create by WangSJ on 2025/04/10
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {

    T get() throws Exception;
}
