package com.devvv.commons.core.config;

import cn.hutool.core.util.RandomUtil;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Date;

/**
 * Create by WangSJ on 2025/02/12
 * SpringTask 定时任务各任务并行执行配置类
 */
@Slf4j
@Configuration
@EnableScheduling
public class AsyncTaskConfig implements SchedulingConfigurer {

    //线程池线程数量
    private final int corePoolSize = 20;

    /**
     * 为定时任务增加线程池，用以并发执行
     *
     * 同一定时任务，将会串行化执行，
     * 也就是说，某一个定时任务执行时间过长，超过了第二次执行的时间点，
     *      那么在第二次应执行的时间点时，也不会再次触发此任务执行
     *      此任务会在本次执行后，下一个有效时间点，再次被出发执行
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler(){
            protected void beforeExecute(Thread thread, Runnable task) {
                // traceId
                Object serverFlag = ApplicationInfo.CURRENT_APP_TYPE == null ? "_" : ApplicationInfo.CURRENT_APP_TYPE.ordinal();
                String traceId = serverFlag + RandomUtil.randomString(5);
                MDC.put("traceId", traceId);
                BusiContext context = new BusiContext();
                context.setTraceId(traceId);
                context.setRequestTime(new Date());
                BusiContextHolder.setContext(context);

                super.beforeExecute(thread, task);
            }
            protected void afterExecute(Runnable task, @Nullable Throwable ex) {
                // 清理上下文
                BusiContextHolder.releaseContext();
                super.afterExecute(task, ex);
            }
        };

        scheduler.setPoolSize(corePoolSize);//线程池容量
        scheduler.setThreadNamePrefix("Task-");//线程名前缀
        // 未知异常处理
        scheduler.setErrorHandler(throwable -> {
            log.error("定时任务执行异常（未捕捉的异常）！", throwable);
        });
        scheduler.initialize();//初始化线程池
        scheduledTaskRegistrar.setTaskScheduler(scheduler);
    }
}
