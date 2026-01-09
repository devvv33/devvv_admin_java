package com.devvv.cms.models.form.user;

import com.devvv.commons.common.dto.common.PageForm;
import lombok.Data;

/**
 * Create by WangSJ on 2024/06/25
 */
@Data
public class QueryUserForm extends PageForm {

    private Long userId;
    private String nickname;

}
