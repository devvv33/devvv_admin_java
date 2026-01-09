package com.devvv.commons.core.config.cache.table.config;

import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Type;

/**
 * Create by WangSJ on 2023/08/08
 */
public class FastJsonSerializable implements Serializer {

    @Override
    public String seriazileAsString(Object object) {
        if (object == null) {
            return "";
        }
        return JSONObject.toJSONString(object);
    }

    @Override
    public <T> T deserializeAsObject(String jsonString, Type type) {
        if (jsonString == null || type == null) {
            return null;
        }
        return JSONObject.parseObject(jsonString, type);
    }
}
