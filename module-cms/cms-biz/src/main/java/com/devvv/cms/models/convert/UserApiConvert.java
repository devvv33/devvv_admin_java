package com.devvv.cms.models.convert;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import com.devvv.cms.models.form.user.QueryUserForm;
import com.devvv.cms.models.vo.UserBanVO;
import com.devvv.cms.models.vo.UserVO;
import com.devvv.commons.common.constant.GlobalConstant;
import com.devvv.user.api.models.dto.BanDTO;
import com.devvv.user.api.models.dto.BanLogDTO;
import com.devvv.user.api.models.dto.UserDTO;
import com.devvv.user.api.models.dto.UserPageQueryDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Date;

/**
 * Create by WangSJ on 2024/06/26
 */
@Mapper(builder = @Builder(disableBuilder = true))
public interface UserApiConvert {
    UserApiConvert INSTANCT = Mappers.getMapper(UserApiConvert.class);

    // region 用户相关
    UserPageQueryDTO pageForm_DTO(QueryUserForm from);

    UserVO userDTO_userVO(UserDTO dto);
    // endregion


    // region 封禁相关
    @Mapping(target = "banTime", expression = "java(buildBanTime(dto.getStartTime(),dto.getEndTime()))")
    UserBanVO banDTO_banVO(BanDTO dto);
    @Mapping(target = "banTime", expression = "java(buildBanTime(dto.getStartTime(),dto.getEndTime()))")
    UserBanVO banLogDTO_banVO(BanLogDTO dto);

    default String buildBanTime(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        if (endTime.getTime() >= GlobalConstant.MAX_TIME.getTime()) {
            return "永久";
        }
        return DateUtil.formatBetween(endTime.getTime() - startTime.getTime(), BetweenFormatter.Level.SECOND);
    }
    // endregion
}
