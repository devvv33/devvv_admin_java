package com.devvv.gateway.utils;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Create by WangSJ on 2024/06/28
 */
public class WebFluxUtil {

    /**
     * 异步写出json
     */
    public static Mono<Void> writeJson(ServerHttpResponse response, Object obj) {
        String jsonString = JSONObject.toJSONString(obj);
        return writeJson(response, jsonString);
    }
    public static Mono<Void> writeJson(ServerHttpResponse response, String msg) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        DataBuffer buffer = response.bufferFactory().wrap(msg.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
