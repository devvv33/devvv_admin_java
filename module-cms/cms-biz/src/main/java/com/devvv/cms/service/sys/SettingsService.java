package com.devvv.cms.service.sys;

import cn.hutool.core.lang.Assert;
import com.github.pagehelper.PageHelper;
import com.devvv.cms.manager.AdminUserManager;
import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.utils.MyStrUtil;
import com.devvv.commons.core.config.cache.local.LocalCache;
import com.devvv.commons.core.config.cache.local.LocalCacheEnums;
import com.devvv.commons.manager.sys.dao.sys.entity.SysSetting;
import com.devvv.commons.manager.sys.dao.sys.entity.SysSettingHistory;
import com.devvv.commons.manager.sys.dao.sys.mapper.SysSettingHistoryMapper;
import com.devvv.commons.manager.sys.dao.sys.mapper.SysSettingMapper;
import com.devvv.commons.manager.sys.manager.SettingManager;
import com.devvv.commons.manager.sys.models.form.SettingsEditForm;
import com.devvv.commons.manager.sys.models.form.SettingsQueryForm;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Create by WangSJ on 2024/06/20
 */
@Slf4j
@Service
public class SettingsService {

    @Resource
    private SettingManager settingManager;
    @Resource
    private SysSettingMapper settingMapper;
    @Resource
    private SysSettingHistoryMapper historyMapper;



    /**
     * 全局参数
     */
    public PageVO<SysSetting> querySettingsList(SettingsQueryForm form) {
        MyStrUtil.trimStringFields(form);
        PageHelper.startPage(form.getPageNum(), form.getPageSize());
        List<SysSetting> list = settingMapper.listByParam(form);
        return PageVO.build(list);
    }

    @Transactional
    public void createSetting(SettingsEditForm form) {
        Assert.notBlank(form.getKey(), "key不能为空");
        form.setChangeRemark("添加配置");
        settingManager.saveSetting(form);
    }

    @Transactional
    public void updateSetting(SettingsEditForm form) {
        Assert.notBlank(form.getKey(), "key不能为空");
        settingManager.saveSetting(form);
    }

    /**
     * 通知更新系统配置
     */
    public void syncSetting(SettingsEditForm form) {
        // 发送Redis广播，通知所有模块更新缓存
        LocalCache.notifyReload(LocalCacheEnums.SettingManager, form.getKey());
    }

    /**
     * 查询历史记录
     */
    public PageVO<SysSettingHistory> listHistory(SettingsQueryForm form) {
        MyStrUtil.trimStringFields(form);
        PageHelper.startPage(form.getPageNum(), form.getPageSize());
        List<SysSettingHistory> list = historyMapper.listByParam(form);
        list.forEach(item->{
            item.setChangeAdminName(AdminUserManager.getInstance().getAdminName(item.getChangeAdminId()));
        });
        return PageVO.build(list);
    }
}
