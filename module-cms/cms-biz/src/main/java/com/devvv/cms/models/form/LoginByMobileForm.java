package com.devvv.cms.models.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * Create by WangSJ on 2024/06/20
 */
@Data
public class LoginByMobileForm {

    @Schema(title = "手机号")
    @Length(min = 11, max = 11)
    private String mobile;

    @Schema(title = "验证码")
    @Length(min = 4, max = 4)
    private String code;
}
