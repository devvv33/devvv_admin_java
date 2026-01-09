package com.devvv.user.web.controller;

import com.devvv.commons.common.response.Result;
import com.devvv.user.web.dao.entity.UUserBasic;
import com.devvv.user.web.models.form.UserIdForm;
import com.devvv.user.web.models.form.UserUpdateForm;
import com.devvv.user.web.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "102-用户相关接口")
@RestController
@RequestMapping("/api/user/info")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @Operation(summary = "1-获取用户详情")
    @PostMapping("/get")
    public Result<UUserBasic> getUser(@Valid @RequestBody UserIdForm form) {
        UUserBasic user = userInfoService.getUser(form);
        return Result.success(user);
    }

    @Operation(summary = "2-创建用户")
    @PostMapping("/create")
    public Result<Long> createUser(@Valid @RequestBody UserUpdateForm form) {
        Long id = userInfoService.createUser(form);
        return Result.success(id);
    }

    @Operation(summary = "3-修改用户")
    @PutMapping("/update")
    public Result updateUser(@Valid @RequestBody UserUpdateForm form) {
        userInfoService.updateUser(form);
        return Result.success();
    }

    @Operation(summary = "4-删除用户")
    @DeleteMapping("/delete")
    public Result deleteUser(@Valid @RequestBody UserIdForm form) {
        userInfoService.deleteUser(form);
        return Result.success();
    }
}
