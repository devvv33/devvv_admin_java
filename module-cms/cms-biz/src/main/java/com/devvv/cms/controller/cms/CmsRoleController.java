package com.devvv.cms.controller.cms;

import com.devvv.cms.dao.cms.entity.CmsRole;
import com.devvv.cms.models.form.RoleEditForm;
import com.devvv.cms.models.form.RoleMenuForm;
import com.devvv.cms.models.form.RoleQueryForm;
import com.devvv.cms.service.cms.CmsRoleService;
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

import java.util.List;

/**
 * Create by WangSJ on 2024/07/04
 */
@Tag(name = "102-角色管理")
@RestController
@RequestMapping("/cmsApi/role")
public class CmsRoleController {

    @Resource
    private CmsRoleService roleService;

    @Operation(summary = "01-查询角色列表")
    @PostMapping("/pageListRole")
    public Result<PageVO<CmsRole>> pageListRole(@RequestBody RoleQueryForm form) {
        return Result.success(roleService.pageListRole(form));
    }

    @Operation(summary = "02-角色列表-一般用于选择器")
    @PostMapping("/listRole")
    public Result<List<CmsRole>> listRole(RoleQueryForm form) {
        return Result.success(roleService.listRole(form));
    }


    @Operation(summary = "11-创建角色")
    @PostMapping("/createRole")
    public Result<Void> createRole(@Valid @RequestBody RoleEditForm form) {
        roleService.createRole(form);
        return Result.success();
    }

    @Operation(summary = "12-修改角色")
    @PostMapping("/updateRole")
    public Result<Void> updateRole(@Valid @RequestBody RoleEditForm form) {
        roleService.updateRole(form);
        return Result.success();
    }

    @Operation(summary = "13-删除角色")
    @PostMapping("/deleteRole")
    public Result<Void> deleteRole(@RequestBody RoleEditForm form) {
        roleService.deleteRole(form.getId());
        return Result.success();
    }

    @Operation(summary = "21-查询角色下的所有菜单")
    @PostMapping("/roleMenus")
    public Result<List<Long>> roleMenus(@RequestBody RoleMenuForm form) {
        return Result.success(roleService.roleMenus(form));
    }

    @Operation(summary = "22-给角色分配菜单")
    @PostMapping("/assignRoleMenu")
    public Result<Void> assignRoleMenu(@Valid @RequestBody RoleMenuForm form) {
        roleService.assignRoleMenu(form);
        return Result.success();
    }
}
