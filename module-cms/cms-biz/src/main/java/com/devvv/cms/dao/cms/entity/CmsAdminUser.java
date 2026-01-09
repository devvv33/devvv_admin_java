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
 * 后台用户表
 */
@Schema(description = "后台用户表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmsAdminUser {
    /**
     * 管理员id
     */
    @Schema(description = "管理员id")
    private Long adminId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;

    /**
     * 头像
     */
    @Schema(description = "头像")
    private String avatar;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
    private String mobile;

    /**
     * 所属部门ID
     */
    @Schema(description = "所属部门ID")
    private Long departmentId;

    /**
     * 状态; E正常,D禁用
     */
    @Schema(description = "状态; E正常,D禁用")
    private EnableStatus status;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    private Date lastLoginTime;

    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    /**
     * 登录次数
     */
    @Schema(description = "登录次数")
    private Long loginCount;

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
     * 更新人
     */
    @Schema(description = "更新人")
    private Long updateId;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 其他字段
     */
    private List<CmsRole> roleList;
    private CmsDepartment department;
}