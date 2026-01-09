package com.devvv.user.web.dao.entity;

import com.devvv.commons.common.enums.user.BanType;
import com.devvv.commons.common.enums.user.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 封禁记录
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TBan {
    /**
     * 目标类型
     */
    private TargetType targetType;

    /**
     * 目标值
     */
    private String targetValue;

    /**
     * 封禁类型
     */
    private BanType banType;

    /**
     * 封禁生效时间
     */
    private Date startTime;

    /**
     * 封禁结束时间
     */
    private Date endTime;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作人
     */
    private Long createBy;

    /**
     * 封禁时间
     */
    private Date createTime;

    public TBan(TargetType targetType, String targetValue, BanType banType) {
        this.targetType = targetType;
        this.targetValue = targetValue;
        this.banType = banType;
    }
}