package com.devvv.commons.core.session;

import com.devvv.commons.common.enums.status.UserStatus;
import com.devvv.commons.common.enums.user.GenderType;
import com.devvv.commons.common.enums.user.UserType;
import lombok.Data;

import java.util.Date;

/**
 * Create by WangSJ on 2024/06/28
 */
@Data
public class UserSessionInfo {

    private Long userId;
    private UserType userType;
    private UserStatus userStatus;
    private String nickname;
    private String realname;
    private String avatar;
    private GenderType gender;
    private Date registerTime;
    private Date vipExpireTime;

    // 此字段在多端登录时，将会不准确，届时将会是最后一次登录的时间
    private Date loginTime;

}
