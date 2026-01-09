package com.devvv.cms.controller.cms;

import com.devvv.cms.dao.cms.entity.CmsMenu;
import com.devvv.cms.service.cms.CmsMenuService;
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
 * Create by WangSJ on 2025/12/30
 */
@Tag(name = "101-菜单管理")
@RestController
@RequestMapping("/cmsApi/menu")
public class CmsMenuController {

    @Resource
    private CmsMenuService menuService;

    @Operation(summary = "01-查询菜单树-所有树菜单")
    @PostMapping("/allMenuTree")
    public Result<List<CmsMenu>> allMenuTree() {
        return Result.success(menuService.allMenuTree());
    }

    @Operation(summary = "02-查询菜单列表-当前用户的")
    @PostMapping("/userMenus")
    public Result<List<CmsMenu>> userMenus() {
        return Result.success(menuService.userMenus());
    }


    @Operation(summary = "11-创建菜单")
    @PostMapping("/createMenu")
    public Result<CmsMenu> createMenu(@Valid @RequestBody CmsMenu form) {
        return Result.success(menuService.createMenu(form));
    }

    @Operation(summary = "12-更新菜单")
    @PostMapping("/updateMenu")
    public Result<CmsMenu> updateMenu(@Valid @RequestBody CmsMenu form) {
        return Result.success(menuService.updateMenu(form));
    }

    @Operation(summary = "13-删除菜单")
    @PostMapping("/deleteMenu")
    public Result<Void> deleteMenu(@Valid @RequestBody CmsMenu form) {
        menuService.deleteMenu(form.getId());
        return Result.success();
    }
}
