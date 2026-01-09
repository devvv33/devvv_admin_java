package com.devvv.user.web.models.form;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create by WangSJ on 2023/06/30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdForm {
    @NotNull
    private Long userId;
}
