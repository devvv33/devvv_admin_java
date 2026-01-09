package com.devvv.commons.common.response;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import com.devvv.commons.common.exception.BusiException;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Create by WangSJ on 2022/10/26
 * 重新定义接口返回格式
 */
@Data
public class Result<T> implements Serializable, IResult {
    private static final long serialVersionUID = 1L;

    /**
     * 响应代码
     */
    private Integer code;
    /**
     * 响应描述信息
     */
    private String msg;
    /**
     * 响应数据
     */
    private T data;


    // 默认构造
    private Result() {
    }

    /**
     * build 方法
     * 一般用来返回失败的数据
     */

    public static <T> Result<T> build(int code, String msg) {
        return Result.build(code, msg, null);
    }
    public static <T> Result<T> build(int code, String msg, T data) {
        Result<T> result = new Result<T>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
    public static <T> Result<T> build(IResult r) {
        Result<T> result = new Result<T>();
        result.setCode(r.getCode());
        result.setMsg(r.getMsg());
        return result;
    }


    /**
     * success 方法
     * 一般用来返回成功的数据
     */
    public static <T> Result<T> success() {
        return build(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg(), null);
    }

    public static <T> Result<T> success(T data) {
        return build(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg(), data);
    }


    /**
     * 判断返回结果是否成功
     */
    @JSONField(serialize = false)
    public boolean isSuccess() {
        return Objects.equals(code, ErrorCode.SUCCESS.getCode());
    }
    public String toJSONString() {
        return JSONObject.toJSONString(this);
    }

    /**
     * 结果处理-链式调用
     */
    public Result<T> onSuccess(Consumer<? super T> action) {
        if (isSuccess()) {
            action.accept(this.data);
        }
        return this;
    }

    public T ifFail(BiConsumer<Integer, String> action) {
        if (!isSuccess()) {
            action.accept(this.code, this.msg);
        }
        return this.data;
    }

    public T ifFailThrow(BiFunction<Integer, String, BusiException> action) {
        if (!isSuccess()) {
            throw action.apply(this.code, this.msg);
        }
        return this.data;
    }
    public T ifFailThrow(Supplier< BusiException> action) {
        if (!isSuccess()) {
            throw action.get();
        }
        return this.data;
    }
    public T ifFailThrow(String template, Object... args) {
        if (!isSuccess()) {
            throw new BusiException(this, StrUtil.format(template, args));
        }
        return this.data;
    }
    public T ifFailThrow() {
        if (!isSuccess()) {
            throw new BusiException(this);
        }
        return this.data;
    }
}