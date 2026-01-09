package com.devvv.commons.manager.sys.models.form;

import com.github.pagehelper.PageParam;
import lombok.Data;

/**
 * Create by WangSJ on 2024/06/24
 */
@Data
public class SettingsQueryForm extends PageParam {

    /** 参数名称 */
    private String key;
    private String value;
    /** 参数备注 */
    private String remark;
}
