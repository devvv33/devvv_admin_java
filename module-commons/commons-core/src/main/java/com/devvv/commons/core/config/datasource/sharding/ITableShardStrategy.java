package com.devvv.commons.core.config.datasource.sharding;

/**
 * Create by WangSJ on 2023/07/03
 * 分表策略
 */
public interface ITableShardStrategy {

    /** 实现此方法，返回一个新的表名 */
    String getNewTableName(String tableName);

}
