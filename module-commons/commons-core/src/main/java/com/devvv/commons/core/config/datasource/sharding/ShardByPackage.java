package com.devvv.commons.core.config.datasource.sharding;

import com.devvv.commons.common.enums.PackageType;
import org.springframework.stereotype.Component;

/**
 * Create by WangSJ on 2023/07/03
 */
@Component("ShardByPackage")
public class ShardByPackage implements ITableShardStrategy{
    @Override
    public String getNewTableName(String tableName) {
        // TODO：暂时写死为转遇
        PackageType packageType = PackageType.BF;
        return tableName + "_" + packageType.name();
    }
}
