package com.devvv.cms.manager;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.devvv.cms.dao.cms.entity.CmsMenu;
import com.devvv.cms.dao.cms.entity.CmsMenuField;
import com.devvv.cms.dao.cms.mapper.CmsMenuFieldMapper;
import com.devvv.cms.dao.cms.mapper.CmsMenuMapper;
import com.devvv.commons.common.utils.CommonUtil;
import com.devvv.commons.core.config.cache.local.LocalCache;
import com.devvv.commons.core.config.cache.local.LocalCacheEnums;
import com.devvv.commons.core.config.cache.local.LocalCacheFactory;
import com.devvv.commons.core.utils.RoleUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create by WangSJ on 2025/12/31
 */
@Slf4j
@Component
public class MenuManager implements LocalCache {

    @Resource
    private CmsMenuMapper menuMapper;
    @Resource
    private CmsMenuFieldMapper fieldMapper;

    private List<CmsMenu> LIST_ALL = new ArrayList<>();
    private List<String> PERMISSION_ALL = new ArrayList<>();
    private Map<Long, CmsMenu> MENU_MAP = new HashMap<>();

    private List<CmsMenuField> FIELD_LIST_ALL = new ArrayList<>();
    private Map<Long, List<CmsMenuField>> FIELD_GROUPBY_MENUID = new HashMap<>();

    @Override

    public void init() {
        FIELD_LIST_ALL = fieldMapper.listAll();
        FIELD_GROUPBY_MENUID = FIELD_LIST_ALL.stream().collect(Collectors.groupingBy(CmsMenuField::getMenuId));

        LIST_ALL = menuMapper.listAll();
        PERMISSION_ALL = convertResource2Permission(LIST_ALL);
        MENU_MAP = LIST_ALL.stream().collect(Collectors.toMap(CmsMenu::getId, Function.identity()));
    }

    @Override
    public void reload(List<String> keys) {
        init();
    }

    // 注册完成后即可进行实例化
    @PostConstruct
    public void postConstruct() {
        getInstance();
    }

    /**
     * 提供静态方法，获取实例
     */
    public static MenuManager getInstance() {
        return LocalCacheFactory.getInstance(LocalCacheEnums.MenuManager, true);
    }


    public static List<String> convertResource2Permission(List<CmsMenu> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list
                .stream()
                .flatMap(r -> Opt.of(CommonUtil.splitStringSet(r.getExtApiUrl(), ","))
                        .peek(s -> s.add(r.getApiUrl()))
                        .orElse(Collections.emptySet())
                        .stream()
                )
                .filter(StrUtil::isNotBlank)
                .filter(s -> !s.startsWith("http"))
                .distinct()
                .toList();
    }


    public List<CmsMenu> listAllMenu() {
        return LIST_ALL;
    }

    public List<String> listAllPermission() {
        return PERMISSION_ALL;
    }

    /**
     * 根据用户ID和角色， 查询所有菜单
     */
    public List<CmsMenu> listUserMenus(Long adminId, Collection<String> roleCodes) {
        // 超管，直接返回所有菜单
        if (RoleUtil.hasSuperAdmin(roleCodes)) {
            return LIST_ALL;
        }

        List<CmsMenu> menuList = menuMapper.listByAdminId(adminId);
        // 如果只授权了叶子节点, 也需要反向查询所有父节点
        Set<Long> existingIds = menuList.stream().map(CmsMenu::getId).collect(Collectors.toSet());
        // 关键：用下标循环，允许在遍历过程中往 list 末尾追加元素
        for (int i = 0; i < menuList.size(); i++) {
            Long parentId = menuList.get(i).getParentId();
            if (parentId == null || parentId == 0L || existingIds.contains(parentId)) {
                continue;
            }
            // 从全集菜单中找到父节点
            CmsMenu parent = getMenuById(parentId);
            if (parent != null) {
                // 追加父节点到列表末尾，并记录到 existingIds
                menuList.add(parent);
                existingIds.add(parent.getId());
            }
        }
        // 整体排个序
        menuList.sort(Comparator.comparingInt(CmsMenu::getSort));
        return menuList;
    }

    public CmsMenu getMenuById(Long menuId) {
        if (menuId == null) {
            return null;
        }
        return MENU_MAP.get(menuId);
    }


    /**
     * 字段相关
     */
    public List<CmsMenuField> listAllField() {
        return FIELD_LIST_ALL;
    }

    public List<CmsMenuField> listFieldByMenuId(Long menuId) {
        if (menuId == null) {
            return Collections.emptyList();
        }
        return FIELD_GROUPBY_MENUID.getOrDefault(menuId, Collections.emptyList());
    }

}
