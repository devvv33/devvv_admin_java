package com.devvv.cms.dao.cms.mapper;

import com.devvv.cms.dao.cms.entity.CmsDepartment;
import com.devvv.cms.models.form.DeptQueryForm;
import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Table(DB = DBType.cms)
public interface CmsDepartmentMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CmsDepartment record);

    int insertSelective(CmsDepartment record);

    CmsDepartment selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CmsDepartment record);

    int updateByPrimaryKey(CmsDepartment record);

    /**
     * 查询所有部门
     */
    List<CmsDepartment> listAll();

    /**
     * 多条件查询
     */
    List<CmsDepartment> listByParam(DeptQueryForm form);

    /**
     * 根据部门code查询部门
     */
    CmsDepartment getByCode(@Param("deptCode") String deptCode);

    /**
     * 根据父级id查询子部门
     */
    List<CmsDepartment> listByParentId(@Param("parentId") Long parentId);
}