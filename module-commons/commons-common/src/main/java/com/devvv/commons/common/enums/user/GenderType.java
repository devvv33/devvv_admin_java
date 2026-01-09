package com.devvv.commons.common.enums.user;

import com.devvv.commons.common.enums.IDEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by WangSJ on 2024/07/05
 * 性别
 */
@Getter
@AllArgsConstructor
public enum GenderType implements IDEnum {

    Male("M", "男"),
    Female("F", "女"),
    ;

    public final String id;
    public final String desc;

    /**
     * 获取相对的 目标性别
     */
    public GenderType getTargetGender(){
        return switch (this.id) {
            case "M" -> Female;
            case "F" -> Male;
            default -> null;
        };
    }
}
