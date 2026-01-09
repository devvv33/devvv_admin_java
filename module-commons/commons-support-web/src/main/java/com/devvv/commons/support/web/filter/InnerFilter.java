package com.devvv.commons.support.web.filter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextHolder;
import com.devvv.commons.core.context.BusiContextUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.List;

/**
 * Create by WangSJ on 2024/01/15
 */
@Slf4j
public class InnerFilter implements Filter {

    /**
     * 拦截的URL
     */
    public static final List<String> URL_PATTERN = Lists.newArrayList("/inner/*");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        processInnerContext(request);
        // 执行业务
        filterChain.doFilter(request, response);
    }

    /**
     * 补充处理内部调用的上下文
     */
    private static void processInnerContext(HttpServletRequest request) {
        // 本次请求自己的数据
        BusiContext context = BusiContextUtil.getContext();

        // 上游服务从header传过来的Context
        String contextBase64 = request.getHeader(BusiContextHolder.CONTEXT_KEY);
        if (StrUtil.isNotBlank(contextBase64)) {
            try {
                String contextJsonStr = Base64.decodeStr(contextBase64);

                // 使用上游服务的Context， 本次请求的基本信息使用自己的
                BusiContext remoteContext = JSONObject.parseObject(contextJsonStr, BusiContext.class);
                remoteContext.setTraceId(remoteContext.getTraceId() + ":" + context.getTraceId());
                remoteContext.setRequestURL(context.getRequestURL());
                remoteContext.setRequestURI(context.getRequestURI());
                remoteContext.setRequestTime(context.getRequestTime());
                remoteContext.setClientIp(context.getClientIp());
                remoteContext.setUserAgent(context.getUserAgent());
                remoteContext.setRequestQuery(context.getRequestQuery());
                remoteContext.setRequestBody(context.getRequestBody());
                remoteContext.setResult(context.getResult());
                context = remoteContext;
                BusiContextHolder.setContext(context);
            } catch (Exception e) {
                log.warn("解析上游服务上下文失败", e);
            }
        }
        MDC.put("traceId", context.getTraceId());
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
