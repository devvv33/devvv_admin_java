package com.devvv.commons.core.config.cache.table.config;

import java.lang.reflect.Type;

/**
 * Create by WangSJ on 2023/08/08
 *
 * 缓存类型转换器，因为缓存的Map仅支持字符串
 */
public interface Serializer {

    /**
     * 将对象序列化为字符串存入缓存服务器
     * @param object
     * @return
     */
    String seriazileAsString(Object object);

    /**
     * 将字符串反序列化为对象
     * @return
     */
    <T> T deserializeAsObject(String jsonString, Type type);

}
