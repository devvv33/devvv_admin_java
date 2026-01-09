package com.devvv.user.web.models.vo;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.devvv.commons.core.session.UserSessionInfo;
import lombok.Data;

/**
 * Create by WangSJ on 2024/07/05
 */
@Data
public class UserLoginVO {

    private SaTokenInfo tokenInfo;
    private UserSessionInfo userSessionInfo;

}
