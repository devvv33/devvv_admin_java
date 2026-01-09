package com.devvv.cms.dao.cms.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户-角色关联表
 */
@Schema(description="用户-角色关联表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmsAdminUserRole {
    /**
    * 用户ID
    */
    @Schema(description="用户ID")
    private Long adminId;

    /**
    * 角色ID
    */
    @Schema(description="角色ID")
    private Long roleId;

    /**
    * 绑定时间
    */
    @Schema(description="绑定时间")
    private Date createTime;
}