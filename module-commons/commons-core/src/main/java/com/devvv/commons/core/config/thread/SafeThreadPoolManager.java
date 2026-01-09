package com.devvv.commons.core.config.thread;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Create by WangSJ on 2025/07/11
 */
@Slf4j
@Component
public class SafeThreadPoolManager {

    // 准备一个线程池
    private static final ExecutorService safeThreadPool = Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
            .name("SafeVT-", 0)
            .uncaughtExceptionHandler((t, e) -> log.error("[异步线程池]-执行失败！", e))
            .factory());

    /**
     * SpringBoot 停机时, 优雅关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        System.out.println("SafeThreadPool-开始优雅关闭线程池...");
        SafeThreadPoolManager.safeThreadPool.shutdown();  // 停止接收新任务
        try {
            if (!SafeThreadPoolManager.safeThreadPool.awaitTermination(120, TimeUnit.SECONDS)) {
                System.err.println("SafeThreadPool-仍有任务未完成，强制关闭！");
                SafeThreadPoolManager.safeThreadPool.shutdownNow();  // 强制关闭
            } else {
                System.out.println("SafeThreadPool-线程池关闭完成");
            }
        } catch (InterruptedException e) {
            System.err.println("SafeThreadPool-线程池关闭报错!");
            e.printStackTrace();
            SafeThreadPoolManager.safeThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获取安全线程池
     */
    public static ExecutorService getSafePool() {
        return safeThreadPool;
    }
}
