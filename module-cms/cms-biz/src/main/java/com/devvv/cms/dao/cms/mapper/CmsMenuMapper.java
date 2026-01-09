package com.devvv.cms.dao.cms.mapper;

import com.devvv.cms.dao.cms.entity.CmsMenu;
import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Table(DB = DBType.cms)
public interface CmsMenuMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CmsMenu record);

    int insertSelective(CmsMenu record);

    CmsMenu selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CmsMenu record);

    int updateByPrimaryKey(CmsMenu record);

    /**
     * 查询所有
     */
    List<CmsMenu> listAll();

    /**
     * 根据父级id查询
     */
    List<CmsMenu> listByParentId(@Param("parentId") Long parentId);

    /**
     * 根据用户id查询
     */
    List<CmsMenu> listByAdminId(@Param("adminId") Long adminId);
}