package com.devvv.cms.controller.sys;

import com.devvv.cms.service.sys.SettingsService;
import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.response.Result;
import com.devvv.commons.manager.sys.dao.sys.entity.SysSetting;
import com.devvv.commons.manager.sys.dao.sys.entity.SysSettingHistory;
import com.devvv.commons.manager.sys.models.form.SettingsEditForm;
import com.devvv.commons.manager.sys.models.form.SettingsQueryForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by WangSJ on 2024/07/22
 */
@Tag(name = "111-系统配置")
@RestController
@RequestMapping("/cmsApi/sys/setting")
public class SettingController {

    @Resource
    private SettingsService settingsService;


    @Operation(summary = "01-查询所有系统配置")
    @PostMapping("/listAll")
    public Result<PageVO<SysSetting>> querySettingsList(@RequestBody SettingsQueryForm form) {
        return Result.success(settingsService.querySettingsList(form));
    }

    @Operation(summary = "02-添加系统配置")
    @PostMapping("/createSetting")
    public Result<Void> createSetting(@RequestBody SettingsEditForm form) {
        settingsService.createSetting(form);
        return Result.success();
    }

    @Operation(summary = "03-修改系统配置")
    @PostMapping("/updateSetting")
    public Result<Void> updateSetting(@RequestBody SettingsEditForm form) {
        settingsService.updateSetting(form);
        return Result.success();
    }

    @Operation(summary = "04-通知更新系统配置")
    @PostMapping("/syncSetting")
    public Result<Void> syncSetting(@RequestBody SettingsEditForm form) {
        settingsService.syncSetting(form);
        return Result.success();
    }

    @Operation(summary = "11-查询历史记录")
    @PostMapping("/listHistory")
    public Result<PageVO<SysSettingHistory>> listHistory(@RequestBody SettingsQueryForm form) {
        return Result.success(settingsService.listHistory(form));
    }
}
