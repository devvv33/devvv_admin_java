package com.devvv.cms.dao.cms.mapper;

import com.devvv.cms.dao.cms.entity.CmsAdminUserRole;
import com.devvv.cms.dao.cms.entity.CmsRole;
import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Table(DB = DBType.cms)
public interface CmsAdminUserRoleMapper {
    int deleteByPrimaryKey(@Param("adminId") Long adminId, @Param("roleId") Long roleId);

    int insert(CmsAdminUserRole record);

    int insertSelective(CmsAdminUserRole record);

    CmsAdminUserRole selectByPrimaryKey(@Param("adminId") Long adminId, @Param("roleId") Long roleId);

    int updateByPrimaryKeySelective(CmsAdminUserRole record);

    int updateByPrimaryKey(CmsAdminUserRole record);

    /**
     * 根据用户id, 查询角色
     */
    List<CmsRole> listRoleByAdminId(@Param("adminId") Long adminId);


    /**
     * 删除角色映射
     */
    void deleteByAdminId(@Param("adminId") Long adminId);

    /**
     * 批量插入
     */
    void insertBatch(List<CmsAdminUserRole> list);

}