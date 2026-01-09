package com.devvv.cms.dao.cms.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单表
 */
@Schema(description = "菜单表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmsMenu {
    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 父级ID
     */
    @Schema(description = "父级ID")
    private Long parentId;

    /**
     * ID路径
     */
    @Schema(description = "ID路径")
    private String idPath;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    private String menuName;

    /**
     * 菜单类型
     */
    @Schema(description = "菜单类型")
    private String menuType;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private String icon;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sort;

    /**
     * 路由路径
     */
    @Schema(description = "路由路径")
    private String routePath;

    /**
     * 页面类型
     */
    @Schema(description = "页面类型")
    private String pageType;

    /**
     * 接口路径
     */
    @Schema(description = "接口路径")
    private String apiUrl;

    /**
     * 扩展接口路径
     */
    @Schema(description = "扩展接口路径")
    private String extApiUrl;

    /**
     * 扩展配置
     */
    @Schema(description = "扩展配置")
    private String extra;

    /**
     * 自定义页面code
     */
    @Schema(description = "自定义页面code")
    private String customComponent;

    /**
     * 按钮所在位置
     */
    @Schema(description = "按钮所在位置")
    private String buttonPosition;

    /**
     * 按钮行为
     */
    @Schema(description = "按钮行为")
    private String buttonAction;

    /**
     * 行数据回显前脚本
     */
    @Schema(description = "行数据回显前脚本")
    private String beforeShowScript;

    /**
     * 表单提交前脚本
     */
    @Schema(description = "表单提交前脚本")
    private String beforeSubmitScript;

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
    private List<CmsMenuField> searchFieldList;
    private List<CmsMenuField> columnFieldList;
    private List<CmsMenuField> formFieldList;
    private List<CmsMenu> children;

}