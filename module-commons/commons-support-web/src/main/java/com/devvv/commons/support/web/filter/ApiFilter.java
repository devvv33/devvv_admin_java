package com.devvv.commons.support.web.filter;

import com.google.common.collect.Lists;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.context.ClientInfoUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * Create by WangSJ on 2024/01/15
 */
@Slf4j
public class ApiFilter implements Filter {

    /**
     * 拦截的URL
     */
    public static final List<String> URL_PATTERN = Lists.newArrayList("/api/*", "/cmsApi/*");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        processApiContext(request);
        // 执行业务
        filterChain.doFilter(request, response);
    }

    /**
     * 补充处理外部调用的上下文
     */
    private static void processApiContext(HttpServletRequest request) {
        BusiContext context = BusiContextUtil.getContext();
        // 补充客户端相关信息
        context.setClientInfo(ClientInfoUtil.parseByHeader(request));
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
