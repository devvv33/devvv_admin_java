package com.devvv.commons.support.web.config;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.devvv.commons.common.utils.CommonUtil;
import com.devvv.commons.core.config.ApplicationInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by WangSJ on 2024/11/13
 */
@Slf4j
@RestController
public class NacosRegisterConfig {

    /**
     * Nacos服务下线
     */
    @RequestMapping("/dev/nacos/deregister")
    public String deregister(HttpServletRequest request) {
        try {
            NacosRegistration registration = SpringUtil.getBean(NacosRegistration.class);
            NacosServiceRegistry registry = SpringUtil.getBean(NacosServiceRegistry.class);
            registry.deregister(registration);
            log.warn("[{}]- 服务下线: Nacos deregister 调用IP:{} {}", ApplicationInfo.CURRENT_APP_TYPE, request.getRemoteAddr(), CommonUtil.getRuntimeMsg());
        } catch (Exception e) {
            log.warn("[{}]- 服务下线 （无Nacos注册中心依赖） 调用IP:{} {}", ApplicationInfo.CURRENT_APP_TYPE, request.getRemoteAddr(), CommonUtil.getRuntimeMsg());
        }
        return "success";
    }
}
