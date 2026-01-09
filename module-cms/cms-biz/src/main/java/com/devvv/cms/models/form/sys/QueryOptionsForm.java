package com.devvv.cms.models.form.sys;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Create by WangSJ on 2025/12/26
 */
@Data
public class QueryOptionsForm {

    @NotBlank
    private String className;
}
