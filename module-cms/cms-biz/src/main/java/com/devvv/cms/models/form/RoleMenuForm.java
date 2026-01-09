package com.devvv.cms.models.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Create by WangSJ on 2025/12/31
 */
@Data
public class RoleMenuForm {

    @NotNull
    private Long roleId;
    @NotNull
    private List<Long> menuIds;
}
