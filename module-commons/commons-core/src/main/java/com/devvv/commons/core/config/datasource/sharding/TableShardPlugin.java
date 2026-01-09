package com.devvv.commons.core.config.datasource.sharding;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.devvv.commons.core.config.datasource.annotation.Table;
import com.devvv.commons.core.config.datasource.routing.RoutingContext;
import com.devvv.commons.core.config.datasource.routing.RoutingContextHolder;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Properties;

/**
 * Create by WangSJ on 2023/07/03
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class TableShardPlugin implements Interceptor {

    private static final String DELEGATE_BOUND_SQL_SQL = "delegate.boundSql.sql";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        RoutingContext context = RoutingContextHolder.getRoutingContext();
        Table table;
        String shardBy;
        if (context == null || (table = context.getTableAnnotation()) == null || StrUtil.isBlank(shardBy = table.shardBy())) {
            return invocation.proceed();
        }

        // 获取新表名
        ITableShardStrategy strategy = SpringUtil.getBean(shardBy, ITableShardStrategy.class);
        String newTableName = strategy.getNewTableName(context.getTableName());

        // 抽取sql，替换表名
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        String sql = (String) metaObject.getValue(DELEGATE_BOUND_SQL_SQL);
        String newSql = sql.replaceAll(context.getTableName().toLowerCase(), newTableName.toLowerCase());
        metaObject.setValue(DELEGATE_BOUND_SQL_SQL, newSql);

        // 执行
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }
}
