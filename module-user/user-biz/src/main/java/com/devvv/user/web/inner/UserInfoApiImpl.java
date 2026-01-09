package com.devvv.user.web.inner;

import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.response.Result;
import com.devvv.user.api.UserInfoApi;
import com.devvv.user.api.models.dto.UserDTO;
import com.devvv.user.api.models.dto.UserPageQueryDTO;
import com.devvv.user.web.dao.entity.UUserBasic;
import com.devvv.user.web.models.convert.UserConvert;
import com.devvv.user.web.models.form.UserIdForm;
import com.devvv.user.web.service.UserInfoService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class UserInfoApiImpl implements UserInfoApi {

    @Resource
    private UserInfoService userInfoService;

    @Override
    public Result<UserDTO> getUser(Long id) {
        UUserBasic user = userInfoService.getUser(new UserIdForm(id));
        UserDTO dto = UserConvert.INSTANCE.userEntity_DTO(user);
        return Result.success(dto);
    }

    @Override
    public Result<PageVO<UserDTO>> pageList(UserPageQueryDTO pageQuery) {
        PageVO<UUserBasic> basicPage = userInfoService.pageList(pageQuery);
        PageVO<UserDTO> dtoPage = basicPage.convertTo(UserConvert.INSTANCE::userEntity_DTO);
        return Result.success(dtoPage);
    }

}
