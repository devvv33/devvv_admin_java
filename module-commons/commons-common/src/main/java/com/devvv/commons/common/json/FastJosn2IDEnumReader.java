package com.devvv.commons.common.json;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.devvv.commons.common.enums.IDEnum;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Create by WangSJ on 2024/07/05
 */
public class FastJosn2IDEnumReader<E extends IDEnum> implements ObjectReader {

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        String str = jsonReader.readString();
        if (StrUtil.isBlank(str)) {
            return null;
        }
        // 尝试id匹配
        if (fieldType instanceof Class c && c.isEnum()) {
            for (Class<?> iface : c.getInterfaces()) {
                if (iface == IDEnum.class) {
                    try {
                        Method getId = iface.getMethod("getId");
                        Object[] enumValues = c.getEnumConstants();
                        for (Object emv : enumValues) {
                            // 根据id匹配
                            if (ObjectUtil.equal(getId.invoke(emv), str)) {
                                return emv;
                            }
                            // 根据name匹配
                            if (ObjectUtil.equal(emv.toString(), str)) {
                                return emv;
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        return null;
    }
}
