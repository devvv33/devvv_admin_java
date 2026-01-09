package com.devvv.commons.core.context;

import cn.hutool.core.util.RandomUtil;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.common.response.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

/**
 * Create by WangSJ on 2024/06/26
 */
@Slf4j
public class BusiContextHolder {
    /**
     * 使用ThreadLocal来存储当前上下文
     */
    private static final ThreadLocal<BusiContext> CONTEXT = new ThreadLocal<>();
    public static final String CONTEXT_KEY = "__context__";

    public static void setContext(BusiContext context) {
        CONTEXT.remove();
        if (context == null) {
            return;
        }
        CONTEXT.set(context);
    }

    /**
     * 为避免ThreadLocal污染，必须手动维护Context的添加和释放
     *
     */
    public static @Nullable BusiContext getContext(){
        return CONTEXT.get();
    }

    public static void releaseContext(){
        CONTEXT.remove();
    }

    /**
     * 获取全局的请求id，一般用于分布式锁
     */
    public static long getOrSetGlobalRequestId() {
        BusiContext context = BusiContextHolder.getContext();
        if (context == null) {
            // 为避免ThreadLocal污染，必须手动维护BusiContext的添加和释放
            log.error("context上下文尚未初始化");
            throw new BusiException(ErrorCode.ERR);
        }
        Long globalRequestId = context.getGlobalRequestId();
        if (globalRequestId == null) {
            globalRequestId = RandomUtil.randomLong();
            context.setGlobalRequestId(globalRequestId);
        }
        return globalRequestId;
    }


    /**
     * 更新返回结果
     */
    public static void setResult(Object result) {
        if (result == null || getContext() == null) {
            return;
        }
        getContext().setResult(result);
    }

    public static void setRequestBody(String requestBody) {
        if (requestBody == null || getContext() == null) {
            return;
        }
        getContext().setRequestBody(requestBody);
    }
}
