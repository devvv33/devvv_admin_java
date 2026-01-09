package com.devvv.commons.token.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.listener.SaTokenEventCenter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import com.devvv.commons.core.config.ApplicationInfo;
import com.devvv.commons.core.context.ClientInfoUtil;
import com.devvv.commons.token.dao.SaJsonTemplateForFastjson2;
import com.devvv.commons.token.dao.SaSessionForFastjson2Customized;
import com.devvv.commons.token.interceptor.ParseTokenUserInterceptor;
import com.devvv.commons.token.listener.ClearTokenListener;
import com.devvv.commons.token.utils.StpAdminUtil;
import com.devvv.commons.token.utils.StpUserUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Create by WangSJ on 2024/06/26
 */
@Order(1)
@Configuration
public class SaTokenConfiguration implements WebMvcConfigurer {

    // 注册 Sa-Token 的拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 检查token
        // 注册路由拦截器，自定义认证规则
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 登录校验 -- 拦截所有api开头的请求，并排除/login 用于开放登录
            SaRouter.match("/api/**")
                    .check(r -> StpUserUtil.checkLogin())
                    // 已登录的接口，必须要有自定义请求头；如果没有，那就不是我们的app发起的请求，直接拒绝
                    .check(r -> ClientInfoUtil.checkClient());

            // 内部服务调用，也必须同源
            // feign调用时自动添加标识: com.devvv.commons.feign.config.FeignInterceptor.apply
            // 参考: https://sa-token.cc/doc.html#/micro/same-token
            SaRouter.match("/inner/**")
                    .check(r -> SaSameUtil.checkCurrentRequestToken());
        })).addPathPatterns("/**");

        // 解析Token
        // 登录账号解析并写入当前Context中
        registry.addInterceptor(new ParseTokenUserInterceptor()).addPathPatterns(ParseTokenUserInterceptor.URL_PATTERN);
    }

    /**
     * 添加session监听器
     */
    @PostConstruct
    public void addListener(){
        // 增加监听器，被踢掉时、被顶掉时 删除对应token
        SaTokenEventCenter.getListenerList().add(new ClearTokenListener());

        // 设置JSON转换器：Fastjson 版
        SaManager.setSaJsonTemplate(new SaJsonTemplateForFastjson2());
        // 重写 SaSession 生成策略
        SaStrategy.instance.createSession = SaSessionForFastjson2Customized::new;
        // 指定 SaSession 类型
        SaStrategy.instance.sessionClassType = SaSessionForFastjson2Customized.class;
    }

    /**
     * SaToken配置，参考: https://sa-token.cc/doc.html#/use/config
     * sa-token:
     *   # token 名称（同时也是 cookie 名称）
     *   token-name: CmsToken
     *   # token 有效期（单位：秒） 默认30天，-1 代表永久有效
     *   timeout: 2592000
     *   # token 最低活跃频率（单位：秒），如果 token 超过此时间没有访问系统就会被冻结，默认-1 代表不限制，永不冻结
     *   active-timeout: -1
     *   # 是否允许同一账号多地同时登录 （为 true 时允许一起登录, 为 false 时新登录挤掉旧登录）
     *   is-concurrent: true
     *   # 在多人登录同一账号时，是否共用一个 token （为 true 时所有登录共用一个 token, 为 false 时每次登录新建一个 token）
     *   is-share: true
     *   # token 风格（默认可取值：uuid、simple-uuid、random-32、random-64、random-128、tik）
     *   token-style: simple-uuid
     *   # 是否输出操作日志
     *   is-log: true
     */
    @Bean
    @Primary
    public SaTokenConfig getSaTokenConfig() {
        // 默认的SaToken配置，用于控制通用行为
        SaTokenConfig globalConfig = new SaTokenConfig();
        globalConfig.setTokenName("global");                  // token 名称（同时也是 cookie 名称）
        globalConfig.setTimeout(24 * 60 * 60);               // token 有效期（单位：秒），默认30天，-1代表永不过期
        globalConfig.setActiveTimeout(-1);                   // token 最低活跃频率（单位：秒），如果 token 超过此时间没有访问系统就会被冻结，默认-1 代表不限制，永不冻结
        globalConfig.setIsConcurrent(true);                  // 是否允许同一账号多地同时登录（为 true 时允许一起登录，为 false 时新登录挤掉旧登录）
        globalConfig.setIsShare(true);                       // 在多人登录同一账号时，是否共用一个 token （为 true 时所有登录共用一个 token，为 false 时每次登录新建一个 token）
        globalConfig.setTokenStyle("simple-uuid");              // token 风格
        globalConfig.setIsLog(!ApplicationInfo.isProdEnv());    // 是否输出操作日志
        globalConfig.setIsPrint(!ApplicationInfo.isProdEnv());  // 是否在初始化配置时打印版本字符画

        // CMS模块登录管理器
        SaTokenConfig adminConfig = new SaTokenConfig();
        adminConfig.setTokenName("cms_token");
        adminConfig.setTimeout(24 * 60 * 60);
        adminConfig.setActiveTimeout(-1);
        adminConfig.setIsConcurrent(true);
        adminConfig.setIsShare(true);
        adminConfig.setTokenStyle("simple-uuid");
        adminConfig.setIsReadBody(false);
        StpAdminUtil.stpLogic.setConfig(adminConfig);

        // app用户登录管理器
        SaTokenConfig userConfig = new SaTokenConfig();
        userConfig.setTokenName("sid");
        userConfig.setTimeout(30 * 24 * 60 * 60);
        userConfig.setActiveTimeout(-1);
        userConfig.setIsConcurrent(false);                  // false:同端互斥登录
        userConfig.setIsShare(false);
        userConfig.setTokenStyle("random-64");
        userConfig.setIsReadBody(false);
        StpUserUtil.stpLogic.setConfig(userConfig);

        return globalConfig;
    }
}
