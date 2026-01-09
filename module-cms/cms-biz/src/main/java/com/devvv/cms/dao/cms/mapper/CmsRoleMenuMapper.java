package com.devvv.cms.dao.cms.mapper;

import com.devvv.cms.dao.cms.entity.CmsRoleMenu;

import java.util.List;

import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Param;

@Table(DB = DBType.cms)
public interface CmsRoleMenuMapper {
    int deleteByPrimaryKey(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    int insert(CmsRoleMenu record);

    int insertSelective(CmsRoleMenu record);

    CmsRoleMenu selectByPrimaryKey(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    int updateByPrimaryKeySelective(CmsRoleMenu record);

    int updateByPrimaryKey(CmsRoleMenu record);

    int batchInsert(@Param("list") List<CmsRoleMenu> list);

    /**
     * 删除角色下的所有菜单
     */
    void deleteByRoleId(@NotNull @Param("roleId") Long roleId);

    /**
     * 根据角色ID查询菜单ID
     */
    List<Long> listMenuIdByRoleId(@NotNull @Param("roleId") Long roleId);
}