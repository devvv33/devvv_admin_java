package com.devvv.commons.core.config.datasource.routing;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import com.devvv.commons.core.config.datasource.annotation.Table;
import org.springframework.stereotype.Component;

/**
 * Create by WangSJ on 2023/07/03
 */
@Slf4j
@Aspect
@Component
public class RoutingTableAspect {

    /**
     * 拦截所有Mapper，动态切换数据源
     */
    @Around(value = "@annotation(com.devvv.commons.core.config.datasource.annotation.Table) || @within(com.devvv.commons.core.config.datasource.annotation.Table)")
    public Object multiDatasource(ProceedingJoinPoint point) throws Throwable {
        Class<?> mapperClass = point.getSignature().getDeclaringType();
        Table table = mapperClass.getAnnotation(Table.class);
        if (table.DB() == null) {
            throw new Exception("数据库连接未定义！ " + mapperClass.getName());
        }

        try {
            // 切换连接
            RoutingContext context = new RoutingContext(mapperClass, table, table.DB(), table.value());
            RoutingContextHolder.setRoutingContext(context);
            // 执行原方法
            return point.proceed();
        } finally {
            RoutingContextHolder.clearRoutingContext();
        }
    }

}
