package com.devvv.commons.support.job.aop;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.devvv.commons.core.config.ApplicationInfo;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextHolder;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.sls.SLSLoggerUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Create by WangSJ on 2022/12/08
 */
@Slf4j
@Aspect
@Component
public class XxlJobAspect {

    /**
     * 切面
     * 如果定时任务执行异常，打印日志并报警
     */
    @Around(value = "@annotation(xxlJob)")
    public Object doAspect(ProceedingJoinPoint point, XxlJob xxlJob) throws Throwable {
        long startTime = System.currentTimeMillis();
        String handler = xxlJob.value();
        Object result = null;
        String error = null;
        try {
            // traceId
            Object serverFlag = ApplicationInfo.CURRENT_APP_TYPE == null ? "_" : ApplicationInfo.CURRENT_APP_TYPE.ordinal();
            String traceId = serverFlag + RandomUtil.randomString(5);
            MDC.put("traceId", traceId);
            BusiContext context = new BusiContext();
            context.setTraceId(traceId);
            context.setRequestTime(new Date());
            context.setRequestURI(handler);
            BusiContextHolder.setContext(context);

            // 记录调度日志
            startLog();

            // 执行原方法
            result = point.proceed();

            // 执行时间超过10分钟，打印日志记录
            long stepTime = System.currentTimeMillis() - startTime;
            if (stepTime > 10 * 60 * 1000) {
                log.warn("[定时任务]- 执行完毕，执行时间过长! Handler:{} 共耗时:{}", handler, DateUtil.formatBetween(stepTime));
            }
        } catch (Exception e) {
            // 如果抛出异常，打印日志并报警
            log.error("[定时任务]- 执行异常！ Handler:{} 共耗时:{}", handler, DateUtil.formatBetween(System.currentTimeMillis() - startTime), e);
            error = e.getMessage();
            // 大群提醒
            // String errorMsg = CommonUtil.getSimpleErrorMsg(e);
            // DaQunUtil.sendAsyncMessage(MsgUtils.format("【定时任务执行异常】 {}\n{}", handler, errorMsg), DaQunUtil.Group.Error);
        } finally {
            endLog(result, error);
            BusiContextHolder.releaseContext();
        }
        return result;
    }

    /**
     * 记录调度日志
     */
    private void startLog() {
        int shardIndex = XxlJobContext.getXxlJobContext().getShardIndex();
        int shardTotal = XxlJobContext.getXxlJobContext().getShardTotal();
        // 发送日志写到数据库
        Map<String, Object> map = new HashMap<>();
        map.put("Handler", BusiContextUtil.getContext().getRequestURI());
        map.put("traceId", BusiContextUtil.getContext().getTraceId());
        map.put("Action", "Start");
        map.put("Shard", shardIndex + "  " + (shardIndex + 1) + "/" + shardTotal);

        SLSLoggerUtil.sendBusiLog("Scheduling", map);
    }

    private void endLog(Object result, String errorMsg) {
        int shardIndex = XxlJobContext.getXxlJobContext().getShardIndex();
        int shardTotal = XxlJobContext.getXxlJobContext().getShardTotal();
        // 发送日志写到数据库
        Map<String, Object> map = new HashMap<>();
        map.put("Handler", BusiContextUtil.getContext().getRequestURI());
        map.put("traceId", BusiContextUtil.getContext().getTraceId());
        map.put("Action", "End");
        map.put("Shard", shardIndex + "  " + (shardIndex + 1) + "/" + shardTotal);
        map.put("Result", result instanceof ReturnT ? ((ReturnT<?>) result).getCode() : result);
        map.put("ErrorMsg", errorMsg);
        map.put("execTime", DateUtil.formatBetween(System.currentTimeMillis() - BusiContextUtil.getContext().getRequestTime().getTime()));
        SLSLoggerUtil.sendBusiLog("Scheduling", map);
    }
}
