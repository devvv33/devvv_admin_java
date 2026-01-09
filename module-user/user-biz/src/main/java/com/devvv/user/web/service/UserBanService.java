package com.devvv.user.web.service;

import cn.hutool.core.lang.Assert;
import com.github.pagehelper.PageHelper;
import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.enums.user.TargetType;
import com.devvv.commons.common.utils.MyStrUtil;
import com.devvv.commons.token.utils.StpUserUtil;
import com.devvv.user.api.models.dto.BanDTO;
import com.devvv.user.api.models.dto.UnbanDTO;
import com.devvv.user.api.models.dto.UserBanPageQueryDTO;
import com.devvv.user.web.dao.entity.TBan;
import com.devvv.user.web.dao.entity.TBanLog;
import com.devvv.user.web.dao.mapper.TBanLogMapper;
import com.devvv.user.web.dao.mapper.TBanMapper;
import com.devvv.user.web.manager.BanManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Create by WangSJ on 2024/07/08
 */
@Slf4j
@Service
public class UserBanService {

    @Resource
    private BanManager banManager;
    @Resource
    private TBanMapper banMapper;
    @Resource
    private TBanLogMapper banLogMapper;

    /**
     * 封禁登录
     */
    @Transactional
    public void ban(BanDTO form) {
        banManager.ban(form);

        // 踢出用户
        if (form.getTargetType() == TargetType.ACCOUNT) {
            StpUserUtil.kickout(Long.parseLong(form.getTargetValue()));
        }
    }

    /**
     * 解除-封禁登录
     */
    @Transactional
    public void unban(UnbanDTO form) {
        banManager.unban(form);
    }

    /**
     * 分页查询
     */
    public PageVO<TBan> pageList(UserBanPageQueryDTO pageQuery) {
        Assert.notNull(pageQuery.getPageNum(), "pageNum 不能为空");
        Assert.notNull(pageQuery.getPageSize(), "pageSize 不能为空");
        Assert.isTrue(pageQuery.getPageSize() <= 1000, "pageSize 不能超过1000");
        MyStrUtil.trimStringFields(pageQuery);

        // 分页查询
        PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
        List<TBan> list = banMapper.pageList(pageQuery);
        return PageVO.build(list);
    }

    /**
     * 分页查询 封禁历史记录
     */
    public PageVO<TBanLog> banLogPageList(UserBanPageQueryDTO pageQuery) {
        Assert.notNull(pageQuery.getPageNum(), "pageNum 不能为空");
        Assert.notNull(pageQuery.getPageSize(), "pageSize 不能为空");
        Assert.isTrue(pageQuery.getPageSize() <= 1000, "pageSize 不能超过1000");
        MyStrUtil.trimStringFields(pageQuery);

        // 分页查询
        PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
        List<TBanLog> list = banLogMapper.pageList(pageQuery);
        return PageVO.build(list);
    }
}
