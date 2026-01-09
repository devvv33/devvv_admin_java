package com.devvv.commons.manager.sys.dao.sys.mapper;

import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import com.devvv.commons.manager.sys.dao.sys.entity.SysSetting;
import com.devvv.commons.manager.sys.models.form.SettingsQueryForm;

import java.util.List;

@Table(DB = DBType.sys)
public interface SysSettingMapper {
    int deleteByPrimaryKey(String key);

    int insert(SysSetting record);

    int insertSelective(SysSetting record);

    SysSetting selectByPrimaryKey(String key);

    int updateByPrimaryKeySelective(SysSetting record);

    int updateByPrimaryKey(SysSetting record);

    /**
     * 获取所有配置
     */
    List<SysSetting> listAll();

    /**
     * 列表查詢
     */
    List<SysSetting> listByParam(SettingsQueryForm form);
}