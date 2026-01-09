package com.devvv.cms.models.form;

import com.devvv.commons.common.enums.status.EnableStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Create by WangSJ on 2024/06/21
 */
@Data
public class AdminUserForm {
    // 管理员id
    private Long adminId;
    // 用户名
    private String username;
    // 密码
    private String password;
    // 昵称
    @NotBlank
    @Length(min = 1, max = 20, message = "昵称长度不能超过20个字符")
    private String nickname;
    // 头像
    private String avatar;
    private MultipartFile avatarFile;
    // 手机号
    @Length(min = 11, max = 11, message = "手机号长度错误")
    private String mobile;
    // 状态
    private EnableStatus status;
    // 所属部门ID
    @NotNull
    private Long departmentId;
    // 角色列表
    private List<Long> roleIdList;
}
