package com.devvv.user.web.models.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Create by WangSJ on 2024/07/05
 */
@Data
public class LoginByMobileForm {

    // 手机号
    @NotBlank
    private String mobile;
    // 短信验证码
    private String code;

}
