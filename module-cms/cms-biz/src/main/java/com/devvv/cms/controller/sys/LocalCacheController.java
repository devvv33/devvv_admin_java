package com.devvv.cms.controller.sys;

import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.response.Result;
import com.devvv.commons.core.config.cache.local.LocalCacheEnums;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Create by WangSJ on 2024/08/07
 */
@Tag(name = "107-本地缓存")
@RestController
@RequestMapping("/cmsApi/sys/cache")
public class LocalCacheController {

    @Operation(summary = "1-查询所有配置")
    @PostMapping("/querySettings")
    public Result<PageVO> list() {
        List<Map<String, String>> list = Arrays.stream(LocalCacheEnums.values())
                .map(em -> Map.of("key", em.name(), "name", em.getDesc()))
                .toList();
        return Result.success(PageVO.build(list));
    }

}
