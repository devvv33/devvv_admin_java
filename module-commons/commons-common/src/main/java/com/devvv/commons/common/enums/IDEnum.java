package com.devvv.commons.common.enums;

import java.util.Objects;

/**
 * Create by WangSJ on 2023/07/05
 */
public interface IDEnum {

    String getId();

    String getDesc();

    /**
     * 根据ID获取枚举类型，匹配的是自定义的ID值
     *
     * @param id    枚举自定义的ID数字
     * @param clazz 实现了IDEnum接口的Class枚举类
     * @param <T>   必须实现 IDEnum 接口
     * @return 如果没有找到，则返回null
     */
    public static <T extends IDEnum> T byId(String id, Class<T> clazz) {
        if (id == null) {
            return null;
        }
        T[] enums = clazz.getEnumConstants();
        for (T en : enums) {
            if (Objects.equals(en.getId(), id)) {
                return en;
            }
        }
        return null;
    }
}
