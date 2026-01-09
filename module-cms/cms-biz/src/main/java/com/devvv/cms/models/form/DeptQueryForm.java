package com.devvv.cms.models.form;

import com.devvv.commons.common.dto.common.PageForm;
import com.devvv.commons.common.enums.status.EnableStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Create by WangSJ on 2025/12/29
 */
@Data
public class DeptQueryForm extends PageForm {

    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "上级部门ID，0表示顶级部门")
    private Long parentId;
    @Schema(description = "id路径")
    private String idPath;

    @Schema(description = "部门名称")
    private String deptName;
    @Schema(description = "部门编码")
    private String deptCode;

    @Schema(description = "负责人")
    private String leader;
    @Schema(description = "联系电话")
    private String mobile;

    @Schema(description = "状态：E启用，D停用")
    private EnableStatus status;
    @Schema(description = "备注")
    private String remark;

}
