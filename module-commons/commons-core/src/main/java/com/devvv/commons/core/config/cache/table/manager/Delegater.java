package com.devvv.commons.core.config.cache.table.manager;

import com.devvv.commons.core.config.cache.table.exception.DataProxyException;

/**
 * Create by WangSJ on 2023/08/07
 *
 * 实现该接口完成数据持久化操作
 */
public interface Delegater<T> {

    T execute()throws DataProxyException;
}
