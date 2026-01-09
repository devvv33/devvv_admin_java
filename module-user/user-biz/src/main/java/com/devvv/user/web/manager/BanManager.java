package com.devvv.user.web.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.devvv.commons.common.constant.GlobalConstant;
import com.devvv.commons.common.enums.user.BanType;
import com.devvv.commons.common.enums.user.TargetType;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.common.response.ErrorCode;
import com.devvv.commons.common.utils.MyStrUtil;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.lock.BusiRedissonLockUtil;
import com.devvv.user.web.dao.entity.TBan;
import com.devvv.user.web.dao.entity.TBanLog;
import com.devvv.user.web.dao.mapper.TBanLogMapper;
import com.devvv.user.web.dao.mapper.TBanMapper;
import com.devvv.user.web.models.convert.BanConvert;
import com.devvv.user.api.models.dto.BanDTO;
import com.devvv.user.api.models.dto.UnbanDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Supplier;

/**
 * Create by WangSJ on 2024/07/08
 */
@Slf4j
@Component
public class BanManager {
    @Resource
    private TBanMapper banMapper;
    @Resource
    private TBanLogMapper banLogMapper;


    /**
     * 获取封禁记录
     */
    public TBan getBan(TargetType targetType, Object targetValue, BanType banType) {
        if (targetType == null || targetValue == null || banType == null) {
            return null;
        }
        TBan ban = banMapper.selectByPrimaryKey(new TBan(targetType, targetValue.toString(), banType));
        // 封禁过期清理
        if (ban != null && ban.getEndTime().getTime() < System.currentTimeMillis()) {
            banMapper.deleteByPrimaryKey(ban);
            return null;
        }
        return ban;
    }

    /**
     * 检查封禁，并抛出异常
     */
    public void checkBan(TBan ban) {
        if (ban == null) {
            return;
        }
        // 提示信息
        String reason = ObjectUtil.defaultIfBlank(ban.getReason(), (Supplier<? extends String>) () -> buildReason(ban));
        // 抛出异常
        throw new BusiException(ErrorCode.LOGIN_BAN, reason);
    }

    /**
     * 封禁
     */
    public void ban(BanDTO form) {
        MyStrUtil.trimStringFields(form);
        Assert.isFalse(form.getBanType().isUnban(), "banType必须是封禁类型");
        TBan ban = BanConvert.INSTANCT.banDto_banEntity(form);
        ban.setCreateBy(BusiContextUtil.getAdminId());
        ban.setCreateTime(new Date());
        // 加锁
        BusiRedissonLockUtil.lockObject(StrUtil.format("{}_{}", ban.getTargetType(), ban.getTargetValue()));

        // 限制最大封禁时间
        if (form.getEndTime().getTime() > GlobalConstant.MAX_TIME.getTime()) {
            form.setEndTime(GlobalConstant.MAX_TIME);
        }
        // 处理提示信息
        ban.setReason(ObjectUtil.defaultIfBlank(ban.getReason(), (Supplier<? extends String>) () -> buildReason(ban)));

        // 最新封禁记录
        TBan existBan = banMapper.selectByPrimaryKey(ban);
        if (existBan == null) {
            banMapper.insert(ban);
        } else if (existBan.getEndTime().getTime() < ban.getEndTime().getTime()) {
            banMapper.updateByPrimaryKey(ban);
        }

        // 记录日志
        TBanLog banLog = BanConvert.INSTANCT.banEntity_banLog(ban);
        banLogMapper.insert(banLog);
    }

    /**
     * 构建 封禁提示信息
     */
    private String buildReason(TBan ban) {
        String dateStr = DateUtil.format(ban.getEndTime(), "yyyy年MM月dd日");
        return switch (ban.getBanType()) {
            case BAN_LOGIN -> StrUtil.format("账号违规已被封禁至:{}", dateStr);
            default -> ban.getBanType().getDesc();
        };
    }

    /**
     * 解除封禁
     */
    public void unban(UnbanDTO form) {
        MyStrUtil.trimStringFields(form);
        Assert.isTrue(form.getBanType().isUnban(), "banType必须是解封类型");
        TBan unban = BanConvert.INSTANCT.unbanDto_banEntity(form);
        // 加锁
        BusiRedissonLockUtil.lockObject(StrUtil.format("{}_{}", unban.getTargetType(), unban.getTargetValue()));

        // 删除封禁记录
        banMapper.deleteByPrimaryKey(new TBan(unban.getTargetType(), unban.getTargetValue(), unban.getBanType().getBanTypeRef()));

        // 记录日志
        TBanLog banLog = BanConvert.INSTANCT.banEntity_banLog(unban);
        banLog.setCreateBy(BusiContextUtil.getAdminId());
        banLog.setCreateTime(new Date());
        banLogMapper.insert(banLog);
    }

}
