package com.devvv.user.api;

import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.response.Result;
import com.devvv.user.api.models.constants.ApiConstants;
import com.devvv.user.api.models.dto.UserDTO;
import com.devvv.user.api.models.dto.UserPageQueryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户管理接口
 */
@FeignClient(name = ApiConstants.USER_WEB)
public interface UserInfoApi {

    /**
     * 根据id，获取用户详情
     */
    @GetMapping("/inner/user/info/get")
    Result<UserDTO> getUser(@RequestParam("id") Long id);

    /**
     * 分页获取用户列表
     */
    @PostMapping("/inner/user/info/pageList")
    Result<PageVO<UserDTO>> pageList(@RequestBody UserPageQueryDTO pageQuery);

}
