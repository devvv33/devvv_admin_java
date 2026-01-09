package com.devvv.user.api.models.dto;

import com.devvv.commons.common.constant.GlobalConstant;
import com.devvv.commons.common.enums.user.BanType;
import com.devvv.commons.common.enums.user.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Create by WangSJ on 2024/07/08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BanDTO {

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
     * 封禁开始时间
     * 当前时间
     * 封禁判断时，仅会判断endTime，不会判断startTime，此字段仅是用于计算封禁时长
     */
    @NotNull
    private Date startTime;
    /**
     * 封禁结束时间 = startTime + 封禁时长
     * 永久封禁时间，{@link GlobalConstant#MAX_TIME}
     */
    @NotNull
    private Date endTime;

    /**
     * 封禁原因，给客户端的提示
     */
    private String reason;
    /**
     * 备注，给CMS端查看
     */
    private String remark;

    private Long createBy;
    private Date createTime;
}
