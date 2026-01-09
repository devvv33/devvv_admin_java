package com.devvv.commons.common.annotation;


import com.devvv.commons.common.utils.MyStrUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * Create by WangSJ on 2024/06/13
 *
 * 用来修饰字段长度
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface MaxLengthClip {

    /**
     * 最大字符串长度
     * 配合{@link MyStrUtil#trimStringFields}方法，如果字符串长度过长，则截取字符串，截取后的字符串长度为value
     */
    int value();
}