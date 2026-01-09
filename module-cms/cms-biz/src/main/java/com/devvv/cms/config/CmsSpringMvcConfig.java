package com.devvv.cms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * Create by WangSJ on 2024/06/27
 */
@Configuration
public class CmsSpringMvcConfig implements WebMvcConfigurer {


    /**
     * 静态资源访问：
     * 参考视频： https://www.bilibili.com/video/BV1Es4y1q7Bf?p=27&vd_source=17aed484c322026c2291e43d606033f9
     * 源码参看: {@link WebProperties.Resources}
     * 1、 访问 /webjars/**    ->      classpath:/META-INF/resources/webjars/**
     * 2、 访问 /**            ->      classpath:/META-INF/resources/static/**、classpath:/resources/**、classpath:/static/**、classpath:/public/**
     *
     * 静态资源默认都有缓存相关的设置 {@link WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter#addResourceHandler(ResourceHandlerRegistry, String, Consumer)}
     * 配置调整：
     *  spring.mvc.static-path-pattern=/static/**               # 只将static开头的请求认为是访问静态资源
     *  spring.web.resources.static-locations=classpath:/META-INF/resources/static/,classpath:/static/   # 暴漏本地目录，作为静态资源
     */

    @Value("${file.access_path:/cmsFile}")
    private String accessPath;
    @Value("${file.upload_dir:upload}")
    private String uploadDir;

    /**
     * 静态资源访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String externalUploadPath = Paths.get(uploadDir).toUri().toString();    // jar 同级目录
        // 这个路径的请求, 当做是静态资源访问, 会访问指定目录下的文件
        registry.addResourceHandler(accessPath + "/**")
                .addResourceLocations(externalUploadPath);
    }
}
