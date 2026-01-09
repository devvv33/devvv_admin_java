package com.devvv.gateway.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.devvv.gateway.utils.WebFluxUtil;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * 自定义限流异常处理
 */
public class SentinelFallbackHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        if (!BlockException.isBlockException(ex)) {
            return Mono.error(ex);
        }
        return WebFluxUtil.writeJson(response, "{\"code\":429,\"msg\":\"请求超过最大数，请稍后再试\"}");
    }
}
