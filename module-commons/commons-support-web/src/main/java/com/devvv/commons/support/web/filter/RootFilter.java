package com.devvv.commons.support.web.filter;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.devvv.commons.common.response.ErrorCode;
import com.devvv.commons.common.response.IResult;
import com.devvv.commons.common.response.Result;
import com.devvv.commons.core.config.ApplicationInfo;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextHolder;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.sls.SLSLoggerUtil;
import com.devvv.commons.support.web.utils.WebUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Create by WangSJ on 2024/01/15
 */
@Slf4j
public class RootFilter implements Filter {

    /**
     * 拦截的URL
     */
    public static final List<String> URL_PATTERN = Lists.newArrayList("/*");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            initBusiContext(request, response);
            // 执行业务
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 异常处理
            Result result = handlerException(e);
            BusiContextHolder.setResult(result);
            WebUtil.sendJsonMessage(response, result);
        } finally {
            printLog();
            // 清空数据
            BusiContextHolder.releaseContext();
        }
    }

    /**
     * 初始化上下文
     * 这里只封装基础数据，鉴权校验等逻辑请参考：
     * @see com.devvv.commons.token.config.SaTokenConfiguration#addInterceptors(InterceptorRegistry)
     * @see com.devvv.commons.token.interceptor.ParseTokenUserInterceptor#preHandle(HttpServletRequest, HttpServletResponse, Object)
     */
    private void initBusiContext(HttpServletRequest request, HttpServletResponse response) {
        // 解析请求数据
        BusiContext context = new BusiContext();
        context.setRequest(request);
        context.setResponse(response);
        BusiContextHolder.setContext(context);

        // traceId
        Object serverFlag = ApplicationInfo.CURRENT_APP_TYPE == null ? "_" : ApplicationInfo.CURRENT_APP_TYPE.ordinal();
        String traceId = serverFlag + RandomUtil.randomString(5);
        MDC.put("traceId", traceId);
        context.setTraceId(traceId);
        context.setRequestURL(request.getRequestURL().toString());
        context.setRequestURI(request.getRequestURI());
        context.setRequestTime(new Date());
        context.setClientIp(WebUtil.getClientIp(request));
        context.setUserAgent(request.getHeader("User-Agent"));
        context.setRequestQuery(request.getQueryString());
        context.setRequestBody(null);   // 初始化时，要将参数覆盖掉，防止有脏数据
        context.setResult(null);
    }

    /**
     * 异常结构处理
     */
    private Result handlerException(Exception e) {
        logSimpleMsg(ExceptionUtil.getRootCause(e));
        Throwable tmp = e;
        do {
            if (tmp instanceof IResult r) {
                return Result.build(r.getCode(), r.getMsg(), r.getData());
            } else if (tmp instanceof IllegalArgumentException r) {
                return Result.build(ErrorCode.LOGIC_ERROR.getCode(), r.getMessage());
            } else if (tmp instanceof ValidateException r) {
                return Result.build(ErrorCode.LOGIC_ERROR.getCode(), r.getMessage());
            }
            tmp = tmp.getCause();
        } while (tmp != null);  // 循环处理嵌套异常

        log.error("全局异常拦截-未知异常: {}", e.getMessage(), e);
        return Result.build(ErrorCode.ERR.getCode(), ErrorCode.ERR.getMsg());
    }

    // 打印简单日志
    private void logSimpleMsg(Throwable e) {
        StringBuilder errorMsg = new StringBuilder();
        if (e.getStackTrace() != null) {
            Arrays.stream(e.getStackTrace())
                    .filter(bean -> bean != null && bean.toString().startsWith("com.devvv."))
                    .map(bean-> StrUtil.format("({}:{}): ",bean.getFileName(),bean.getLineNumber()))
                    .findFirst()
                    .ifPresent(errorMsg::append);
        }

        if (e.getMessage() != null) {
            Arrays.stream(e.getMessage().split("\n"))
                    .filter(StrUtil::isNotBlank)
                    .findFirst()
                    .ifPresent(errorMsg::append);
        } else {
            errorMsg.append(e.getClass().getSimpleName());
        }
        BusiContextUtil.getContext().setError(errorMsg.toString());
        log.debug("--{}", errorMsg);
    }

    /**
     * 打印日志
     */
    private void printLog() {
        // 静态资源，不打印日志
        if (ReUtil.isMatch(".*\\.(html|css|js|jpg|png|gif|pdf)$", BusiContextUtil.getContext().getRequestURI())) {
            return;
        }
        if (ApplicationInfo.isProdEnv()) {
            SLSLoggerUtil.sendApiLog();
        } else {
            log.info("请求返回:{}", SLSLoggerUtil.buildApiInfoLogMsg());
        }
    }


    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
