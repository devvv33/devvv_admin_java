package com.devvv.commons.support.web.config;

import com.devvv.commons.support.web.converter.MyHttpMessageConverter;
import com.devvv.commons.support.web.filter.ApiFilter;
import com.devvv.commons.support.web.filter.InnerFilter;
import com.devvv.commons.support.web.filter.RootFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.yaml.MappingJackson2YamlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Create by WangSJ on 2024/01/16
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册接口Filter
     */
    @Bean
    public FilterRegistrationBean<RootFilter> rootFilter() {
        FilterRegistrationBean<RootFilter> registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new RootFilter());
        registrationBean.setUrlPatterns(RootFilter.URL_PATTERN);
        registrationBean.setOrder(-999);
        return registrationBean;
    }
    @Bean
    public FilterRegistrationBean<ApiFilter> apiFilter() {
        FilterRegistrationBean<ApiFilter> registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new ApiFilter());
        registrationBean.setUrlPatterns(ApiFilter.URL_PATTERN);
        registrationBean.setOrder(-1);
        return registrationBean;
    }
    @Bean
    public FilterRegistrationBean<InnerFilter> innerFilter() {
        FilterRegistrationBean<InnerFilter> registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new InnerFilter());
        registrationBean.setUrlPatterns(InnerFilter.URL_PATTERN);
        registrationBean.setOrder(-1);
        return registrationBean;
    }

    /**
     * 注册拦截器
     */
    // @Override
    // public void addInterceptors(InterceptorRegistry registry) {
    //     registry.addInterceptor(new ApiInterceptor())
    //             .addPathPatterns("/**");
    // }

    /**
     * 使用fastJson 做消息转化器
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        converters.removeIf(converter -> converter instanceof MappingJackson2YamlHttpMessageConverter);
        converters.add(new MyHttpMessageConverter());
    }

}
