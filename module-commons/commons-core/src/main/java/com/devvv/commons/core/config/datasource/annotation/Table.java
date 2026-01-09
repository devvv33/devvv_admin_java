package com.devvv.commons.core.config.datasource.annotation;

import com.devvv.commons.common.enums.DBType;

import java.lang.annotation.*;

/**
 * Create by WangSJ on 2023/07/03
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Table {

    /**
     * 表名，大小写不敏感
     */
    String value() default "";

    /**
     * 数据库名
     */
    DBType DB() default DBType.sys;


    /**
     * 切分所使用的属性名称，暂无用途，保留元属性
     */
    String shardBy() default "";


    /**
     * 缓存相关
     */
    boolean useTableCache() default false;
    boolean cacheNullValue() default false;
    boolean useSelectAll() default false;
    int cacheExpire() default 24 * 3600;
    String[] primaryKey() default {"id"};
}
