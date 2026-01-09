package com.devvv.user.api.models.dto;

import com.devvv.commons.common.enums.user.BanType;
import com.devvv.commons.common.enums.user.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create by WangSJ on 2024/07/08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnbanDTO {
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
     * 解封时，此处必须是解封类型
     */
    @NotNull
    private BanType banType;

    /**
     * 备注
     */
    private String remark;
}
