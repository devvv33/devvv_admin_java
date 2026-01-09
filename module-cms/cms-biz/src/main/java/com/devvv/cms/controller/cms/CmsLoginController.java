package com.devvv.cms.controller.cms;

import cn.dev33.satoken.annotation.SaIgnore;
import com.devvv.cms.models.form.LoginByMobileForm;
import com.devvv.cms.models.form.LoginByUsernameForm;
import com.devvv.cms.models.vo.CmsLoginInfoVO;
import com.devvv.cms.service.cms.CmsLoginService;
import com.devvv.commons.common.response.Result;
import com.devvv.commons.token.utils.StpAdminUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Create by WangSJ on 2024/06/25
 */
@Tag(name = "001-登录接口")
@RestController
@RequestMapping("/cmsApi/login")
public class CmsLoginController {

    @Resource
    private CmsLoginService loginService;

    @Operation(summary = "0-查询当前已登录用户信息")
    @GetMapping("/loginInfo")
    public Result<CmsLoginInfoVO> loginInfo() {
        return Result.success(loginService.getLoginInfo());
    }

    @Operation(summary = "1-账号密码登录")
    @SaIgnore
    @PostMapping("/loginByUsername")
    public Result<CmsLoginInfoVO> loginByUsername(@Valid @RequestBody LoginByUsernameForm form) {
        return Result.success(loginService.loginByUsername(form));
    }

    @Operation(summary = "2-手机号登录")
    @SaIgnore
    @PostMapping("/loginByMobile")
    public Result<CmsLoginInfoVO> saveLoginByMobile(@Valid @RequestBody LoginByMobileForm form) {
        return Result.success(loginService.loginByMobile(form));
    }

    @Operation(summary = "3-获取验证码")
    @SaIgnore
    @PostMapping("/getSmsCode")
    public Result<String> getSmsCode(@Valid @RequestBody LoginByMobileForm form) {
        loginService.getSmsCode(form);
        return Result.success();
    }


    @Operation(summary = "999-退出登录")
    @SaIgnore
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpAdminUtil.logout();
        return Result.success();
    }
}
