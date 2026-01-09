package com.devvv.commons.core.busicode;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by WangSJ on 2024/07/01
 */
@Getter
@AllArgsConstructor
public enum BusiCodeDefine implements IDEnum {

    None("None"),

    /**********************  登录相关 **************************/
    LoginByMobile("手机号登录"),
    Register("完成注册"),
    Online("用户上线"),
    Logout("退出登录"),

    ;

    private final String desc;

    @Override
    public String getId() {
        return this.name();
    }
}
