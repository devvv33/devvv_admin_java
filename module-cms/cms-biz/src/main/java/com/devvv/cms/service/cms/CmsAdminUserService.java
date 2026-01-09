package com.devvv.cms.service.cms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.devvv.cms.dao.cms.entity.CmsAdminUser;
import com.devvv.cms.dao.cms.entity.CmsAdminUserRole;
import com.devvv.cms.dao.cms.mapper.CmsAdminUserMapper;
import com.devvv.cms.dao.cms.mapper.CmsAdminUserRoleMapper;
import com.devvv.cms.dao.cms.mapper.CmsDepartmentMapper;
import com.devvv.cms.dao.cms.mapper.CmsRoleMapper;
import com.devvv.cms.manager.AdminUserManager;
import com.devvv.cms.models.form.AdminQueryForm;
import com.devvv.cms.models.form.AdminUserForm;
import com.devvv.commons.common.dto.common.PageVO;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.common.utils.MyStrUtil;
import com.devvv.commons.common.utils.PasswordUtil;
import com.devvv.commons.core.config.cache.local.LocalCache;
import com.devvv.commons.core.config.cache.local.LocalCacheEnums;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.session.RoleVO;
import com.devvv.commons.core.utils.BusiTransactionUtil;
import com.devvv.commons.core.utils.MyFileUtil;
import com.devvv.commons.core.utils.RoleUtil;
import com.devvv.commons.token.utils.StpAdminUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by WangSJ on 2024/06/20
 */
@Slf4j
@Service
public class CmsAdminUserService {

    @Resource
    private AdminUserManager adminUserManager;
    @Resource
    private CmsAdminUserMapper cmsAdminUserMapper;
    @Resource
    private CmsAdminUserRoleMapper userRoleMapper;
    @Resource
    private CmsRoleMapper roleMapper;
    @Resource
    private CmsDepartmentMapper departmentMapper;

    /**
     * 查询账号列表
     */
    public PageVO<CmsAdminUser> pageListAdmin(AdminQueryForm form) {
        MyStrUtil.trimStringFields(form);
        PageHelper.startPage(form.getPageNum(), form.getPageSize());
        List<CmsAdminUser> list = cmsAdminUserMapper.listByParams(form);
        list.forEach(item -> {
            item.setPassword(null);
            item.setRoleList(roleMapper.listByAdminId(item.getAdminId()));
            item.setDepartment(departmentMapper.selectByPrimaryKey(item.getDepartmentId()));
        });
        return PageVO.build(list);
    }

    /**
     * 创建账户
     */
    @Transactional
    public void createAdmin(AdminUserForm form) throws IOException {
        MyStrUtil.trimStringFields(form);
        Assert.notBlank(form.getUsername(), "用户名不能为空");
        PasswordUtil.checkSimplePassword(form.getPassword());
        // 用户名不能重复
        if (StrUtil.isNotBlank(form.getUsername()) && adminUserManager.countByUsername(form.getUsername()) > 0) {
            throw new BusiException("用户名已存在");
        }
        if (StrUtil.isNotBlank(form.getMobile()) && adminUserManager.countByMobile(form.getMobile()) > 0) {
            throw new BusiException("手机号已存在");
        }
        // 如果当前用户非超管, 则只能分配自己已有的角色
        if (form.getRoleIdList() != null && !BusiContextUtil.isSuperAdmin()) {
            List<Long> currUserRoleIds = BusiContextUtil.getAdminSessionCopy().getRoleList().stream().map(RoleVO::getId).toList();
            Assert.isTrue(CollUtil.containsAll(currUserRoleIds, form.getRoleIdList()), "越权分配权限!");
        }

        CmsAdminUser admin = CmsAdminUser.builder()
                .username(form.getUsername())
                .password(PasswordUtil.encodePassword(form.getPassword()))
                .nickname(form.getNickname())
                .avatar(form.getAvatar())
                .mobile(form.getMobile())
                .departmentId(form.getDepartmentId())
                .status(form.getStatus())
                .createId(BusiContextUtil.getAdminId())
                .createTime(new Date())
                .loginCount(0L)
                .build();
        cmsAdminUserMapper.insertSelective(admin);

        // 添加角色
        if (form.getRoleIdList() != null && !form.getRoleIdList().isEmpty()) {
            userRoleMapper.insertBatch(form.getRoleIdList().stream()
                    .map(roleId -> CmsAdminUserRole.builder()
                            .adminId(admin.getAdminId())
                            .roleId(roleId)
                            .build())
                    .collect(Collectors.toList()));
        }

        // 通知缓存变更
        BusiTransactionUtil.execAfterCommit(()-> LocalCache.notifyReload(LocalCacheEnums.AdminUserManager, admin.getAdminId().toString()));
    }

    /**
     * 修改账户
     */
    @Transactional
    public void updateAdmin(AdminUserForm form) throws IOException {
        MyStrUtil.trimStringFields(form);
        Assert.notNull(form.getAdminId(), "id不能为空");
        Assert.notBlank(form.getNickname(), "昵称不能为空");
        Assert.notNull(form.getDepartmentId(), "部门不能为空");
        Assert.notNull(form.getStatus(), "状态不能为空");
        CmsAdminUser admin = cmsAdminUserMapper.selectByPrimaryKey(form.getAdminId());
        Assert.notNull(admin, "账号不存在");
        // 目标用户是则只能被超管修改
        if (!BusiContextUtil.isSuperAdmin() && RoleUtil.hasSuperAdmin(adminUserManager.getRoleCodes(admin.getAdminId()))) {
            throw new BusiException("超管用户,无权修改!");
        }
        // 如果当前用户非超管, 则只能分配自己已有的角色
        if (form.getRoleIdList() != null && !BusiContextUtil.isSuperAdmin()) {
            List<Long> currUserRoleIds = BusiContextUtil.getAdminSessionCopy().getRoleList().stream().map(RoleVO::getId).toList();
            Assert.isTrue(CollUtil.containsAll(currUserRoleIds, form.getRoleIdList()), "越权分配权限!");
        }
        // 手机号发生了改变,检查新手机号是否已存在
        if (!ObjUtil.equal(form.getMobile(), admin.getMobile())) {
            if (StrUtil.isNotBlank(form.getMobile()) && adminUserManager.countByMobile(form.getMobile()) > 0) {
                throw new BusiException("手机号已存在");
            }
        }
        // 头像上传
        if (form.getAvatarFile() != null) {
            String url = MyFileUtil.saveUploadFile(form.getAvatarFile().getInputStream(), FileUtil.getSuffix(form.getAvatarFile().getOriginalFilename()));
            form.setAvatar(url);
        }

        // 执行更新
        CmsAdminUser update = CmsAdminUser.builder()
                .adminId(form.getAdminId())
                .nickname(form.getNickname())
                .avatar(form.getAvatar())
                .mobile(form.getMobile())
                .departmentId(form.getDepartmentId())
                .build();
        cmsAdminUserMapper.updateByPrimaryKeySelective(update);

        // 删除原角色
        userRoleMapper.deleteByAdminId(update.getAdminId());
        // 重新添加角色
        if (form.getRoleIdList() != null && !form.getRoleIdList().isEmpty()) {
            userRoleMapper.insertBatch(form.getRoleIdList().stream()
                    .map(roleId -> CmsAdminUserRole.builder()
                            .adminId(update.getAdminId())
                            .roleId(roleId)
                            .build())
                    .collect(Collectors.toList()));
        }

        // 修改了账号信息, 直接踢出此账号
        StpAdminUtil.kickout(update.getAdminId());

        // 通知缓存变更
        BusiTransactionUtil.execAfterCommit(()-> LocalCache.notifyReload(LocalCacheEnums.AdminUserManager, admin.getAdminId().toString()));
    }

    /**
     * 改变账号状态
     */
    @Transactional
    public void updateAdminStatus(AdminUserForm form) {
        Assert.notNull(form.getAdminId(), "id不能为空");
        Assert.notNull(form.getStatus(), "status不能为空");

        CmsAdminUser update = CmsAdminUser.builder()
                .adminId(form.getAdminId())
                .status(form.getStatus())
                .build();
        cmsAdminUserMapper.updateByPrimaryKeySelective(update);

        // 修改了账号信息, 直接踢出此账号
        StpAdminUtil.kickout(update.getAdminId());

        // 通知缓存变更
        BusiTransactionUtil.execAfterCommit(()-> LocalCache.notifyReload(LocalCacheEnums.AdminUserManager, update.getAdminId().toString()));
    }

}
