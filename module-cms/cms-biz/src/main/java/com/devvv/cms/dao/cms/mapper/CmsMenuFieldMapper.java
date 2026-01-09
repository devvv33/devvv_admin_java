package com.devvv.cms.dao.cms.mapper;

import com.devvv.cms.dao.cms.entity.CmsMenuField;

import java.util.List;

import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import org.apache.ibatis.annotations.Param;

@Table(DB = DBType.cms)
public interface CmsMenuFieldMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CmsMenuField record);

    int insertSelective(CmsMenuField record);

    CmsMenuField selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CmsMenuField record);

    int updateByPrimaryKey(CmsMenuField record);

    int batchInsert(@Param("list") List<CmsMenuField> list);

    int batchInsertSelectiveUseDefaultForNull(@Param("list") List<CmsMenuField> list);

    /**
     * 查询所有
     */
    List<CmsMenuField> listAll();

    /**
     * 根据menuId删除
     */
    void deleteByMenuId(@Param("menuId") Long menuId);
}