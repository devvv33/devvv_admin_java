package com.devvv.cms.models.form;

import com.devvv.commons.common.dto.common.PageForm;
import com.devvv.commons.common.enums.status.EnableStatus;
import lombok.Data;

/**
 * Create by WangSJ on 2024/06/21
 */
@Data
public class RoleQueryForm extends PageForm {
    /** 角色名称 */
    private String roleName;
    /** 角色code */
    private String roleCode;
    /** 状态 */
    private EnableStatus status;
}
