package com.devvv.user.web.dao.mapper;

import com.devvv.commons.common.enums.DBType;
import com.devvv.commons.core.config.datasource.annotation.Table;
import com.devvv.user.web.dao.entity.UUserMobile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Table(DB = DBType.user)
public interface UUserMobileMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(UUserMobile record);

    int insertSelective(UUserMobile record);

    UUserMobile selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(UUserMobile record);

    int updateByPrimaryKey(UUserMobile record);

    /**
     * 根据手机号查询记录
     */
    UUserMobile getByMobile(@Param("mobile") String mobile);

    List<UUserMobile> listAll();
}