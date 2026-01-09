package com.devvv.cms.service.cms;

import cn.hutool.core.lang.Assert;
import com.github.pagehelper.PageHelper;
import com.devvv.cms.dao.cms.entity.CmsRole;
import com.devvv.cms.dao.cms.entity.CmsRoleMenu;
import com.devvv.cms.dao.cms.mapper.CmsRoleMapper;
import com.devvv.cms.dao.cms.mapper.CmsRoleMenuMapper;
import com.devvv.cms.models.form.RoleEditForm;
import com.devvv.cms.models.form.RoleMenuForm;
import com.devvv.cms.models.form.RoleQueryForm;
import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.utils.MyStrUtil;
import com.devvv.commons.core.context.BusiContextUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Create by WangSJ on 2024/07/04
 */
@Slf4j
@Service
public class CmsRoleService {

    @Resource
    private CmsRoleMapper cmsRoleMapper;
    @Resource
    private CmsRoleMenuMapper roleMenuMapper;

    /**
     * 查询角色列表
     */
    public PageVO<CmsRole> pageListRole(RoleQueryForm form) {
        MyStrUtil.trimStringFields(form);
        PageHelper.startPage(form.getPageNum(), form.getPageSize());
        List<CmsRole> list = cmsRoleMapper.listByParam(form);
        return PageVO.build(list);
    }

    public List<CmsRole> listRole(RoleQueryForm form) {
        return cmsRoleMapper.listByParam(form);
    }

    /**
     * 创建角色
     */
    public void createRole(RoleEditForm form) {
        MyStrUtil.trimStringFields(form);
        CmsRole cmsRole = cmsRoleMapper.getByCode(form.getRoleCode());
        Assert.isNull(cmsRole, "角色code已存在");

        CmsRole role = CmsRole.builder()
                .roleCode(form.getRoleCode())
                .roleName(form.getRoleName())
                .status(form.getStatus())
                .sort(form.getSort())
                .remark(form.getRemark())
                .createId(BusiContextUtil.getAdminId())
                .build();
        cmsRoleMapper.insertSelective(role);
    }

    /**
     * 修改角色
     */
    public void updateRole(RoleEditForm form) {
        Assert.notNull(form.getId(), "角色不存在");
        MyStrUtil.trimStringFields(form);
        CmsRole cmsRole = cmsRoleMapper.selectByPrimaryKey(form.getId());
        Assert.notNull(cmsRole, "角色不存在");
        Assert.notEquals(cmsRole.getCreateId(), 0L, "系统内置角色,不可编辑");

        CmsRole update = CmsRole.builder()
                .id(form.getId())
                .roleCode(cmsRole.getRoleCode())
                .roleName(form.getRoleName())
                .status(form.getStatus())
                .remark(form.getRemark())
                .sort(form.getSort())
                .build();
        cmsRoleMapper.updateByPrimaryKeySelective(update);
    }

    /**
     * 删除角色
     */
    public void deleteRole(Long id) {
        Assert.notNull(id, "角色不存在");
        CmsRole cmsRole = cmsRoleMapper.selectByPrimaryKey(id);
        Assert.notNull(cmsRole, "角色不存在");
        Assert.notEquals(cmsRole.getCreateId(), 0L, "系统内置角色,不可删除");

        // 删除角色
        cmsRoleMapper.deleteByPrimaryKey(id);
        // 删除关联菜单
        roleMenuMapper.deleteByRoleId(id);
    }


    /**
     * 查询 角色下的所有菜单ID
     */
    public List<Long> roleMenus(RoleMenuForm form) {
        Assert.notNull(form.getRoleId(), "角色ID不能为空");
        return roleMenuMapper.listMenuIdByRoleId(form.getRoleId());
    }

    /**
     * 为角色分配菜单
     */
    public void assignRoleMenu(RoleMenuForm form) {
        CmsRole role = cmsRoleMapper.selectByPrimaryKey(form.getRoleId());
        Assert.notNull(role, "角色不存在");


        // 删除角色下所有菜单
        roleMenuMapper.deleteByRoleId(form.getRoleId());
        // 重新构建映射
        if (form.getMenuIds() == null || form.getMenuIds().isEmpty()) {
            return;
        }
        List<CmsRoleMenu> roleMenuList = form.getMenuIds().stream().map(menuId -> CmsRoleMenu.builder()
                        .roleId(form.getRoleId())
                        .menuId(menuId)
                        .createTime(new Date())
                        .build())
                .toList();
        roleMenuMapper.batchInsert(roleMenuList);
    }

}
