package com.devvv.commons.manager.sys.models.form;

import lombok.Data;

/**
 * Create by WangSJ on 2024/06/24
 */
@Data
public class SettingsEditForm {
    /** 参数名 */
    private String key;
    /** 参数值 */
    private String value;
    /** 排序 */
    private Integer sort;
    /** 参数描述 */
    private String remark;
    /** 变动说明 */
    private String changeRemark;
}
