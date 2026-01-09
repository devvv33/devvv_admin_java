package com.devvv.commons.common.enums.app;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 应用类型枚举定义
 */
@Getter
@AllArgsConstructor
public enum AppType implements IDEnum {

    Gateway("Gateway", "网关"),
    UserWeb("user-web", "用户WEB"),
    UserSvr("user-svr", "用户服务"),
    AiWeb("ai-web", "AI-WEB"),
    CmsWeb("cms-web", "管理WEB"),
    ;
    private final String id;
    private final String desc;


}
