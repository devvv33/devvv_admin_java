package com.devvv.commons.core.config;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.devvv.commons.common.enums.app.AppType;
import com.devvv.commons.common.enums.app.ApplicationType;
import com.devvv.commons.common.enums.type.Env;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.system.ApplicationHome;

import java.io.File;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Create by WangSJ on 2023/07/03
 */
@Slf4j
public class ApplicationInfo {

    /** 当前环境类型 */
    public static final Env ENV;
    /** 当前服务端口 */
    public static final Integer SERVER_PORT;
    /** 当前服务名称 */
    public static final AppType CURRENT_APP_TYPE;
    /** 本机IP */
    public static final String SERVER_IP;
    /** 当前jar包所在目录 */
    public static final String BASE_PATH;
    /** 文件地址 */
    public static String FILE_URL;

    static {
        CURRENT_APP_TYPE = Arrays.stream(Thread.currentThread().getStackTrace())
                .filter(s -> "main".equals(s.getMethodName()))
                .map(s -> ClassUtil.loadClass(s.getClassName()))
                .findAny()
                .map(c -> c.getAnnotation(ApplicationType.class))
                .map(ApplicationType::value)
                .orElse(EnumUtil.getBy(AppType::getId, SpringUtil.getProperty("spring.application.name")));

        // 支持多个 profile 的情况
        ENV = Arrays.stream(SpringUtil.getProperty("spring.profiles.active", "dev").split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(profile -> EnumUtil.getBy(Env::name, profile))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(Env.DEV);
        SERVER_IP =  Opt.ofTry(() -> InetAddress.getLocalHost().getHostAddress()).orElse("");
        SERVER_PORT = SpringUtil.getProperty("server.port", Integer.class, 8080);
        BASE_PATH = new ApplicationHome().getDir().getAbsolutePath() + File.separator;
        FILE_URL = SpringUtil.getProperty("app.fileUrl");
    }

    public static boolean isProdEnv() {
        return ApplicationInfo.ENV == Env.PROD;
    }
    public static boolean isTestEnv() {
        return ApplicationInfo.ENV == Env.TEST;
    }
    public static boolean isDevEnv() {
        return ApplicationInfo.ENV == Env.DEV;
    }
}
