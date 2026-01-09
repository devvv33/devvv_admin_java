package com.devvv.cms.dao.cms.entity;

import com.devvv.commons.common.enums.status.EnableStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色表
 */
@Schema(description = "角色表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmsRole {
    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 角色编码，唯一标识，如 ADMIN
     */
    @Schema(description = "角色编码，唯一标识，如 ADMIN")
    private String roleCode;

    /**
     * 角色名称，如 管理员
     */
    @Schema(description = "角色名称，如 管理员")
    private String roleName;

    /**
     * 状态：E启用，D停用
     */
    @Schema(description = "状态：E启用，D停用")
    private EnableStatus status;

    /**
     * 排序值，越小越靠前
     */
    @Schema(description = "排序值，越小越靠前")
    private Integer sort;

    /**
     * 数据权限范围：0全部数据 1本部门 2本部门及以下 3仅本人 4自定义
     */
    @Schema(description = "数据权限范围：0全部数据 1本部门 2本部门及以下 3仅本人 4自定义")
    private Byte dataScope;

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
}