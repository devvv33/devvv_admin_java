package com.devvv.commons.core.config.datasource;

import com.alibaba.druid.support.jakarta.StatViewServlet;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;

/**
 * Create by WangSJ on 2024/07/02
 */
@Configuration
@ConditionalOnClass(Servlet.class)
public class DruidConfig {

    /**
     * 返回后台监控servlet
     */
    @Bean
    @Profile({"dev", "test"})
    public ServletRegistrationBean monitor() {
        ServletRegistrationBean<StatViewServlet> bean = new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        // 后台需要用户登录，账号密码配置
        HashMap<String, String> initParameters = new HashMap<>();
        // 增加配置
        // initParameters.put("loginUsername", "admin");
        // initParameters.put("loginPassword", "123456");
        // 访问白名单
        //        initParameters.put("allow","ip地址");
        bean.setInitParameters(initParameters);// 设置初始化参数
        return bean;
    }

    // @Bean
    // public FilterRegistrationBean webStatFilter(){
    //     FilterRegistrationBean<Filter> filterBean = new FilterRegistrationBean<>();
    //     filterBean.setFilter(new WebStatFilter());
    //     //可以过滤那些请求
    //     Map<String, String> initParameters = new HashMap<>();
    //     //不统计这些东西
    //     initParameters.put("exclusions","*.js,*.css,/druid/*");
    //     filterBean.setInitParameters(initParameters);
    //     return filterBean;
    // }
    //
}
