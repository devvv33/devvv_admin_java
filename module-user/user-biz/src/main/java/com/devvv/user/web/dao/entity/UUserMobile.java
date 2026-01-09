package com.devvv.user.web.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户手机号
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UUserMobile {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 是否已取消订阅
     */
    private Boolean unsubscribe;

    /**
     * 取消订阅时间
     */
    private Date unsubscribeTime;

    /**
     * 创建时间
     */
    private Date createTime;
}