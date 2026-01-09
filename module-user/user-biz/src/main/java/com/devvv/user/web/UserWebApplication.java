package com.devvv.user.web;

import com.devvv.commons.common.enums.app.AppType;
import com.devvv.commons.common.enums.app.ApplicationType;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 */
@EnableFileStorage
@SpringBootApplication
@MapperScan("com.devvv.**.dao.mapper")
@ApplicationType(AppType.UserWeb)
public class UserWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserWebApplication.class, args);
    }

}
