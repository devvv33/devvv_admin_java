package com.devvv.user.web.inner;

import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.enums.user.BanType;
import com.devvv.commons.common.enums.user.TargetType;
import com.devvv.commons.common.response.Result;
import com.devvv.user.api.UserBanApi;
import com.devvv.user.api.models.dto.BanDTO;
import com.devvv.user.api.models.dto.BanLogDTO;
import com.devvv.user.api.models.dto.UnbanDTO;
import com.devvv.user.api.models.dto.UserBanPageQueryDTO;
import com.devvv.user.web.dao.entity.TBan;
import com.devvv.user.web.dao.entity.TBanLog;
import com.devvv.user.web.manager.BanManager;
import com.devvv.user.web.models.convert.BanConvert;
import com.devvv.user.web.service.UserBanService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by WangSJ on 2024/07/08
 */
@RestController
@Validated
public class UserBanImpl implements UserBanApi {

    @Resource
    private UserBanService userBanService;
    @Resource
    private BanManager banManager;

    /**
     * 封禁
     */
    @Override
    public Result ban(BanDTO banDTO) {
        userBanService.ban(banDTO);
        return Result.success();
    }

    /**
     * 解除封禁
     */
    @Override
    public Result unban(UnbanDTO unbanDTO) {
        userBanService.unban(unbanDTO);
        return Result.success();
    }

    /**
     * 获取封禁信息
     */
    @Override
    public Result<BanDTO> getBan(TargetType targetType, String targetValue, BanType banType) {
        TBan ban = banManager.getBan(targetType, targetValue, banType);
        BanDTO banDTO = BanConvert.INSTANCT.banEntity_banDto(ban);
        return Result.success(banDTO);
    }

    /**
     * 检查是否被封禁
     * 如果被封禁，将会返回异常状态码
     */
    @Override
    public Result<Void> checkBan(TargetType targetType, String targetValue, BanType banType) {
        TBan ban = banManager.getBan(targetType, targetValue, banType);
        banManager.checkBan(ban);
        return Result.success();
    }

    /**
     * 获取当前有效的封禁列表
     */
    @Override
    public Result<PageVO<BanDTO>> pageList(UserBanPageQueryDTO pageQuery) {
        PageVO<TBan> offsetPage = userBanService.pageList(pageQuery);
        PageVO<BanDTO> dtoPage = offsetPage.convertTo(BanConvert.INSTANCT::banEntity_banDto);
        return Result.success(dtoPage);
    }

    /**
     * 封禁历史记录
     */
    @Override
    public Result<PageVO<BanLogDTO>> banLogPageList(UserBanPageQueryDTO pageQuery) {
        PageVO<TBanLog> offsetPage = userBanService.banLogPageList(pageQuery);
        PageVO<BanLogDTO> dtoPage = offsetPage.convertTo(BanConvert.INSTANCT::banLogEntity_banLogDto);
        return Result.success(dtoPage);
    }
}
