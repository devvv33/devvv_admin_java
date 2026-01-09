package com.devvv.commons.token.interceptor;

import cn.dev33.satoken.session.SaSession;
import cn.hutool.core.lang.Opt;
import com.google.common.collect.Lists;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextHolder;
import com.devvv.commons.core.session.AdminSessionInfo;
import com.devvv.commons.core.session.UserSessionInfo;
import com.devvv.commons.token.utils.StpAdminUtil;
import com.devvv.commons.token.utils.StpUserUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Create by WangSJ on 2024/06/28
 */
@Order(10)
public class ParseTokenUserInterceptor implements HandlerInterceptor {

    /**
     * 拦截的URL
     */
    public static final List<String> URL_PATTERN = Lists.newArrayList("/api/**", "/cmsApi/**");

    /**
     * 在进入Controller方法之前执行
     *
     * 解析当前登录的用户，放入到Context中
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        BusiContext context = BusiContextHolder.getContext();
        // 解析user用户
        Opt.ofNullable(StpUserUtil.getLoginIdDefaultNull())                                 // 1、根据请求头的Token获取用户id
                .map(id-> StpUserUtil.getSessionByLoginId(id, false))       // 2、根据用户id查询用户级session
                .map(s -> s.getModel(SaSession.USER, UserSessionInfo.class))
                .peek(s -> {
                    context.setUserId(s.getUserId());
                    context.setUserSessionCopy(s);
                });

        // 解析admin用户
        Opt.ofNullable(StpAdminUtil.getLoginIdDefaultNull())
                .map(id-> StpAdminUtil.getSessionByLoginId(id, false))
                .map(s -> s.getModel(SaSession.USER, AdminSessionInfo.class))
                .peek(s -> {
                    context.setAdminId(s.getAdminId());
                    context.setAdminSessionCopy(s);
                });
        return true;
    }

    // 在处理完请求后，但视图渲染之前调用
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    // 请求结束之后被调用，即在视图渲染完成后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
