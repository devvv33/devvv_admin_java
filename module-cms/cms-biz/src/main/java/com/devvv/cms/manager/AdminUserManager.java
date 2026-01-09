package com.devvv.cms.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.devvv.cms.dao.cms.entity.CmsAdminUser;
import com.devvv.cms.dao.cms.entity.CmsDepartment;
import com.devvv.cms.dao.cms.entity.CmsRole;
import com.devvv.cms.dao.cms.mapper.CmsAdminUserMapper;
import com.devvv.cms.dao.cms.mapper.CmsDepartmentMapper;
import com.devvv.cms.dao.cms.mapper.CmsRoleMapper;
import com.devvv.commons.core.config.cache.local.LocalCache;
import com.devvv.commons.core.config.cache.local.LocalCacheEnums;
import com.devvv.commons.core.config.cache.local.LocalCacheFactory;
import com.devvv.commons.core.utils.BusiTransactionUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create by WangSJ on 2024/07/09
 */
@Slf4j
@Component
public class AdminUserManager implements LocalCache {

    @Resource
    private CmsAdminUserMapper adminUserMapper;
    @Resource
    private CmsDepartmentMapper departmentMapper;
    @Resource
    private CmsRoleMapper roleMapper;

    private List<CmsAdminUser> LIST_ALL = new ArrayList<>();
    private Map<Long, CmsAdminUser> ADMIN_MAP = new HashMap<>();

    // 加载所有用户
    @Override
    public void init() {
        List<CmsAdminUser> listAll = adminUserMapper.listAll();

        Map<Long, CmsDepartment> deptAll = departmentMapper.listAll().stream().collect(Collectors.toMap(CmsDepartment::getId, Function.identity()));
        for (CmsAdminUser admin : listAll) {
            // 补充部门和角色
            admin.setDepartment(deptAll.get(admin.getDepartmentId()));
            admin.setRoleList(roleMapper.listByAdminId(admin.getAdminId()));
        }
        LIST_ALL = listAll;
        ADMIN_MAP = LIST_ALL.stream().collect(Collectors.toMap(CmsAdminUser::getAdminId, Function.identity()));
    }

    @Override
    public void reload(List<String> keys) {
        // 如果只有一个数据变化了, 尝试只更新一个数据
        if (keys != null && keys.size() == 1) {
            try {
                Long updatedAdminId = Long.parseLong(keys.get(0));
                CmsAdminUser dirtyAdmin = ADMIN_MAP.get(updatedAdminId);
                if (dirtyAdmin != null) {
                    CmsAdminUser newAdmin = adminUserMapper.selectByPrimaryKey(updatedAdminId);
                    BeanUtil.copyProperties(newAdmin, dirtyAdmin);
                    return;
                }
            } catch (Exception ignored) {}
        }
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
    public static AdminUserManager getInstance() {
        return LocalCacheFactory.getInstance(LocalCacheEnums.AdminUserManager, true);
    }

    public CmsAdminUser getAdmin(Long adminId) {
        if (adminId == null) {
            return null;
        }
        return ADMIN_MAP.get(adminId);
    }

    public String getAdminName(Long adminId) {
        CmsAdminUser admin = getAdmin(adminId);
        if (admin == null) {
            return null;
        }
        return admin.getNickname();
    }

    /**
     * 根据用户名查询用户
     */
    public CmsAdminUser getByUsername(String username) {
        if (StrUtil.isBlank(username)) {
            return null;
        }
        return LIST_ALL.stream()
                .filter(admin -> ObjectUtil.equal(admin.getUsername(), username))
                .findFirst()
                .orElse(null);
    }
    /**
     * 根据手机号获取用户
     */
    public CmsAdminUser getByMobile(String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return null;
        }
        return LIST_ALL.stream()
                .filter(admin -> ObjectUtil.equal(admin.getMobile(), mobile))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据用户名，统计用户数量
     */
    public int countByUsername(String username) {
        return (int) LIST_ALL.stream()
                .filter(admin -> ObjectUtil.equal(admin.getUsername(), username))
                .count();
    }

    /**
     * 根据手机号，统计用户数量
     */
    public int countByMobile(String mobile) {
        return (int) LIST_ALL.stream()
                .filter(admin -> ObjectUtil.equal(admin.getMobile(), mobile))
                .count();
    }

    /**
     * 获取角色列表
     */
    public List<String> getRoleCodes(Long adminId) {
        return Opt.ofNullable(adminId)
                .map(this::getAdmin)
                .map(CmsAdminUser::getRoleList)
                .map(roleList -> roleList.stream().map(CmsRole::getRoleCode).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * 更新最后登录时间
     */
    public void updateLastLoginTime(Long adminId, Date loginTime, String loginIp) {
        CmsAdminUser admin = ADMIN_MAP.get(adminId);

        adminUserMapper.updateByPrimaryKeySelective(CmsAdminUser.builder()
                .adminId(adminId)
                .lastLoginTime(loginTime)
                .lastLoginIp(loginIp)
                .loginCount(ObjUtil.defaultIfNull(admin.getLoginCount(), 0L) + 1)
                .build());
        BusiTransactionUtil.execAfterCommit(()-> LocalCache.notifyReload(LocalCacheEnums.AdminUserManager, adminId.toString()));
    }
}
