package com.devvv.cms.service.user;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import com.devvv.cms.manager.AdminUserManager;
import com.devvv.cms.models.convert.UserApiConvert;
import com.devvv.cms.models.form.user.CmsBanUserForm;
import com.devvv.cms.models.vo.UserBanVO;
import com.devvv.commons.common.constant.GlobalConstant;
import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.enums.user.BanType;
import com.devvv.commons.common.enums.user.TargetType;
import com.devvv.commons.common.utils.CommonUtil;
import com.devvv.user.api.UserBanApi;
import com.devvv.user.api.models.dto.BanDTO;
import com.devvv.user.api.models.dto.BanLogDTO;
import com.devvv.user.api.models.dto.UnbanDTO;
import com.devvv.user.api.models.dto.UserBanPageQueryDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create by WangSJ on 2024/07/08
 */
@Slf4j
@Service
public class CmsUserBanService {

    @Resource
    private UserBanApi userBanApi;
    @Resource
    private AdminUserManager adminUserManager;


    /**
     * 分页查询封禁列表
     */
    public PageVO<UserBanVO> pageList(UserBanPageQueryDTO param) {
        PageVO<BanDTO> dtoPage = userBanApi.pageList(param).ifFailThrow();
        PageVO<UserBanVO> voPage = dtoPage.convertTo(dto -> {
            UserBanVO vo = UserApiConvert.INSTANCT.banDTO_banVO(dto);
            Opt.ofNullable(vo.getTargetType()).map(TargetType::getDesc).ifPresent(vo::setTargetTypeStr);
            Opt.ofNullable(vo.getBanType()).map(BanType::getDesc).ifPresent(vo::setBanTypeStr);
            Opt.ofNullable(vo.getCreateBy()).map(adminUserManager::getAdminName).ifPresent(vo::setCreateName);
            return vo;
        });
        return voPage;
    }

    /**
     * 批量封禁
     */
    public void ban(CmsBanUserForm param) {
        Assert.notNull(param.getBanSecond(), "封禁时间不能为空");
        Date startTime = new Date();
        Date endTime = param.getBanSecond() == -1 ? GlobalConstant.MAX_TIME : DateUtil.offsetSecond(startTime, param.getBanSecond().intValue());

        // 此处仅是为了示例CMS页面按钮组，才用了批量封禁
        // 正式使用时可调整至单用户封禁
        List<String> targets = CommonUtil.splitStringList(param.getTargetValue());
        for (String target : targets) {
            BanDTO dto = BanDTO.builder()
                    .targetType(param.getTargetType())
                    .targetValue(target)
                    .banType(param.getBanType())
                    .startTime(startTime)
                    .endTime(endTime)
                    .reason(param.getReason())
                    .remark(param.getRemark())
                    .build();
            userBanApi.ban(dto).ifFailThrow();
        }
    }

    /**
     * 批量解封
     */
    public void unban(CmsBanUserForm param) {
        List<String> targets = CommonUtil.splitStringList(param.getTargetValue());
        for (String target : targets) {
            UnbanDTO dto = UnbanDTO.builder()
                    .targetType(param.getTargetType())
                    .targetValue(target)
                    .banType(param.getBanType())
                    .remark(param.getRemark())
                    .build();
            userBanApi.unban(dto).ifFailThrow();
        }
    }

    /**
     * 分页获取封禁历史记录
     */
    public PageVO<UserBanVO> banLogPageList(UserBanPageQueryDTO param) {
        PageVO<BanLogDTO> dtoPage = userBanApi.banLogPageList(param).ifFailThrow();

        // 抽取所有adminId
        Set<Long> adminIds = dtoPage.getList().stream().map(BanLogDTO::getCreateBy).collect(Collectors.toSet());


        PageVO<UserBanVO> voPage = dtoPage.convertTo(dto -> {
            UserBanVO vo = UserApiConvert.INSTANCT.banLogDTO_banVO(dto);
            Opt.ofNullable(vo.getTargetType()).map(TargetType::getDesc).ifPresent(vo::setTargetTypeStr);
            Opt.ofNullable(vo.getBanType()).map(BanType::getDesc).ifPresent(vo::setBanTypeStr);
            Opt.ofNullable(vo.getCreateBy()).map(adminUserManager::getAdminName).ifPresent(vo::setCreateName);
            return vo;
        });
        return voPage;
    }
}
