package com.devvv.cms.models.convert;

import com.devvv.cms.dao.cms.entity.CmsRole;
import com.devvv.commons.core.session.RoleVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Create by WangSJ on 2025/12/26
 */
@Mapper(builder = @Builder(disableBuilder = true))
public interface SysConvert {
    SysConvert INSTANCT = Mappers.getMapper(SysConvert.class);


    /**
     * 角色相关
     */
    RoleVO cmsRole_roleVO(CmsRole entity);
    List<RoleVO> cmsRole_roleVO(List<CmsRole> entityList);

}
