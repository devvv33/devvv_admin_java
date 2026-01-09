package com.devvv.commons.core.busicode;

import cn.hutool.core.lang.Opt;
import com.devvv.commons.core.context.BusiContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Create by WangSJ on 2024/07/01
 */
@Slf4j
@Aspect
@Component
@Order(0)
public class BusiCodeAspect {

    /**
     * 如果方法上有 @BusiCode 注解，则将其添加到上下文
     */
    @Before("@annotation(busiCode)")
    public void beforeMethodWithAnnotation(BusiCode busiCode) {
        BusiCodeDefine code = busiCode.value();
        Opt.ofNullable(BusiContextHolder.getContext()).ifPresent(context -> context.setBusiCode(code));
    }

}
