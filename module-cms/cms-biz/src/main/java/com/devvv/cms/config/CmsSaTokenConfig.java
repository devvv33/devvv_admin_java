package com.devvv.cms.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import com.devvv.cms.manager.MenuManager;
import com.devvv.commons.core.context.ClientInfoUtil;
import com.devvv.commons.core.utils.RoleUtil;
import com.devvv.commons.token.utils.StpAdminUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Create by WangSJ on 2024/06/25
 */
@Configuration
public class CmsSaTokenConfig implements WebMvcConfigurer {

    // 注册 Sa-Token 的拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册路由拦截器，自定义认证规则
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 登录校验 -- 拦截所有路由，并排除/login 用于开放登录
            SaRouter.match("/cmsApi/**")
                    .check(r -> StpAdminUtil.checkLogin())
                    .check(r -> ClientInfoUtil.checkClient());

            // 对菜单中配置的接口，都添加权限校验
            if (!StpAdminUtil.hasRole(RoleUtil.ROLE_SUPER_ADMIN)) {
                String requestPath = SaHolder.getRequest().getRequestPath();
                if (MenuManager.getInstance().listAllPermission().contains(requestPath)) {
                    StpAdminUtil.checkPermission(requestPath);
                }
            }
        })).addPathPatterns("/**");
    }

}
