package com.devvv.cms.models.vo;

import com.devvv.user.api.models.dto.UserDTO;
import lombok.Data;

/**
 * Create by WangSJ on 2024/07/09
 */
@Data
public class UserVO extends UserDTO {

    private String userStatusStr;
    private String genderStr;

    // 封禁状态
    private String banStatusStr;

}
