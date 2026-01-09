package com.devvv.user.web.dao.mapper;

import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import com.devvv.user.api.models.dto.UserPageQueryDTO;
import com.devvv.user.web.dao.entity.UUserBasic;

import java.util.List;

@Table(DB = DBType.user)
public interface UUserBasicMapper {
    int deleteByPrimaryKey(UUserBasic record);

    int insert(UUserBasic record);

    int insertSelective(UUserBasic record);

    UUserBasic selectByPrimaryKey(UUserBasic record);

    int updateByPrimaryKeySelective(UUserBasic record);

    int updateByPrimaryKey(UUserBasic record);


    /**
     * 分页查询用户列表
     */
    List<UUserBasic> pageList(UserPageQueryDTO pageQuery);

}