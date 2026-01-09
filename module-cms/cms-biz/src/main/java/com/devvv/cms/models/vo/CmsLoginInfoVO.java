package com.devvv.cms.models.vo;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.devvv.commons.core.session.AdminSessionInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create by WangSJ on 2024/07/04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsLoginInfoVO {

    // token相关信息
    private SaTokenInfo tokenInfo;
    // 用户信息
    private AdminSessionInfo adminSessionInfo;
}
