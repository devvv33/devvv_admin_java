package com.devvv.cms.controller.sys;

import cn.hutool.core.util.StrUtil;
import com.devvv.cms.models.bo.Option;
import com.devvv.cms.models.form.sys.QueryOptionsForm;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Create by WangSJ on 2025/12/26
 */
@Slf4j
@Tag(name = "112-选择器")
@RestController
@RequestMapping("/cmsApi/sys/selector")
public class SelectorController {

    private static final Map<String, List<Option>> enumOptionsCache = new ConcurrentHashMap<>();

    @Operation(summary = "1-根据枚举类名，查询下拉框")
    @PostMapping("/listOptionsByEnum")
    public Result<List<Option>> listOptionsByEnum(@Valid @RequestBody QueryOptionsForm form) {
        // 1. 先查本地缓存
        List<Option> cached = enumOptionsCache.get(form.getClassName());
        if (cached != null) {
            return Result.success(cached);
        }

        List<Option> list = new ArrayList<>();
        try {
            Class<?> clazz = Class.forName(form.getClassName());
            if (!clazz.isEnum()) {
                throw new BusiException("参数不是枚举类型:" + form.getClassName(), "非法请求");
            }

            // 需要尝试的字段名按优先级排序
            List<String> candidateFieldNames = Arrays.asList("desc", "id", "name");
            // 预先扫描可用的 getter 或字段，避免每次循环都反射查找
            List<Function<Object,Object>> accessors = buildAccessors(clazz, candidateFieldNames);

            for (Object item : clazz.getEnumConstants()) {
                String value = ((Enum<?>) item).name();
                String lable = accessors.stream()
                        .map(accessor -> accessor.apply(item))
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .findFirst()
                        .orElse(value);

                list.add(Option.builder()
                        .value(value)
                        .label(lable)
                        .build());
            }

            // 2. 放入缓存（只在成功构建后缓存）
            enumOptionsCache.put(form.getClassName(), Collections.unmodifiableList(list));
        } catch (Exception e) {
            throw new BusiException("参数不是枚举类型:" + form.getClassName(), "非法请求");
        }
        return Result.success(list);
    }

    /**
     * 构建按优先级排列的字段访问器（先 getter，后 field）
     */
    private static List<Function<Object,Object>> buildAccessors(Class<?> clazz, List<String> fieldNames) {
        List<Function<Object,Object>> accessors = new ArrayList<>();
        for (String fieldName : fieldNames) {
            // 1. 先找 getter，如 getDesc()/getId()/getName()
            String getterName = "get" + StrUtil.upperFirst(fieldName);
            try {
                Method getter = clazz.getMethod(getterName);
                getter.setAccessible(true);
                accessors.add(obj -> {
                    try {
                        return getter.invoke(obj);
                    } catch (Exception e) {
                        log.warn("反射获取字段失败！", e);
                        return null;
                    }
                });
                // 找到了 getter 就不再找同名字段（通常 getter 封装了真实逻辑）
                continue;
            } catch (NoSuchMethodException ignored) {
            }

            // 2. 再找字段 desc/id/name
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                accessors.add(obj -> {
                    try {
                        return field.get(obj);
                    } catch (Exception e) {
                        log.warn("反射获取字段失败！", e);
                        return null;
                    }
                });
            } catch (NoSuchFieldException ignored) {
            }
        }
        return accessors;
    }
}
