package com.devvv.cms.dao.cms.entity;

import com.devvv.cms.models.enums.FieldType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单页面字段表
 */
@Schema(description = "菜单页面字段表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmsMenuField {
    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属菜单ID
     */
    @Schema(description = "所属菜单ID")
    private Long menuId;

    /**
     * 字段Key
     */
    @Schema(description = "字段Key")
    private String fieldKey;

    /**
     * 字段描述
     */
    @Schema(description = "字段描述")
    private String fieldLabel;

    /**
     * 字段类型; SEARCH, COLUMN, FORM
     */
    @Schema(description = "字段类型; SEARCH, COLUMN, FORM")
    private FieldType fieldType;

    /**
     * 是否必须
     */
    @Schema(description = "是否必须")
    private Boolean required;

    /**
     * 输入框类型
     */
    @Schema(description = "输入框类型")
    private String inputType;

    /**
     * 展示类型
     */
    @Schema(description = "展示类型")
    private String showType;

    /**
     * 宽
     */
    @Schema(description = "宽")
    private String width;

    /**
     * 格式化脚本
     */
    @Schema(description = "格式化脚本")
    private String formatScript;

    /**
     * 扩展配置
     */
    @Schema(description = "扩展配置")
    private String extra;

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
}