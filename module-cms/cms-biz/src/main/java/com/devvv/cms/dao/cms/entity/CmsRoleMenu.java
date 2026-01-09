package com.devvv.cms.dao.cms.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色-菜单关联表
 */
@Schema(description = "角色-菜单关联表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmsRoleMenu {
    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    private Long roleId;

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID")
    private Long menuId;

    /**
     * 绑定时间
     */
    @Schema(description = "绑定时间")
    private Date createTime;
}