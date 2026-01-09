package com.devvv.user.api.models.dto;

import com.devvv.commons.common.dto.common.PageForm;
import lombok.Data;

/**
 * Create by WangSJ on 2024/06/26
 */
@Data
public class UserPageQueryDTO extends PageForm {

    private Long userId;
    private String nickname;
}
