package com.devvv.user.web.models.form;

import com.devvv.commons.common.enums.user.GenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * Create by WangSJ on 2024/12/05
 */
@Data
public class RegisterForm {
    @NotBlank
    private String nickname;
    private String avatar;
    @NotNull
    private GenderType gender;
    private Date birthday;
}
