package com.devvv.commons.core.config.redis.key;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.core.config.redis.RedisKey;
import com.devvv.commons.core.config.redis.template.AbstractRedisTemplate;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Create by WangSJ on 2024/07/23
 */
@Slf4j
@Aspect
@Component
@Profile({"dev", "test"})
public class RedisKeyCheckAspect {

    /**
     * 检查 XXRedisTemplate是否按规范使用了 XXKeyDefine
     */
    @Around("execution(public * com.devvv.commons.core.config.redis.template.AbstractRedisTemplate.*(..))")
    public Object doCheck(ProceedingJoinPoint pj) throws Throwable {
        AbstractRedisTemplate template = (AbstractRedisTemplate) pj.getTarget();
        Class<? extends KeyDefine> supportKeyDefine = template.useRedisKeyDefinition();
        if (supportKeyDefine == null) {
            return pj.proceed();
        }
        Object[] args = pj.getArgs();
        if (args == null || args.length == 0) {
            return pj.proceed();
        }
        if (args[0] instanceof RedisKey redisKey && !ObjectUtil.equal(redisKey.getKeyDefine().getClass(), supportKeyDefine)) {
            BusiException e = new BusiException(StrUtil.format("{}不能被用作 {}，请规范使用", redisKey.getKeyDefine().getClass().getSimpleName(), template.getClass().getSimpleName()), "系统内部错误");
            log.error("RedisKey检查异常", e);
            throw e;
        }
        return pj.proceed();
    }
}
