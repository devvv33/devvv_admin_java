package com.devvv.commons.core.config.datasource.routing;

/**
 * Create by WangSJ on 2023/07/03
 */
public class RoutingContextHolder {

    private static final ThreadLocal<RoutingContext> contextHolder = new ThreadLocal<RoutingContext>();

    public static void setRoutingContext(RoutingContext context) {
        contextHolder.set(context);
    }

    public static RoutingContext getRoutingContext() {
        return contextHolder.get();
    }

    public static void clearRoutingContext() {
        contextHolder.remove();
    }
}
