package com.devvv.cms.service.cms;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ObjUtil;
import com.devvv.cms.dao.cms.entity.CmsDepartment;
import com.devvv.cms.dao.cms.mapper.CmsDepartmentMapper;
import com.devvv.cms.models.form.DeptQueryForm;
import com.devvv.commons.common.utils.MyStrUtil;
import com.devvv.commons.core.context.BusiContextUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Create by WangSJ on 2025/12/29
 */
@Slf4j
@Service
public class CmsDepartmentService {

    @Resource
    private CmsDepartmentMapper departmentMapper;

    /**
     * 查询部门列表
     */
    public List<CmsDepartment> treeListDept(DeptQueryForm form) {
        MyStrUtil.trimStringFields(form);
        List<CmsDepartment> list = departmentMapper.listByParam(form);
        // 构建树
        List<CmsDepartment> treeList = TreeUtil.build(list, 0L, CmsDepartment::getId, CmsDepartment::getParentId, CmsDepartment::setChildren);
        return treeList;
    }




    /**
     * 添加部门
     */
    @Transactional
    public void createDept(CmsDepartment form) {
        Assert.notNull(form.getParentId(), "上级部门不存在");
        // Assert.notBlank(form.getDeptCode(), "部门code不能为空");
        Assert.notBlank(form.getDeptName(), "部门名称不能为空");
        Assert.notNull(form.getStatus(), "状态不能为空");
        MyStrUtil.trimStringFields(form);
        CmsDepartment cmsDepartment = departmentMapper.getByCode(form.getDeptCode());
        Assert.isNull(cmsDepartment, "部门code已存在");
        // 查找上级节点
        CmsDepartment parent = ObjUtil.equal(form.getParentId(), 0L) ?
                CmsDepartment.builder()
                        .id(0L)
                        .idPath("/")
                        .build()
                : departmentMapper.selectByPrimaryKey(form.getParentId());
        Assert.notNull(parent, "上级部门不存在");


        CmsDepartment dept = CmsDepartment.builder()
                .parentId(form.getParentId())
                .idPath("")
                .deptCode(form.getDeptCode())
                .deptName(form.getDeptName())
                .status(form.getStatus())
                .sort(form.getSort())
                .leader(form.getLeader())
                .mobile(form.getMobile())
                .remark(form.getRemark())
                .createId(BusiContextUtil.getAdminId())
                .build();
        departmentMapper.insertSelective(dept);

        // 拿到ID后, 补充idPath
        departmentMapper.updateByPrimaryKeySelective(CmsDepartment.builder()
                .id(dept.getId())
                .idPath(parent.getIdPath() + dept.getId() + "/")
                .build());
    }

    /**
     * 编辑角色
     */
    @Transactional
    public void updateDept(@Valid CmsDepartment form) {
        Assert.notNull(form.getId(), "部门不存在");
        Assert.notNull(form.getParentId(), "上级部门不存在");
        Assert.notBlank(form.getDeptName(), "部门名称不能为空");
        Assert.notNull(form.getStatus(), "状态不能为空");
        MyStrUtil.trimStringFields(form);
        CmsDepartment exist = departmentMapper.selectByPrimaryKey(form.getId());
        Assert.notNull(exist, "部门不存在");
        Assert.notEquals(exist.getCreateId(), 0L, "系统内置部门,不可编辑");

        // 查找上级节点
        CmsDepartment parent = ObjUtil.equal(form.getParentId(), 0L) ?
                CmsDepartment.builder()
                        .id(0L)
                        .idPath("/")
                        .build()
                : departmentMapper.selectByPrimaryKey(form.getParentId());
        Assert.notNull(parent, "上级部门不存在");


        CmsDepartment dept = CmsDepartment.builder()
                .id(form.getId())
                .parentId(form.getParentId())
                .idPath("")
                .deptName(form.getDeptName())
                .status(form.getStatus())
                .sort(form.getSort())
                .leader(form.getLeader())
                .mobile(form.getMobile())
                .remark(form.getRemark())
                .createId(BusiContextUtil.getAdminId())
                .build();
        departmentMapper.updateByPrimaryKeySelective(dept);

        // 拿到ID后, 补充idPath
        departmentMapper.updateByPrimaryKeySelective(CmsDepartment.builder()
                .id(dept.getId())
                .idPath(parent.getIdPath() + dept.getId() + "/")
                .build());
    }

    /**
     * 删除角色
     */
    @Transactional
    public void deleteDept(Long id) {
        Assert.notNull(id, "部门不存在");
        CmsDepartment exist = departmentMapper.selectByPrimaryKey(id);
        Assert.notNull(exist, "部门不存在");
        Assert.notEquals(exist.getCreateId(), 0L, "系统内置部门,不可删除");

        // 下级部门如何处理?
        List<CmsDepartment> children = departmentMapper.listByParentId(id);
        Assert.empty(children, "部门下有下级部门,请先删除下级部门");

        departmentMapper.deleteByPrimaryKey(id);
    }
}
