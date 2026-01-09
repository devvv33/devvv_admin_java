package com.devvv.cms.controller.user;

import com.devvv.cms.models.bo.Option;
import com.devvv.cms.models.form.user.CmsBanUserForm;
import com.devvv.cms.models.vo.UserBanVO;
import com.devvv.cms.service.user.CmsUserBanService;
import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.enums.user.BanType;
import com.devvv.commons.common.response.Result;
import com.devvv.user.api.models.dto.UserBanPageQueryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Create by WangSJ on 2024/07/08
 */
@Tag(name = "202-封禁管理")
@RestController
@RequestMapping("/cmsApi/user/ban")
public class CmsUserBanController {

    @Resource
    private CmsUserBanService userBanService;

    @Operation(summary = "01-分页查询封禁列表")
    @PostMapping("/pageList")
    public Result<PageVO<UserBanVO>> pageList(@Valid @RequestBody UserBanPageQueryDTO param) {
        PageVO<UserBanVO> page = userBanService.pageList(param);
        return Result.success(page);
    }

    @Operation(summary = "02-批量封禁")
    @PostMapping("/ban")
    public Result ban(@Valid @RequestBody CmsBanUserForm param) {
        userBanService.ban(param);
        return Result.success();
    }

    @Operation(summary = "03-批量解封")
    @PostMapping("/unban")
    public Result unban(@Valid @RequestBody CmsBanUserForm param) {
        userBanService.unban(param);
        return Result.success();
    }

    @Operation(summary = "04-批量解封-自动转换解封类型")
    @PostMapping("/autoUnban")
    public Result autoUnban(@Valid @RequestBody CmsBanUserForm param) {
        // 按照约定，此处传过来的banType是封禁类型，所以需要自动转换
        Arrays.stream(BanType.values())
                .filter(BanType::isUnban)
                .filter(enm -> enm.getBanTypeRef() == param.getBanType())
                .findAny()
                .ifPresent(param::setBanType);
        userBanService.unban(param);
        return Result.success();
    }

    @Operation(summary = "11-封禁选项")
    @PostMapping("/banTypes")
    public Result banTypes() {
        List<Option> banTypes = Arrays.stream(BanType.values())
                .filter(enm -> !enm.isUnban())
                .map(enm -> new Option(enm.name(), enm.getDesc()))
                .toList();
        return Result.success(banTypes);
    }
    @Operation(summary = "12-解封选项")
    @PostMapping("/unbanTypes")
    public Result unbanTypes() {
        List<Option> banTypes = Arrays.stream(BanType.values())
                .filter(BanType::isUnban)
                .map(enm -> new Option(enm.name(), enm.getDesc()))
                .toList();
        return Result.success(banTypes);
    }

    @Operation(summary = "21-分页查询封禁日志")
    @PostMapping("/banLogPageList")
    public Result<PageVO<UserBanVO>> banLogPageList(@Valid @RequestBody UserBanPageQueryDTO param) {
        PageVO<UserBanVO> page = userBanService.banLogPageList(param);
        return Result.success(page);
    }
}
