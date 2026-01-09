package com.devvv.cms.models.form.user;

import com.devvv.commons.common.enums.user.BanType;
import com.devvv.commons.common.enums.user.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Create by WangSJ on 2024/07/08
 */
@Data
public class CmsBanUserForm {

    /**
     * 封禁目标类型
     */
    @NotNull
    private TargetType targetType;
    /**
     * 封禁目标
     */
    @NotBlank
    private String targetValue;

    /**
     * 封禁时，此处必须是封禁类型
     */
    @NotNull
    private BanType banType;

    /**
     * 封禁时间（秒）
     */
    private Long banSecond;

    /**
     * 封禁原因，给客户端的提示
     */
    private String reason;
    /**
     * 备注，给CMS端查看
     */
    private String remark;
}
