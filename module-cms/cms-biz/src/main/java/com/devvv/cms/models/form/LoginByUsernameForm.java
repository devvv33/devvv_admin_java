package com.devvv.cms.models.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * Create by WangSJ on 2025/12/31
 */
@Data
public class LoginByUsernameForm {

    @Schema(title = "账号")
    @Length(min = 2, max = 20)
    private String username;

    @Schema(title = "密码")
    @Length(min = 2, max = 20)
    private String password;
}
