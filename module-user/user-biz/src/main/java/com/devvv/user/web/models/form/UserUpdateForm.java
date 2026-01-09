package com.devvv.user.web.models.form;

import com.devvv.commons.common.enums.PackageType;
import com.devvv.commons.common.enums.status.UserStatus;
import com.devvv.commons.common.enums.user.GenderType;
import com.devvv.commons.common.enums.user.UserType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Create by WangSJ on 2024/07/05
 */
@Data
public class UserUpdateForm {

    private Long userId;

    // 用户类型
    private UserType userType;
    // 昵称
    @NotBlank
    private String nickname;
    // 头像
    private String avatar;
    // 性别
    private GenderType gender;
    // 用户状态
    private UserStatus userStatus;
    // 包体
    private PackageType packageType;

}
