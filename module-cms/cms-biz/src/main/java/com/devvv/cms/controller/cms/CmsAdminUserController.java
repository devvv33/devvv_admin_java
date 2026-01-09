package com.devvv.cms.controller.cms;

import com.devvv.cms.dao.cms.entity.CmsAdminUser;
import com.devvv.cms.models.form.AdminQueryForm;
import com.devvv.cms.models.form.AdminUserForm;
import com.devvv.cms.service.cms.CmsAdminUserService;
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

import java.io.IOException;

/**
 * Create by WangSJ on 2024/06/25
 */
@Tag(name = "104-账号管理")
@RestController
@RequestMapping("/cmsApi/admin")
public class CmsAdminUserController {

    @Resource
    private CmsAdminUserService cmsAdminUserService;

    @Operation(summary = "1-分页查询后台账号列表")
    @PostMapping("/pageListAdmin")
    public Result<PageVO<CmsAdminUser>> pageListAdmin(@RequestBody AdminQueryForm form) {
        return Result.success(cmsAdminUserService.pageListAdmin(form));
    }

    @Operation(summary = "2-创建后台账号")
    @PostMapping("/createAdmin")
    public Result<Void> createAdmin(@Valid @RequestBody AdminUserForm form) throws IOException {
        cmsAdminUserService.createAdmin(form);
        return Result.success();
    }

    @Operation(summary = "3-修改账号")
    @PostMapping("/updateAdmin")
    public Result<Void> updateAdmin(@Valid AdminUserForm form) throws IOException {
        cmsAdminUserService.updateAdmin(form);
        return Result.success();
    }

    @Operation(summary = "4-禁用&启用后台账号")
    @PostMapping("/updateAdminStatus")
    public Result<Void> updateAdminStatus(@RequestBody AdminUserForm form) {
        cmsAdminUserService.updateAdminStatus(form);
        return Result.success();
    }

}
