package com.devvv.user.web.config;

import com.devvv.commons.core.config.ApplicationInfo;
import com.devvv.commons.token.utils.StpUserUtil;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Create by WangSJ on 2024/07/03
 */
@Configuration
public class UserSwaggerConfig {

    /**
     * 接口分组
     */
    @Bean
    @Profile({"dev", "test"})
    public GroupedOpenApi createApi() {
        return GroupedOpenApi.builder()
                .group(ApplicationInfo.CURRENT_APP_TYPE.getId())
                .displayName("2-用户模块")
                .pathsToMatch("/api/**")
                .packagesToScan("com.devvv.user.web.controller")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    // 设置默认的 请求头信息值
                    operation.setParameters(List.of(
                            new Parameter().in(ParameterIn.HEADER.toString()).schema(new StringSchema()).name(StpUserUtil.getTokenName()).required(false), // 这里设置为false，仅表示在swagger中无需手动传递（因为有cookie），在app中是必须要传递的
                            new Parameter().in(ParameterIn.HEADER.toString()).schema(new StringSchema()).name("x-arg").required(false),
                            new Parameter().in(ParameterIn.HEADER.toString()).schema(new StringSchema()).name("x-inf").example("p=BF&i=WB&v=1.0.0&t=1640000000&n=_ignore&c=OSS&os=IOS&ov=14.7").required(true)
                    ));
                    return operation;
                })
                .build();
    }

}
