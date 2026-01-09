package com.devvv.commons.core.config;

import com.alibaba.cloud.nacos.refresh.NacosContextRefresher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Create by WangSJ on 2024/06/20
 */
@Slf4j
@Component
public class AppConfigRefreshEventListener implements ApplicationListener<EnvironmentChangeEvent> {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 当Nacos配置文件发生变化时，会触发此方法
     *
     * @see NacosContextRefresher#registerNacosListener(String, String)
     */
    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        // 获取 Environment 对象
        Environment environment = applicationContext.getEnvironment();
        // 遍历变更的配置项
        event.getKeys().forEach(key -> {
            // log.info("配置变更了! {} -> {}", key, environment.getProperty(key));
        });
    }
}
