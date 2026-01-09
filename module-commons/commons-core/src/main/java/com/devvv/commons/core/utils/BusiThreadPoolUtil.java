package com.devvv.commons.core.utils;

import com.devvv.commons.core.config.thread.SafeThreadPoolManager;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Create by WangSJ on 2024/01/24
 * 自定义线程池
 */
@Slf4j
public class BusiThreadPoolUtil {

    /**
     * 默认线程池, 停机时会立即关闭, 不会等待池中任务执行完毕
     */
    public static final ExecutorService DEFAULT_VIRTUAL_THREAD = Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
            .name("DefVT-", 0)
            .uncaughtExceptionHandler((t, e) -> log.error("[异步线程池]-执行失败！", e))
            .factory());


    /**
     * 提交一个任务到安全的线程池中执行
     * 停机时会等待池中任务执行完毕
     */
    public static void executeSafePool(Runnable runnable) {
        SafeThreadPoolManager.getSafePool().execute(wrapped(runnable));
    }
    public static <T> Future<T> executeSafePool(Callable<T> callable) {
       return SafeThreadPoolManager.getSafePool().submit(wrapped(callable));
    }


    /**
     * 提交一个任务到默认线程池中执行
     */
    public static void executeDefaultPool(Runnable runnable) {
        DEFAULT_VIRTUAL_THREAD.execute(wrapped(runnable));
    }
    public static <T> Future<T> executeDefaultPool(Callable<T> callable) {
        return DEFAULT_VIRTUAL_THREAD.submit(wrapped(callable));
    }

    private static Runnable wrapped(Runnable runnable) {
        BusiContext ctx = BusiContextHolder.getContext();
        return () -> {
            // 传递上下文
            if (ctx != null) {
                BusiContextHolder.setContext(ctx);
                MDC.put("traceId", ctx.getTraceId());
            }
            // 执行任务并清理
            try {
                runnable.run();
            } finally {
                BusiContextHolder.releaseContext();
            }
        };
    }
    private static <T> Callable<T> wrapped(Callable<T> callable) {
        BusiContext ctx = BusiContextHolder.getContext();
        return () -> {
            // 传递上下文
            if (ctx != null) {
                BusiContextHolder.setContext(ctx);
                MDC.put("traceId", ctx.getTraceId());
            }
            // 执行任务并清理
            try {
                return callable.call();
            } finally {
                BusiContextHolder.releaseContext();
            }
        };
    }

}
