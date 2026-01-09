package com.devvv.commons.common.utils;

import cn.hutool.core.util.StrUtil;
import com.devvv.commons.common.annotation.MaxLengthClip;

import java.lang.reflect.Field;

/**
 * Create by WangSJ on 2024/06/13
 */
public class MyStrUtil {


    /**
     * 截断字符串
     */
    public static String maxLength(String str, int maxLength){
        if (StrUtil.isBlank(str)) {
            return str;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 1) + "…";
    }


    /**
     * 对对象的所有String字段，调用trim方法，然后再设置回去
     * 配合 {@link MaxLengthClip}注解时，同时支持裁剪功能
     */
    public static void trimStringFields(Object entity) {
        // 获取实体类的所有字段
        Field[] fields = entity.getClass().getDeclaredFields();

        // 遍历所有字段
        for (Field field : fields) {
            // 检查字段是否为String类型
            if (field.getType().equals(String.class)) {
                // 设置访问权限（如果需要，因为默认可能不允许访问私有字段）
                field.setAccessible(true);

                try {
                    // 获取字段的当前值
                    String fieldValue = (String) field.get(entity);
                    // 如果字段值不是null，进行trim操作
                    if (fieldValue != null) {
                        // 1、进行trim操作
                        String val = fieldValue.trim();
                        // 2、进行字符串截断
                        MaxLengthClip ml = field.getAnnotation(MaxLengthClip.class);
                        if (ml != null) {
                            val = maxLength(val, ml.value());
                        }
                        // 更新字段值
                        field.set(entity, val);
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error accessing field " + field.getName(), e);
                }
            }
        }
    }
}
