package com.devvv.user.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.devvv.commons.common.response.Result;
import com.devvv.user.api.models.dto.BanDTO;
import com.devvv.user.api.models.dto.UnbanDTO;
import com.devvv.user.web.service.UserBanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by WangSJ on 2024/07/08
 */
@Tag(name = "103-用户封禁接口")
@RestController
@RequestMapping("/api/user/ban")
public class UserBanController {

    @Resource
    private UserBanService userBanService;

    @SaIgnore
    @Operation(summary = "1-封禁")
    @PostMapping("/ban")
    public Result ban(@Valid @RequestBody BanDTO form) {
        userBanService.ban(form);
        return Result.success();
    }

    @SaIgnore
    @Operation(summary = "2-解除封禁")
    @PostMapping("/unban")
    public Result unban(@Valid @RequestBody UnbanDTO form) {
        userBanService.unban(form);
        return Result.success();
    }

}
