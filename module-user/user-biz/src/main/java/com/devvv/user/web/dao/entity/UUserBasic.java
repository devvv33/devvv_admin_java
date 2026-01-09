package com.devvv.user.web.dao.entity;

import com.devvv.commons.common.enums.PackageType;
import com.devvv.commons.common.enums.status.UserStatus;
import com.devvv.commons.common.enums.user.GenderType;
import com.devvv.commons.common.enums.user.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户基本信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UUserBasic {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型;C普通用户 T测试用户
     */
    private UserType userType;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 性别：男(M)、女(F)
     */
    private GenderType gender;

    /**
     * 状态：正常(E)、封禁(D)
     */
    private UserStatus userStatus;

    /**
     * VIP类型， N、普通VIP Y、年VIP
     */
    private String vipType;

    /**
     * VIP到期时间
     */
    private Date vipExpireTime;

    /**
     * 包体
     */
    private PackageType packageType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 注册时间
     */
    private Date registerTime;

    public UUserBasic(Long userId) {
        this.userId = userId;
    }
}