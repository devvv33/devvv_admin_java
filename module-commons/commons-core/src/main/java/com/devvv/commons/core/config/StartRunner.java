package com.devvv.commons.core.config;

import cn.hutool.core.util.StrUtil;
import com.devvv.commons.common.utils.CommonUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


/**
 * Create by WangSJ on 2023/04/12
 */
@Slf4j
@Component
public class StartRunner implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {
        log.warn("[{}] 服务启动成功 ...  {}:{}  {}", ApplicationInfo.CURRENT_APP_TYPE, ApplicationInfo.SERVER_IP, ApplicationInfo.SERVER_PORT, CommonUtil.getRuntimeMsg());
    }

    @PreDestroy
    public void destory(){
        System.out.println(StrUtil.format("[{}] 服务关闭 {}:{}", ApplicationInfo.CURRENT_APP_TYPE, ApplicationInfo.SERVER_IP, ApplicationInfo.SERVER_PORT));
        log.warn("[{}] 服务关闭中！ {}:{}", ApplicationInfo.CURRENT_APP_TYPE, ApplicationInfo.SERVER_IP, ApplicationInfo.SERVER_PORT);
    }
}
