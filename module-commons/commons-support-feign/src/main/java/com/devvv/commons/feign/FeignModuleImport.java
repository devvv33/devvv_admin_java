package com.devvv.commons.feign;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Create by WangSJ on 2024/06/25
 */
@ComponentScan("com.devvv.commons.feign")
@EnableFeignClients(basePackages = "com.devvv.**.api")
public class FeignModuleImport {
}
