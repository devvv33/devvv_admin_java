package com.devvv.cms.models.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create by WangSJ on 2024/06/21
 *
 * 下拉选项转化类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Option {

    /** 下拉框中option对应的value */
    private String value;
    /** option中对应的text */
    private String label;
}
