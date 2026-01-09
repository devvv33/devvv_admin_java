package com.devvv.cms.service.cms;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.devvv.cms.dao.cms.entity.CmsMenu;
import com.devvv.cms.dao.cms.entity.CmsMenuField;
import com.devvv.cms.dao.cms.mapper.CmsMenuFieldMapper;
import com.devvv.cms.dao.cms.mapper.CmsMenuMapper;
import com.devvv.cms.manager.MenuManager;
import com.devvv.cms.models.enums.FieldType;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.common.response.ErrorCode;
import com.devvv.commons.core.config.cache.local.LocalCache;
import com.devvv.commons.core.config.cache.local.LocalCacheEnums;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.utils.BusiTransactionUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create by WangSJ on 2025/12/30
 */
@Slf4j
@Service
public class CmsMenuService {

    @Resource
    private CmsMenuMapper menuMapper;
    @Resource
    private CmsMenuFieldMapper menuFieldMapper;

    /**
     * 查询菜单树
     */
    public List<CmsMenu> allMenuTree() {
        // 因为是用于编辑, 这里从数据库读取数据
        List<CmsMenu> menuList = menuMapper.listAll();

        List<CmsMenuField> fieldList = menuFieldMapper.listAll();
        Map<Long, List<CmsMenuField>> groupByMenuId = fieldList.stream().collect(Collectors.groupingBy(CmsMenuField::getMenuId));

        // 为menu补充所有字段
        for (CmsMenu menu : menuList) {
            List<CmsMenuField> list = groupByMenuId.getOrDefault(menu.getId(), Collections.emptyList());
            menu.setSearchFieldList(list.stream().filter(field -> ObjUtil.equal(FieldType.Search, field.getFieldType())).toList());
            menu.setColumnFieldList(list.stream().filter(field -> ObjUtil.equal(FieldType.Column, field.getFieldType())).toList());
            menu.setFormFieldList(list.stream().filter(field -> ObjUtil.equal(FieldType.Form, field.getFieldType())).toList());
        }

        return TreeUtil.build(menuList, 0L, CmsMenu::getId, CmsMenu::getParentId, CmsMenu::setChildren);
    }

    /**
     * 查询当前用户的菜单
     */
    public List<CmsMenu> userMenus() {
        // 这里从缓存读取所有菜单
        List<CmsMenu> menuList = MenuManager.getInstance().listUserMenus(BusiContextUtil.getAdminId(), BusiContextUtil.getRoleCodes());

        // 为menu补充所有字段
        for (CmsMenu menu : menuList) {
            List<CmsMenuField> list = MenuManager.getInstance().listFieldByMenuId(menu.getId());
            menu.setSearchFieldList(list.stream().filter(field -> ObjUtil.equal(FieldType.Search, field.getFieldType())).toList());
            menu.setColumnFieldList(list.stream().filter(field -> ObjUtil.equal(FieldType.Column, field.getFieldType())).toList());
            menu.setFormFieldList(list.stream().filter(field -> ObjUtil.equal(FieldType.Form, field.getFieldType())).toList());
        }
        return menuList;
    }

    /**
     * 添加菜单
     */
    @Transactional
    public CmsMenu createMenu(CmsMenu form) {
        Assert.notNull(form.getParentId(), "上级菜单不存在");
        Assert.notBlank(form.getMenuName(), "菜单名称不能为空");
        Assert.notBlank(form.getMenuType(), "菜单类型不能为空");
        Assert.notNull(form.getSort(), "排序不能为空");
        // 路由唯一
        if (StrUtil.isNotBlank(form.getRoutePath())) {
            MenuManager.getInstance().listAllMenu().stream()
                    .filter(menu -> ObjUtil.equal(menu.getRoutePath(), form.getRoutePath()))
                    .findAny()
                    .ifPresent(menu -> {
                        throw new BusiException(ErrorCode.PARAM_ERR, "路由路径已存在");
                    });
        }
        // 查找上级节点
        CmsMenu parent = ObjUtil.equal(form.getParentId(), 0L) ?
                CmsMenu.builder().id(0L).idPath("/").build()
                : menuMapper.selectByPrimaryKey(form.getParentId());
        Assert.notNull(parent, "上级菜单不存在");

        // 添加菜单
        form.setIdPath("");
        form.setCreateId(BusiContextUtil.getAdminId());
        form.setUpdateId(BusiContextUtil.getAdminId());
        menuMapper.insertSelective(form);

        // 补充idPath
        menuMapper.updateByPrimaryKeySelective(CmsMenu.builder()
                .id(form.getId())
                .idPath(parent.getIdPath() + form.getId() + "/")
                .build());

        // 存储字段列表
        List<CmsMenuField> searchFieldList = Opt.ofNullable(form.getSearchFieldList())
                .peek(list -> list.forEach(field -> field.setFieldType(FieldType.Search)))
                .orElse(Collections.emptyList());
        List<CmsMenuField> columnFieldList = Opt.ofNullable(form.getColumnFieldList())
                .peek(list -> list.forEach(field -> field.setFieldType(FieldType.Column)))
                .orElse(Collections.emptyList());
        List<CmsMenuField> formFieldList = Opt.ofNullable(form.getFormFieldList())
                .peek(list -> list.forEach(field -> field.setFieldType(FieldType.Form)))
                .orElse(Collections.emptyList());
        List<CmsMenuField> allFieldList = Stream.of(searchFieldList, columnFieldList, formFieldList)
                .flatMap(List::stream)
                .peek(field -> {
                    field.setMenuId(form.getId());
                    field.setCreateId(BusiContextUtil.getAdminId());
                    field.setCreateTime(new Date());
                })
                .toList();
        if (!allFieldList.isEmpty()) {
            menuFieldMapper.batchInsert(allFieldList);
        }

        BusiTransactionUtil.execAfterCommit(() -> LocalCache.notifyReload(LocalCacheEnums.MenuManager));
        return form;
    }

    /**
     * 更新菜单
     */
    @Transactional
    public CmsMenu updateMenu(@Valid CmsMenu form) {
        Assert.notNull(form.getId(), "菜单ID不能为空");
        Assert.notNull(form.getParentId(), "上级菜单不存在");
        Assert.notBlank(form.getMenuName(), "菜单名称不能为空");
        Assert.notBlank(form.getMenuType(), "菜单类型不能为空");
        Assert.notNull(form.getSort(), "排序不能为空");
        CmsMenu exist = menuMapper.selectByPrimaryKey(form.getId());
        Assert.notNull(exist, "菜单不存在");
        if (ObjUtil.equal(exist.getCreateId(), 0L)) {
            Assert.isTrue(BusiContextUtil.isSuperAdmin(), "系统菜单,无权修改");
        }
        // 路由唯一
        if (StrUtil.isNotBlank(form.getRoutePath()) && ObjUtil.notEqual(form.getRoutePath(), exist.getRoutePath())) {
            MenuManager.getInstance().listAllMenu().stream()
                    .filter(menu -> ObjUtil.equal(menu.getRoutePath(), form.getRoutePath()))
                    .findAny()
                    .ifPresent(menu -> {
                        throw new BusiException(ErrorCode.PARAM_ERR, "路由路径已存在");
                    });
        }

        // 查找上级节点
        CmsMenu parent = ObjUtil.equal(form.getParentId(), 0L) ?
                CmsMenu.builder().id(0L).idPath("/").build()
                : menuMapper.selectByPrimaryKey(form.getParentId());
        Assert.notNull(parent, "上级菜单不存在");

        // 更新菜单
        exist.setParentId(form.getParentId());
        exist.setIdPath(parent.getIdPath() + exist.getId() + "/");
        exist.setMenuName(form.getMenuName());
        exist.setMenuType(form.getMenuType());
        exist.setIcon(form.getIcon());
        exist.setSort(form.getSort());
        exist.setRoutePath(form.getRoutePath());
        exist.setPageType(form.getPageType());
        exist.setApiUrl(form.getApiUrl());
        exist.setExtApiUrl(form.getExtApiUrl());
        exist.setExtra(form.getExtra());
        exist.setPageType(form.getPageType());
        exist.setCustomComponent(form.getCustomComponent());
        exist.setButtonPosition(form.getButtonPosition());
        exist.setButtonAction(form.getButtonAction());
        exist.setBeforeShowScript(form.getBeforeShowScript());
        exist.setBeforeSubmitScript(form.getBeforeSubmitScript());
        exist.setUpdateId(BusiContextUtil.getAdminId());
        exist.setUpdateTime(new Date());
        menuMapper.updateByPrimaryKey(exist);

        // 存储字段列表
        menuFieldMapper.deleteByMenuId(exist.getId());
        List<CmsMenuField> searchFieldList = Opt.ofNullable(form.getSearchFieldList())
                .peek(list -> list.forEach(field -> field.setFieldType(FieldType.Search)))
                .orElse(Collections.emptyList());
        List<CmsMenuField> columnFieldList = Opt.ofNullable(form.getColumnFieldList())
                .peek(list -> list.forEach(field -> field.setFieldType(FieldType.Column)))
                .orElse(Collections.emptyList());
        List<CmsMenuField> formFieldList = Opt.ofNullable(form.getFormFieldList())
                .peek(list -> list.forEach(field -> field.setFieldType(FieldType.Form)))
                .orElse(Collections.emptyList());
        List<CmsMenuField> allFieldList = Stream.of(searchFieldList, columnFieldList, formFieldList)
                .flatMap(List::stream)
                .peek(field -> {
                    field.setMenuId(form.getId());
                    field.setCreateId(BusiContextUtil.getAdminId());
                    field.setCreateTime(new Date());
                })
                .toList();
        if (!allFieldList.isEmpty()) {
            menuFieldMapper.batchInsert(allFieldList);
        }

        BusiTransactionUtil.execAfterCommit(() -> LocalCache.notifyReload(LocalCacheEnums.MenuManager));
        return exist;
    }

    /**
     * 删除菜单
     */
    @Transactional
    public void deleteMenu(Long menuId) {
        Assert.notNull(menuId, "菜单ID不能为空");
        CmsMenu exist = menuMapper.selectByPrimaryKey(menuId);
        Assert.notNull(exist, "菜单不存在");
        if (ObjUtil.equal(exist.getCreateId(), 0L)) {
            throw new BusiException(ErrorCode.LOGIC_ERROR, "系统菜单,不可直接删除");
        }

        // 检查是否还有下级菜单
        List<CmsMenu> children = menuMapper.listByParentId(menuId);
        Assert.empty(children, "包含下级菜单,请先删除下级菜单");

        menuMapper.deleteByPrimaryKey(menuId);
        menuFieldMapper.deleteByMenuId(menuId);

        BusiTransactionUtil.execAfterCommit(() -> LocalCache.notifyReload(LocalCacheEnums.MenuManager));
    }


}
