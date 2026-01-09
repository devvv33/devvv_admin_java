package com.devvv.commons.common.enums.status;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举（通用）
 */
@Getter
@AllArgsConstructor
public enum AuditStatus implements IDEnum {

    Wait("W", "待审核"),
    Pass("P", "审核通过"),
    Reject("J", "审核拒绝"),
    ;

    private final String id;
    private final String desc;

}
