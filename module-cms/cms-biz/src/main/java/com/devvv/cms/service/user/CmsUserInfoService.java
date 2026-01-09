package com.devvv.cms.service.user;

import cn.hutool.core.lang.Opt;
import com.devvv.cms.models.convert.UserApiConvert;
import com.devvv.cms.models.form.user.QueryUserForm;
import com.devvv.cms.models.vo.UserVO;
import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.enums.status.UserStatus;
import com.devvv.commons.common.enums.user.BanType;
import com.devvv.commons.common.enums.user.GenderType;
import com.devvv.commons.common.enums.user.TargetType;
import com.devvv.commons.core.busicode.BusiCode;
import com.devvv.commons.core.busicode.BusiCodeDefine;
import com.devvv.user.api.UserBanApi;
import com.devvv.user.api.UserInfoApi;
import com.devvv.user.api.models.dto.UserDTO;
import com.devvv.user.api.models.dto.UserPageQueryDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create by WangSJ on 2024/06/25
 */
@Slf4j
@Service
public class CmsUserInfoService {

    @Resource
    private UserInfoApi userInfoApi;
    @Resource
    private UserBanApi userBanApi;

    /**
     * 分页查询 用户列表
     */
    @BusiCode(value = BusiCodeDefine.LoginByMobile)
    @Transactional
    public PageVO<UserVO> pageList(QueryUserForm param) {
        UserPageQueryDTO queryDTO = UserApiConvert.INSTANCT.pageForm_DTO(param);
        PageVO<UserDTO> dtoPage = userInfoApi.pageList(queryDTO).ifFailThrow();
        PageVO<UserVO> voPage = dtoPage.convertTo(dto -> {
            UserVO vo = UserApiConvert.INSTANCT.userDTO_userVO(dto);
            Opt.ofNullable(vo.getUserStatus()).map(UserStatus::getDesc).ifPresent(vo::setUserStatusStr);
            Opt.ofNullable(vo.getGender()).map(GenderType::getDesc).ifPresent(vo::setGenderStr);
            setBanStatus(vo);
            return vo;
        });
        return voPage;
    }

    // 补充封禁信息
    private void setBanStatus(UserVO vo) {
        List<String> banMsgs = new ArrayList<>();
        // 检查封禁状态
        Arrays.stream(BanType.values())
                .filter(enm-> !enm.isUnban())
                .forEach(banType -> userBanApi.checkBan(TargetType.ACCOUNT, vo.getUserId().toString(), banType)
                        .ifFail((code, msg) -> banMsgs.add(banType.getDesc())));
        vo.setBanStatusStr(String.join(",", banMsgs));
    }

}
