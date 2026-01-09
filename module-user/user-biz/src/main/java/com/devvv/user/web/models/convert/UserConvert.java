package com.devvv.user.web.models.convert;

import com.devvv.user.api.models.dto.UserDTO;
import com.devvv.user.web.dao.entity.UUserBasic;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Create by WangSJ on 2024/06/26
 */
@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    UserDTO userEntity_DTO(UUserBasic userBasic);
}
