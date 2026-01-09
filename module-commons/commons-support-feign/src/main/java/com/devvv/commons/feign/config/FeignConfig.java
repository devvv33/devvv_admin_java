package com.devvv.commons.feign.config;

import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Create by WangSJ on 2024/06/26
 */
@Configuration
public class FeignConfig {

    private static final HttpMessageConverters HTTP_MESSAGE_CONVERTERS = new HttpMessageConverters(new FastJson2FeignMessageConverter());

    /**
     * 使用FastJson2作为Feign的消息转换器
     */
    @Bean
    public Decoder feignDecoder() {
        return new SpringDecoder(() -> HTTP_MESSAGE_CONVERTERS);
    }

    @Bean
    public Encoder feignEncoder() {
        return new SpringEncoder(() -> HTTP_MESSAGE_CONVERTERS);
    }
}
