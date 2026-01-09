package com.devvv.user.api.models.dto;

import com.devvv.commons.common.dto.common.PageForm;
import com.devvv.commons.common.enums.user.BanType;
import com.devvv.commons.common.enums.user.TargetType;
import lombok.Data;

/**
 * Create by WangSJ on 2024/07/08
 */
@Data
public class UserBanPageQueryDTO extends PageForm {

    /**
     * 封禁目标类型
     */
    private TargetType targetType;
    /**
     * 封禁目标
     */
    private String targetValue;

    /**
     * 封禁时，此处必须是封禁类型
     */
    private BanType banType;

    /**
     * 封禁原因，给客户端的提示
     */
    private String reason;
    /**
     * 备注，给CMS端查看
     */
    private String remark;


}
