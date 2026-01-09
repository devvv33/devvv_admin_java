package com.devvv.user.web.models.convert;

import com.devvv.user.api.models.dto.BanLogDTO;
import com.devvv.user.web.dao.entity.TBan;
import com.devvv.user.web.dao.entity.TBanLog;
import com.devvv.user.api.models.dto.BanDTO;
import com.devvv.user.api.models.dto.UnbanDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Create by WangSJ on 2024/07/08
 */
@Mapper
public interface BanConvert {
    BanConvert INSTANCT = Mappers.getMapper(BanConvert.class);

    TBan banDto_banEntity(BanDTO dto);
    BanDTO banEntity_banDto(TBan record);
    TBanLog banEntity_banLog(TBan record);

    TBan unbanDto_banEntity(UnbanDTO form);

    BanLogDTO banLogEntity_banLogDto(TBanLog record);

}
