package com.devvv.cms.dao.cms.mapper;

import com.devvv.cms.dao.cms.entity.CmsRole;
import com.devvv.cms.models.form.RoleQueryForm;
import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Table(DB = DBType.cms)
public interface CmsRoleMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CmsRole record);

    int insertSelective(CmsRole record);

    CmsRole selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CmsRole record);

    int updateByPrimaryKey(CmsRole record);

    /**
     * 查询所有角色
     */
    List<CmsRole> selectAll();

    /**
     * 根据角色code查询
     */
    CmsRole getByCode(@Param("roleCode") String roleCode);

    /**
     * 查询列表
     */
    List<CmsRole> listByParam(RoleQueryForm form);

    /**
     * 根据用户ID关联查询
     */
    List<CmsRole> listByAdminId(@Param("adminId") Long adminId);
}