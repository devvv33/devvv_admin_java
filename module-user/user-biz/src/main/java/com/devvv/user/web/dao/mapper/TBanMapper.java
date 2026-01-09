package com.devvv.user.web.dao.mapper;

import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import com.devvv.user.api.models.dto.UserBanPageQueryDTO;
import com.devvv.user.web.dao.entity.TBan;

import java.util.List;

@Table(DB = DBType.user)
public interface TBanMapper {
    int deleteByPrimaryKey(TBan record);

    int insert(TBan record);

    int insertSelective(TBan record);

    TBan selectByPrimaryKey(TBan record);

    int updateByPrimaryKeySelective(TBan record);

    int updateByPrimaryKey(TBan record);

    /**
     * 分页查询
     */
    List<TBan> pageList(UserBanPageQueryDTO pageQuery);
}