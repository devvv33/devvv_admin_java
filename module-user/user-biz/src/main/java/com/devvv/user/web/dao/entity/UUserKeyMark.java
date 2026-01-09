package com.devvv.user.web.dao.entity;

import com.devvv.commons.common.enums.user.UserKeyMarkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户标记
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UUserKeyMark {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * key
     */
    private UserKeyMarkType key;

    /**
     * 值
     */
    private String value;

    /**
     * 扩展数据
     */
    private String ext;

    /**
     * 创建时间
     */
    private Date createTime;

    public UUserKeyMark(Long userId, UserKeyMarkType key) {
        this.userId = userId;
        this.key = key;
    }
}