package com.devvv.commons.manager.sys.manager;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.common.response.ErrorCode;
import com.devvv.commons.common.utils.CommonUtil;
import com.devvv.commons.common.utils.MyOpt;
import com.devvv.commons.common.utils.MyStrUtil;
import com.devvv.commons.core.config.cache.local.LocalCache;
import com.devvv.commons.core.config.cache.local.LocalCacheEnums;
import com.devvv.commons.core.config.cache.local.LocalCacheFactory;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.utils.BusiTransactionUtil;
import com.devvv.commons.manager.sys.dao.sys.entity.SysSetting;
import com.devvv.commons.manager.sys.dao.sys.entity.SysSettingHistory;
import com.devvv.commons.manager.sys.dao.sys.mapper.SysSettingHistoryMapper;
import com.devvv.commons.manager.sys.dao.sys.mapper.SysSettingMapper;
import com.devvv.commons.manager.sys.models.form.SettingsEditForm;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Create by WangSJ on 2024/07/22
 */
@Slf4j
@Component
public class SettingManager implements LocalCache{

    @Resource
    private SysSettingMapper settingMapper;
    @Resource
    private SysSettingHistoryMapper settingHistoryMapper;


    private List<SysSetting> LIST_ALL = new ArrayList<>();
    private Map<String, String> KEY_MAP = new HashMap<>();

    @Override
    public void init() {
        LIST_ALL = SpringUtil.getBean(SysSettingMapper.class).listAll();
        KEY_MAP = LIST_ALL.stream()
                .collect(Collectors.toMap(SysSetting::getKey, SysSetting::getValue, (v1, v2) -> v2));
    }

    @Override
    public void reload(List<String> keys) {
        init();
    }

    // 注册完成后即可进行实例化
    @PostConstruct
    public void postConstruct(){
        getInstance();
    }

    /**
     * 提供静态方法，获取实例
     */
    public static SettingManager getInstance() {
        return LocalCacheFactory.getInstance(LocalCacheEnums.SettingManager, true);
    }


    /**
     * 修改配置
     */
    public void saveSetting(SettingsEditForm form) {
        Assert.notBlank(form.getKey(), "key不能为空");
        MyStrUtil.trimStringFields(form);
        SysSetting exist = settingMapper.selectByPrimaryKey(form.getKey());
        SysSetting newSetting;
        if (exist == null) {
            Integer sort = Opt.ofNullable(form.getSort())
                    .orElseGet(() -> LIST_ALL.stream().map(SysSetting::getSort).max(Comparator.comparingInt(v -> v)).orElse(0) + 10);
            newSetting = SysSetting.builder()
                    .key(form.getKey())
                    .value(form.getValue())
                    .readOnly(false)
                    .sort(sort)
                    .remark(form.getRemark())
                    .version(1)
                    .build();
            settingMapper.insertSelective(newSetting);
        } else {
            // 如果是只读的，不可被修改
            Assert.notEquals(exist.getReadOnly(), true, "系统内置配置，不可修改");
            // 如果旧值是json，新值不是json，阻止修改， 目的是为了保证json格式正确
            // 如果确有需要改成非json的，可直接修改数据库，然后在CMS重新点下编辑 触发redis变更广播即可
            if (checkIsJson(exist.getValue()) && !checkIsJson(form.getValue())) {
                throw new BusiException(ErrorCode.PARAM_ERR, "Json格式错误，请检查修正");
            }

            newSetting = SysSetting.builder()
                    .key(form.getKey())
                    .value(form.getValue())
                    .readOnly(exist.getReadOnly())
                    .sort(form.getSort())
                    .remark(form.getRemark())
                    .version(exist.getVersion() + 1)
                    .build();
            settingMapper.updateByPrimaryKeySelective(newSetting);
        }

        SysSettingHistory his = SysSettingHistory.builder()
                .key(newSetting.getKey())
                .value(newSetting.getValue())
                .readOnly(newSetting.getReadOnly())
                .remark(newSetting.getRemark())
                .sort(newSetting.getSort())
                .version(newSetting.getVersion())
                .changeRemark(form.getChangeRemark())
                .changeAdminId(BusiContextUtil.getAdminId())
                .changeTime(new Date())
                .build();
        settingHistoryMapper.insertSelective(his);

        // 更新后，发送Redis广播，通知所有模块更新缓存
        BusiTransactionUtil.execAfterCommit(() -> LocalCache.notifyReload(LocalCacheEnums.SettingManager, form.getKey()));
    }
    private boolean checkIsJson(String string) {
        if (!JSONUtil.isTypeJSON(string)) {
            return false;
        }
        try {
            JSONObject.parseObject(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取配置
     */
    public String getString(String key) {
        return Opt.ofBlankAble(key)
                .map(KEY_MAP::get)
                .orElse(null);
    }

    public Integer getInteger(String key) {
        return MyOpt.ofNullable(getString(key))
                .tryMap(Integer::valueOf)
                .orElse(null);
    }
    public int getInt(String key) {
        return MyOpt.ofNullable(getString(key))
                .tryMap(Integer::valueOf)
                .orElse(0);
    }
    public Long getLong(String key) {
        return MyOpt.ofNullable(getString(key))
                .tryMap(Long::valueOf)
                .orElse(null);
    }
    public Double getDouble(String key) {
        return MyOpt.ofNullable(getString(key))
                .tryMap(Double::valueOf)
                .orElse(null);
    }
    public Boolean getBoolean(String key) {
        return MyOpt.ofNullable(getString(key))
                .tryMap(BooleanUtil::toBoolean)
                .orElse(null);
    }

    public List<String> getStringList(String key) {
        return CommonUtil.splitStringList(getString(key));
    }

    public JSONObject getJson(String key) {
        return MyOpt.ofNullable(getString(key))
                .tryMap(JSONObject::parseObject)
                .orElseGet(JSONObject::new);
    }

}
