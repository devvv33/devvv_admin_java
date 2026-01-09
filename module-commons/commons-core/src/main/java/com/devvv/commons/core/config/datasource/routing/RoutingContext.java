package com.devvv.commons.core.config.datasource.routing;

import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Create by WangSJ on 2023/07/03
 */
@Data
@AllArgsConstructor
public class RoutingContext {

    private Class<?> mapperClass;
    private Table tableAnnotation;

    private DBType dbType;
    private String tableName;
}
