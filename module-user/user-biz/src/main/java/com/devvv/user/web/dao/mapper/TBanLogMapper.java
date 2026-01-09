package com.devvv.user.web.dao.mapper;

import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import com.devvv.user.api.models.dto.UserBanPageQueryDTO;
import com.devvv.user.web.dao.entity.TBanLog;

import java.util.List;

@Table(DB = DBType.user)
public interface TBanLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TBanLog record);

    int insertSelective(TBanLog record);

    TBanLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TBanLog record);

    int updateByPrimaryKey(TBanLog record);

    /**
     * 分页查询
     */
    List<TBanLog> pageList(UserBanPageQueryDTO pageQuery);
}