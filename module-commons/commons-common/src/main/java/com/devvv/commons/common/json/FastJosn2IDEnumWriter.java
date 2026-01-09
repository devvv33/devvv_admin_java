package com.devvv.commons.common.json;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.devvv.commons.common.enums.IDEnum;

import java.lang.reflect.*;

/**
 * Create by WangSJ on 2024/07/05
 */
public class FastJosn2IDEnumWriter<E extends IDEnum> implements ObjectWriter {
    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        if (object instanceof IDEnum idEnum) {
            jsonWriter.writeString(idEnum.getId());
        } else {
            jsonWriter.writeString(object.toString());
        }
    }
}
