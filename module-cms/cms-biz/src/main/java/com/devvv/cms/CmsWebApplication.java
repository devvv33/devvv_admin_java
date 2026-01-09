package com.devvv.cms;

import com.devvv.commons.common.enums.app.AppType;
import com.devvv.commons.common.enums.app.ApplicationType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Create by WangSJ on 2023/09/27
 */
@SpringBootApplication
@MapperScan(basePackages = "com.devvv.cms.**.mapper")
@ApplicationType(AppType.CmsWeb)
public class CmsWebApplication {

    /**
     * 支持jar包
     */
    public static void main(String[] args) {
        SpringApplication.run(CmsWebApplication.class, args);
    }

}
