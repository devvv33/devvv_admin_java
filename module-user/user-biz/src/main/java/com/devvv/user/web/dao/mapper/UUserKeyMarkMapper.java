package com.devvv.user.web.dao.mapper;

import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import com.devvv.user.web.dao.entity.UUserKeyMark;

@Table(DB = DBType.user)
public interface UUserKeyMarkMapper {
    int deleteByPrimaryKey(UUserKeyMark record);

    int insert(UUserKeyMark record);

    int insertSelective(UUserKeyMark record);

    UUserKeyMark selectByPrimaryKey(UUserKeyMark record);

    int updateByPrimaryKeySelective(UUserKeyMark record);

    int updateByPrimaryKey(UUserKeyMark record);
}