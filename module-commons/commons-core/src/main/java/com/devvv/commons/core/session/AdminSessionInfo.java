package com.devvv.commons.core.session;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Create by WangSJ on 2024/06/28
 */
@Data
public class AdminSessionInfo {

    private Long adminId;
    private String username;
    private String nickname;
    private String mobile;
    private String avatar;
    private Date loginTime;

    private List<RoleVO> roleList;
}
