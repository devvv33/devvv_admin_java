package com.devvv.cms.dao.cms.mapper;

import com.devvv.cms.dao.cms.entity.CmsAdminUser;
import com.devvv.cms.models.form.AdminQueryForm;
import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;

import java.util.List;

@Table(DB = DBType.cms)
public interface CmsAdminUserMapper {
    int deleteByPrimaryKey(Long adminId);

    int insert(CmsAdminUser record);

    int insertSelective(CmsAdminUser record);

    CmsAdminUser selectByPrimaryKey(Long adminId);

    int updateByPrimaryKeySelective(CmsAdminUser record);

    int updateByPrimaryKey(CmsAdminUser record);

    /**
     * 查询所有用户
     */
    List<CmsAdminUser> listAll();

    /**
     * 列表查询
     */
    List<CmsAdminUser> listByParams(AdminQueryForm form);
}