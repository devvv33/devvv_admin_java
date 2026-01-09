package com.devvv.cms.models.form;

import com.devvv.commons.common.annotation.MaxLengthClip;
import com.devvv.commons.common.enums.status.EnableStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Create by WangSJ on 2024/06/21
 */
@Data
public class RoleEditForm {

    // 角色ID
    private Long id;

    // 角色代码
    @NotBlank
    private String roleCode;

    // 角色名称
    @NotBlank
    private String roleName;

    // 排序
    private int sort;

    // 状态
    @NotNull
    private EnableStatus status;

    // 备注
    @MaxLengthClip(500)
    private String remark;
}
