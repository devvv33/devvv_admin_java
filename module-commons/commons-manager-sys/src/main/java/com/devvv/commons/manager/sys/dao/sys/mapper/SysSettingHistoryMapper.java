package com.devvv.commons.manager.sys.dao.sys.mapper;

import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import com.devvv.commons.manager.sys.dao.sys.entity.SysSettingHistory;
import com.devvv.commons.manager.sys.models.form.SettingsQueryForm;

import java.util.List;

@Table(DB = DBType.sys)
public interface SysSettingHistoryMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SysSettingHistory record);

    int insertSelective(SysSettingHistory record);

    SysSettingHistory selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysSettingHistory record);

    int updateByPrimaryKey(SysSettingHistory record);

    /**
     * 查询历史记录
     */
    List<SysSettingHistory> listByParam(SettingsQueryForm form);
}