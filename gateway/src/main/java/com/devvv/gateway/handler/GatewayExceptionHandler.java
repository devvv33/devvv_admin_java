package com.devvv.gateway.handler;

import com.devvv.commons.common.response.ErrorCode;
import com.devvv.commons.common.response.IResult;
import com.devvv.commons.common.response.Result;
import com.devvv.gateway.utils.WebFluxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关统一异常处理
 */
@Order(-1)
@Configuration
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 自定义异常，直接写出为json
        if (ex instanceof IResult) {
            return WebFluxUtil.writeJson(response, ex);
        }

        // 其他异常，打印日志
        log.warn("[Gateway] {} 异常:{}", exchange.getRequest().getPath(), ex.getMessage());
        if (ex instanceof NotFoundException) {
            return Mono.error(ex);
        }
        if (ex instanceof ResponseStatusException) {
            return Mono.error(ex);
        }

        Result<Object> result = Result.build(ErrorCode.ERR.getCode(), "内部服务器错误");
        return WebFluxUtil.writeJson(response, result);
    }

}