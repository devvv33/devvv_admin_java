package com.devvv.cms.controller.cms;

import com.devvv.cms.dao.cms.entity.CmsDepartment;
import com.devvv.cms.models.form.DeptQueryForm;
import com.devvv.cms.service.cms.CmsDepartmentService;
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
 * Create by WangSJ on 2025/12/29
 */
@Tag(name = "103-角色管理")
@RestController
@RequestMapping("/cmsApi/dept")
public class CmsDepartmentController {

    @Resource
    private CmsDepartmentService departmentService;

    @Operation(summary = "1-查询部门列表")
    @PostMapping("/treeListDept")
    public Result<List<CmsDepartment>> treeListDept(@RequestBody DeptQueryForm form) {
        return Result.success(departmentService.treeListDept(form));
    }


    @Operation(summary = "2-创建部门")
    @PostMapping("/createDept")
    public Result<Void> createDept(@Valid @RequestBody CmsDepartment form) {
        departmentService.createDept(form);
        return Result.success();
    }

    @Operation(summary = "3-修改部门")
    @PostMapping("/updateDept")
    public Result<Void> updateDept(@Valid @RequestBody CmsDepartment form) {
        departmentService.updateDept(form);
        return Result.success();
    }

    @Operation(summary = "4-删除部门")
    @PostMapping("/deleteDept")
    public Result<Void> deleteDept(@RequestBody CmsDepartment form) {
        departmentService.deleteDept(form.getId());
        return Result.success();
    }
}
