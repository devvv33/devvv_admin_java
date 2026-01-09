package com.devvv.cms.dao.cms.entity;

import com.devvv.commons.common.enums.status.EnableStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门表
 */
@Schema(description = "部门表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmsDepartment {
    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 上级部门ID，0表示顶级部门
     */
    @Schema(description = "上级部门ID，0表示顶级部门")
    private Long parentId;

    /**
     * id路径
     */
    @Schema(description = "id路径")
    private String idPath;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 部门编码
     */
    @Schema(description = "部门编码")
    private String deptCode;

    /**
     * 排序，越小越靠前
     */
    @Schema(description = "排序，越小越靠前")
    private Integer sort;

    /**
     * 负责人
     */
    @Schema(description = "负责人")
    private String leader;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String mobile;

    /**
     * 状态：E启用，D停用
     */
    @Schema(description = "状态：E启用，D停用")
    private EnableStatus status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;


    private List<CmsDepartment> children;
}