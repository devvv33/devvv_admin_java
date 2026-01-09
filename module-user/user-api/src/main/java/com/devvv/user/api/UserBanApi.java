package com.devvv.user.api;

import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.enums.user.BanType;
import com.devvv.commons.common.enums.user.TargetType;
import com.devvv.commons.common.response.Result;
import com.devvv.user.api.models.constants.ApiConstants;
import com.devvv.user.api.models.dto.BanDTO;
import com.devvv.user.api.models.dto.BanLogDTO;
import com.devvv.user.api.models.dto.UnbanDTO;
import com.devvv.user.api.models.dto.UserBanPageQueryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Create by WangSJ on 2024/07/08
 */
@FeignClient(name = ApiConstants.USER_WEB)
public interface UserBanApi {

    /**
     * 封禁
     */
    @PostMapping("/inner/user/ban")
    Result ban(@RequestBody BanDTO banDTO);

    /**
     * 解除封禁
     */
    @PostMapping("/inner/user/unban")
    Result unban(@RequestBody UnbanDTO unbanDTO);

    /**
     * 获取封禁信息
     * 如果未被封禁，或已过封禁期，将返回null
     */
    @PostMapping("/inner/user/getBan")
    Result<BanDTO> getBan(@RequestParam("targetType") TargetType targetType,
                          @RequestParam("targetValue") String targetValue,
                          @RequestParam("banType") BanType banType);

    /**
     * 检查是否被封禁
     * 如果被封禁，将会返回异常状态码
     */
    @PostMapping("/inner/user/checkBan")
    Result<Void> checkBan(@RequestParam("targetType") TargetType targetType,
                    @RequestParam("targetValue") String targetValue,
                    @RequestParam("banType") BanType banType);


    /**
     * 获取当前有效的封禁列表
     */
    @PostMapping("/inner/user/ban/pageList")
    Result<PageVO<BanDTO>> pageList(@RequestBody UserBanPageQueryDTO pageQuery);

    /**
     * 获取封禁历史记录
     */
    @PostMapping("/inner/user/ban/log/pageList")
    Result<PageVO<BanLogDTO>> banLogPageList(@RequestBody UserBanPageQueryDTO pageQuery);
}
