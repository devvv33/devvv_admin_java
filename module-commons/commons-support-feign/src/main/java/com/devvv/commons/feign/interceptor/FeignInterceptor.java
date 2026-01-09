package com.devvv.commons.feign.interceptor;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Create by WangSJ on 2024/06/26
 */
@Component
public class FeignInterceptor implements RequestInterceptor {

    // 为 Feign 的 RCP调用 添加请求头Same-Token
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // SameToken 内部服务的权限校验
        // 要求所有内部服务都连接同一个SessionRedis
        // 原理参看: https://sa-token.cc/doc.html#/micro/same-token
        requestTemplate.header(SaSameUtil.SAME_TOKEN, SaSameUtil.getToken());

        // 登录Token 向后传递
        for (Map.Entry<String, StpLogic> entry : SaManager.stpLogicMap.entrySet()) {
            String tokenName = entry.getValue().getTokenName();
            String tokenValue = entry.getValue().getTokenValue();
            if (StrUtil.isNotBlank(tokenValue)) {
                requestTemplate.header(tokenName, tokenValue);
            }
        }

        // 当前请求的Context 向后传递
        Opt.ofNullable(BusiContextHolder.getContext())
                .map(BusiContext::toJsonStr)
                .map(Base64::encode)
                .ifPresent(s -> requestTemplate.header(BusiContextHolder.CONTEXT_KEY, Collections.singleton(s)));
    }
}
