package com.devvv.cms.models.vo;

import com.devvv.user.api.models.dto.BanDTO;
import lombok.Data;

/**
 * Create by WangSJ on 2024/07/08
 */
@Data
public class UserBanVO extends BanDTO {

    // 日志id
    private Long id;

    /**
     * 封禁目标类型
     */
    private String targetTypeStr;

    /**
     * 封禁时，此处必须是封禁类型
     */
    private String banTypeStr;

    /**
     * 封禁时间
     */
    private String banTime;

    /**
     * 创建人
     */
    private String createName;
}
