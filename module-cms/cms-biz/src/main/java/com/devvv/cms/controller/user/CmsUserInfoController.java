package com.devvv.cms.controller.user;

import com.devvv.cms.models.form.user.QueryUserForm;
import com.devvv.cms.models.vo.UserVO;
import com.devvv.cms.service.user.CmsUserInfoService;
import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by WangSJ on 2024/06/25
 */
@Tag(name = "201-User用户管理")
@RestController
@RequestMapping("/cmsApi/user/info")
public class CmsUserInfoController {

    @Resource
    private CmsUserInfoService cmsUserInfoService;

    @Operation(summary = "1-分页查询用户列表")
    @PostMapping("/pageList")
    public Result<PageVO<UserVO>> pageList(@Valid @RequestBody QueryUserForm param) {
        PageVO<UserVO> page = cmsUserInfoService.pageList(param);
        return Result.success(page);
    }

}
