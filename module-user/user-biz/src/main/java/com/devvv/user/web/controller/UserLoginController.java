package com.devvv.user.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.devvv.commons.common.response.Result;
import com.devvv.commons.core.context.ClientInfoUtil;
import com.devvv.user.web.models.form.LoginByMobileForm;
import com.devvv.user.web.models.form.RegisterForm;
import com.devvv.user.web.models.vo.UserLoginVO;
import com.devvv.user.web.service.UserLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by WangSJ on 2024/07/05
 */
@Tag(name = "101-登录")
@RestController
@RequestMapping("/api/user/login")
public class UserLoginController {

    @Resource
    private UserLoginService loginService;

    @Operation(summary = "0-查询当前已登录用户信息")
    @PostMapping("/loginInfo")
    public Result<UserLoginVO> loginInfo() {
        return Result.success(loginService.getLoginInfo());
    }

    @Operation(summary = "101-手机号登录-获取验证码")
    @SaIgnore
    @PostMapping("/sendSmsCode")
    public Result sendSms(@Valid @RequestBody LoginByMobileForm form) {
        ClientInfoUtil.checkClient();
        loginService.sendSmsCode(form);
        return Result.success();
    }

    @Operation(summary = "102-手机号登录")
    @SaIgnore
    @PostMapping("/loginByMobile")
    public Result<UserLoginVO> loginByMobile(@Valid @RequestBody LoginByMobileForm form) {
        ClientInfoUtil.checkClient();
        return Result.success(loginService.loginByMobile(form));
    }

    @Operation(summary = "970-完善注册")
    @PostMapping("/register")
    public Result<UserLoginVO> register(@Valid @RequestBody RegisterForm form) {
        return Result.success(loginService.register(form));
    }

    @Operation(summary = "980-用户上线")
    @PostMapping("/online")
    public Result<UserLoginVO> online() {
        return Result.success(loginService.online());
    }

    @Operation(summary = "999-退出登录")
    @SaIgnore
    @PostMapping("/logout")
    public Result<Void> logout() {
        loginService.logout();
        return Result.success();
    }
    
}
