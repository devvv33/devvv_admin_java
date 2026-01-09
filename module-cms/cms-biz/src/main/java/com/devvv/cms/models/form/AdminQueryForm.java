package com.devvv.cms.models.form;

import com.devvv.commons.common.dto.common.PageForm;
import lombok.Data;

/**
 * Create by WangSJ on 2024/06/21
 */
@Data
public class AdminQueryForm extends PageForm {

    /** 用户 */
    private Integer adminId;
    /** 角色id */
    private Long roleId;
    /** 手机号 */
    private String mobile;
    /** 昵称 */
    private String nickname;
}
