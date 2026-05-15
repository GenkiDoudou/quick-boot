package io.github.genkidoudou.web.system.menu.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.menu.domain.SysMenu;
import io.github.genkidoudou.web.system.menu.domain.SysRole;
import io.github.genkidoudou.web.system.menu.domain.SysRoleMenu;
import io.github.genkidoudou.web.system.menu.domain.SysUserRole;
import io.github.genkidoudou.web.system.menu.dto.SysMenuSaveRequest;
import io.github.genkidoudou.web.system.menu.mapper.SysMenuMapper;
import io.github.genkidoudou.web.system.menu.mapper.SysRoleMapper;
import io.github.genkidoudou.web.system.menu.mapper.SysRoleMenuMapper;
import io.github.genkidoudou.web.system.menu.mapper.SysUserRoleMapper;
import io.github.genkidoudou.web.system.menu.service.MenuService;
import io.github.genkidoudou.web.system.menu.vo.RoleMenuTreeselectVo;
import io.github.genkidoudou.web.system.menu.vo.SysMenuTreeSelectVo;
import io.github.genkidoudou.web.system.menu.vo.SysMenuTreeVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单服务实现：内存建树、列表剪枝、角色路由与权限汇总。
 */
@Service
public class MenuServiceImpl implements MenuService {

    private static final Long ROOT_PARENT_ID = -1L;
    private static final String TYPE_DIR = "M";
    private static final String TYPE_MENU = "C";
    private static final String TYPE_BTN = "F";
    private static final String ADMIN_ROLE_KEY = "admin";

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public MenuServiceImpl(
            SysMenuMapper sysMenuMapper,
            SysRoleMapper sysRoleMapper,
            SysRoleMenuMapper sysRoleMenuMapper,
            SysUserRoleMapper sysUserRoleMapper) {
        this.sysMenuMapper = sysMenuMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    @Override
    public List<SysMenuTreeVo> listTree(String menuName, String status) {
        List<SysMenu> all = loadAllMenus();
        if (!hasAnyFilter(menuName, status)) {
            return buildTreeVos(all);
        }
        Set<Long> keep = computeFilteredNodeIds(all, menuName, status);
        List<SysMenu> pruned = all.stream().filter(m -> keep.contains(m.getMenuId())).toList();
        return buildTreeVos(pruned);
    }

    @Override
    public List<SysMenuTreeSelectVo> treeselect() {
        List<SysMenu> all = loadAllMenus();
        return buildSelectVos(all);
    }

    @Override
    public SysMenu getById(Long menuId) {
        return sysMenuMapper.selectById(menuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysMenuSaveRequest req) {
        Objects.requireNonNull(req, "req");
        SysMenu m = toEntity(req);
        normalizeNew(m);
        validateTypeFields(m, true);
        validateParentExists(m.getParentId(), null);
        sysMenuMapper.insert(m);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysMenuSaveRequest req) {
        Objects.requireNonNull(req, "req");
        if (req.getMenuId() == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "修改菜单须携带 menuId");
        }
        SysMenu m = toEntity(req);
        SysMenu existing = sysMenuMapper.selectById(m.getMenuId());
        if (existing == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "菜单不存在或已删除");
        }
        validateTypeFields(m, false);
        Long parentId = m.getParentId() != null ? m.getParentId() : existing.getParentId();
        validateParentExists(parentId, m.getMenuId());
        if (parentId.equals(m.getMenuId())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "上级菜单不能为自身");
        }
        assertNoCycle(m.getMenuId(), parentId);
        m.setParentId(parentId);
        sysMenuMapper.updateById(m);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long menuId) {
        SysMenu existing = sysMenuMapper.selectById(menuId);
        if (existing == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "菜单不存在或已删除");
        }
        long childCount = sysMenuMapper.selectCount(
                Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, menuId));
        if (childCount > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "存在未删除的子菜单，无法删除");
        }
        sysMenuMapper.deleteById(menuId);
    }

    @Override
    public RoleMenuTreeselectVo roleMenuTreeselect(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "角色不存在或已删除");
        }
        RoleMenuTreeselectVo vo = new RoleMenuTreeselectVo();
        vo.setMenus(treeselect());
        List<SysRoleMenu> rows = sysRoleMenuMapper.selectList(
                Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        vo.setCheckedKeys(rows.stream().map(SysRoleMenu::getMenuId).toList());
        return vo;
    }

    @Override
    public List<Map<String, Object>> buildRouterVos(Long userId) {
        Set<Long> granted = expandGrantedMenuIds(userId);
        if (granted.isEmpty()) {
            return List.of();
        }
        List<SysMenu> all = loadAllMenus();
        List<SysMenu> routeRows = all.stream()
                .filter(m -> granted.contains(m.getMenuId()))
                .filter(m -> TYPE_DIR.equals(m.getMenuType()) || TYPE_MENU.equals(m.getMenuType()))
                .filter(m -> "0".equals(m.getStatus()))
                .toList();
        return buildRouterMaps(routeRows);
    }

    @Override
    public List<String> listRoleKeysByUserId(Long userId) {
        List<SysUserRole> urs = sysUserRoleMapper.selectList(
                Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (urs.isEmpty()) {
            return List.of();
        }
        Set<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        List<SysRole> roles = roleIds.isEmpty()
                ? List.of()
                : sysRoleMapper.selectList(Wrappers.<SysRole>lambdaQuery().in(SysRole::getRoleId, roleIds));
        return roles.stream()
                .filter(r -> r != null && "0".equals(r.getStatus()))
                .map(SysRole::getRoleKey)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
    }

    @Override
    public List<String> listPermissionsByUserId(Long userId) {
        List<String> roleKeys = listRoleKeysByUserId(userId);
        boolean superAdmin = roleKeys.stream().anyMatch(ADMIN_ROLE_KEY::equals);
        Set<Long> granted = expandGrantedMenuIds(userId);
        LinkedHashSet<String> perms = new LinkedHashSet<>();
        if (superAdmin) {
            perms.add("*:*:*");
        }
        if (granted.isEmpty()) {
            return new ArrayList<>(perms);
        }
        List<SysMenu> all = loadAllMenus();
        for (SysMenu m : all) {
            if (!granted.contains(m.getMenuId())) {
                continue;
            }
            if (StrUtil.isNotBlank(m.getPerms())) {
                String norm = normalizePermsStorage(m.getPerms());
                if (norm != null) {
                    for (String token : norm.split(",")) {
                        if (StrUtil.isNotBlank(token)) {
                            perms.add(token);
                        }
                    }
                }
            }
        }
        return new ArrayList<>(perms);
    }

    private Set<Long> expandGrantedMenuIds(Long userId) {
        Set<Long> direct = loadDirectMenuIdsForUser(userId);
        if (direct.isEmpty()) {
            return Collections.emptySet();
        }
        List<SysMenu> all = loadAllMenus();
        Map<Long, SysMenu> byId = new HashMap<>(all.size() * 2);
        for (SysMenu m : all) {
            byId.put(m.getMenuId(), m);
        }
        Set<Long> expanded = new HashSet<>();
        for (Long mid : direct) {
            Long cur = mid;
            while (cur != null) {
                if (!expanded.add(cur)) {
                    break;
                }
                SysMenu node = byId.get(cur);
                if (node == null) {
                    break;
                }
                Long p = node.getParentId();
                if (p == null || ROOT_PARENT_ID.equals(p)) {
                    break;
                }
                cur = p;
            }
        }
        return expanded;
    }

    private Set<Long> loadDirectMenuIdsForUser(Long userId) {
        List<SysUserRole> urs = sysUserRoleMapper.selectList(
                Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (urs.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        List<SysRoleMenu> rms = sysRoleMenuMapper.selectList(
                Wrappers.<SysRoleMenu>lambdaQuery().in(SysRoleMenu::getRoleId, roleIds));
        return rms.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
    }

    private List<Map<String, Object>> buildRouterMaps(List<SysMenu> rows) {
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        Comparator<SysMenu> order = Comparator
                .comparing(SysMenu::getOrderNum, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SysMenu::getMenuName, Comparator.nullsLast(String::compareTo));
        Map<Long, List<SysMenu>> childrenMap = new LinkedHashMap<>();
        for (SysMenu m : rows) {
            Long pid = m.getParentId() != null ? m.getParentId() : ROOT_PARENT_ID;
            childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(m);
        }
        for (List<SysMenu> list : childrenMap.values()) {
            list.sort(order);
        }
        List<SysMenu> roots = childrenMap.getOrDefault(ROOT_PARENT_ID, Collections.emptyList());
        List<Map<String, Object>> out = new ArrayList<>();
        for (SysMenu r : roots) {
            out.add(toRouterMap(r, childrenMap));
        }
        return out;
    }

    private Map<String, Object> toRouterMap(SysMenu m, Map<Long, List<SysMenu>> childrenMap) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (StrUtil.isNotBlank(m.getRouteName())) {
            map.put("name", m.getRouteName());
        }
        map.put("path", StrUtil.nullToEmpty(m.getPath()));
        map.put("hidden", "1".equals(m.getVisible()));
        if (StrUtil.isNotBlank(m.getComponent())) {
            map.put("component", m.getComponent());
        }
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", m.getMenuName());
        meta.put("icon", StrUtil.nullToEmpty(m.getIcon()));
        meta.put("noCache", "1".equals(m.getIsCache()));
        map.put("meta", meta);
        List<SysMenu> ch = childrenMap.getOrDefault(m.getMenuId(), Collections.emptyList());
        if (!ch.isEmpty()) {
            List<Map<String, Object>> children = new ArrayList<>();
            for (SysMenu c : ch) {
                children.add(toRouterMap(c, childrenMap));
            }
            map.put("children", children);
        }
        return map;
    }

    private List<SysMenu> loadAllMenus() {
        return sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getOrderNum).orderByAsc(SysMenu::getMenuName));
    }

    private static boolean hasAnyFilter(String menuName, String status) {
        return StrUtil.isNotBlank(menuName) || StrUtil.isNotBlank(status);
    }

    static Set<Long> computeFilteredNodeIds(List<SysMenu> all, String menuName, String status) {
        Map<Long, SysMenu> byId = new HashMap<>(all.size() * 2);
        for (SysMenu m : all) {
            byId.put(m.getMenuId(), m);
        }
        Set<Long> matched = new HashSet<>();
        for (SysMenu m : all) {
            if (matchesFilter(m, menuName, status)) {
                matched.add(m.getMenuId());
            }
        }
        if (matched.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> keep = new HashSet<>();
        for (Long id : matched) {
            Long cur = id;
            while (cur != null) {
                if (!keep.add(cur)) {
                    break;
                }
                SysMenu node = byId.get(cur);
                if (node == null) {
                    break;
                }
                Long p = node.getParentId();
                if (p == null || ROOT_PARENT_ID.equals(p)) {
                    break;
                }
                cur = p;
            }
        }
        return keep;
    }

    static boolean matchesFilter(SysMenu m, String menuName, String status) {
        if (StrUtil.isNotBlank(menuName)) {
            String needle = menuName.trim();
            if (m.getMenuName() == null || !m.getMenuName().toLowerCase().contains(needle.toLowerCase())) {
                return false;
            }
        }
        if (StrUtil.isNotBlank(status)) {
            if (!status.trim().equals(m.getStatus())) {
                return false;
            }
        }
        return true;
    }

    private List<SysMenuTreeVo> buildTreeVos(List<SysMenu> rows) {
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        Comparator<SysMenu> order = Comparator
                .comparing(SysMenu::getOrderNum, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SysMenu::getMenuName, Comparator.nullsLast(String::compareTo));
        Map<Long, List<SysMenu>> childrenMap = new LinkedHashMap<>();
        for (SysMenu m : rows) {
            Long pid = m.getParentId() != null ? m.getParentId() : ROOT_PARENT_ID;
            childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(m);
        }
        for (List<SysMenu> list : childrenMap.values()) {
            list.sort(order);
        }
        List<SysMenu> roots = childrenMap.getOrDefault(ROOT_PARENT_ID, Collections.emptyList());
        List<SysMenuTreeVo> out = new ArrayList<>();
        for (SysMenu r : roots) {
            out.add(toTreeVo(r, childrenMap));
        }
        return out;
    }

    private SysMenuTreeVo toTreeVo(SysMenu m, Map<Long, List<SysMenu>> childrenMap) {
        SysMenuTreeVo vo = copyToTreeVo(m);
        List<SysMenu> ch = childrenMap.getOrDefault(m.getMenuId(), Collections.emptyList());
        List<SysMenuTreeVo> vos = new ArrayList<>();
        for (SysMenu c : ch) {
            vos.add(toTreeVo(c, childrenMap));
        }
        vo.setChildren(vos);
        return vo;
    }

    private static SysMenuTreeVo copyToTreeVo(SysMenu m) {
        SysMenuTreeVo vo = new SysMenuTreeVo();
        vo.setMenuId(m.getMenuId());
        vo.setParentId(m.getParentId());
        vo.setMenuType(m.getMenuType());
        vo.setMenuName(m.getMenuName());
        vo.setOrderNum(m.getOrderNum());
        vo.setPath(m.getPath());
        vo.setComponent(m.getComponent());
        vo.setRouteName(m.getRouteName());
        vo.setPerms(m.getPerms());
        vo.setIcon(m.getIcon());
        vo.setVisible(m.getVisible());
        vo.setStatus(m.getStatus());
        vo.setIsFrame(m.getIsFrame());
        vo.setIsCache(m.getIsCache());
        vo.setRemark(m.getRemark());
        vo.setCreateBy(m.getCreateBy());
        vo.setCreateTime(m.getCreateTime());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    private List<SysMenuTreeSelectVo> buildSelectVos(List<SysMenu> rows) {
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        Comparator<SysMenu> order = Comparator
                .comparing(SysMenu::getOrderNum, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SysMenu::getMenuName, Comparator.nullsLast(String::compareTo));
        Map<Long, List<SysMenu>> childrenMap = new LinkedHashMap<>();
        for (SysMenu m : rows) {
            Long pid = m.getParentId() != null ? m.getParentId() : ROOT_PARENT_ID;
            childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(m);
        }
        for (List<SysMenu> list : childrenMap.values()) {
            list.sort(order);
        }
        List<SysMenu> roots = childrenMap.getOrDefault(ROOT_PARENT_ID, Collections.emptyList());
        List<SysMenuTreeSelectVo> out = new ArrayList<>();
        for (SysMenu r : roots) {
            out.add(toSelectVo(r, childrenMap));
        }
        return out;
    }

    private SysMenuTreeSelectVo toSelectVo(SysMenu m, Map<Long, List<SysMenu>> childrenMap) {
        SysMenuTreeSelectVo vo = new SysMenuTreeSelectVo();
        vo.setId(m.getMenuId());
        vo.setLabel(m.getMenuName());
        List<SysMenu> ch = childrenMap.getOrDefault(m.getMenuId(), Collections.emptyList());
        List<SysMenuTreeSelectVo> vos = new ArrayList<>();
        for (SysMenu c : ch) {
            vos.add(toSelectVo(c, childrenMap));
        }
        vo.setChildren(vos);
        return vo;
    }

    private void normalizeNew(SysMenu m) {
        if (m.getParentId() == null) {
            m.setParentId(ROOT_PARENT_ID);
        }
        if (StrUtil.isBlank(m.getDelFlag())) {
            m.setDelFlag("0");
        }
        if (StrUtil.isBlank(m.getStatus())) {
            m.setStatus("0");
        }
        if (StrUtil.isBlank(m.getVisible())) {
            m.setVisible("0");
        }
        if (StrUtil.isBlank(m.getIsFrame())) {
            m.setIsFrame("0");
        }
        if (StrUtil.isBlank(m.getIsCache())) {
            m.setIsCache("0");
        }
        if (m.getOrderNum() == null) {
            m.setOrderNum(0);
        }
        if (TYPE_DIR.equals(m.getMenuType()) && StrUtil.isBlank(m.getComponent())) {
            m.setComponent("Layout");
        }
    }

    private void validateParentExists(Long parentId, Long excludeMenuIdWhenSelf) {
        if (ROOT_PARENT_ID.equals(parentId)) {
            return;
        }
        SysMenu p = sysMenuMapper.selectById(parentId);
        if (p == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "上级菜单不存在或已删除");
        }
        if (excludeMenuIdWhenSelf != null && excludeMenuIdWhenSelf.equals(parentId)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "上级菜单不能为自身");
        }
    }

    private void validateTypeFields(SysMenu m, boolean isNew) {
        validateStatus(m.getStatus());
        if (TYPE_BTN.equals(m.getMenuType())) {
            if (StrUtil.isBlank(normalizePermsStorage(m.getPerms()))) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "按钮类型须填写权限标识");
            }
            return;
        }
        if (TYPE_MENU.equals(m.getMenuType())) {
            if (StrUtil.isBlank(m.getPath()) || StrUtil.isBlank(m.getComponent())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "菜单类型须填写路由地址与组件路径");
            }
            return;
        }
        if (TYPE_DIR.equals(m.getMenuType())) {
            if (StrUtil.isBlank(m.getPath())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "目录类型须填写路由地址");
            }
            return;
        }
        throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "菜单类型不合法");
    }

    private void validateStatus(String status) {
        if (StrUtil.isBlank(status)) {
            return;
        }
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "菜单状态取值须为 0（正常）或 1（停用）");
        }
    }

    private void assertNoCycle(Long menuId, Long newParentId) {
        if (ROOT_PARENT_ID.equals(newParentId)) {
            return;
        }
        Set<Long> descendants = collectDescendantIds(menuId);
        if (descendants.contains(newParentId)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不能将上级菜单设为自己或子孙节点，否则会形成环");
        }
    }

    private Set<Long> collectDescendantIds(Long rootId) {
        List<SysMenu> all = loadAllMenus();
        Map<Long, List<Long>> children = new HashMap<>();
        for (SysMenu m : all) {
            Long pid = m.getParentId() != null ? m.getParentId() : ROOT_PARENT_ID;
            children.computeIfAbsent(pid, k -> new ArrayList<>()).add(m.getMenuId());
        }
        Set<Long> out = new HashSet<>();
        ArrayDeque<Long> dq = new ArrayDeque<>();
        dq.add(rootId);
        while (!dq.isEmpty()) {
            Long id = dq.removeFirst();
            if (!out.add(id)) {
                continue;
            }
            List<Long> ch = children.getOrDefault(id, Collections.emptyList());
            dq.addAll(ch);
        }
        return out;
    }

    private SysMenu toEntity(SysMenuSaveRequest req) {
        SysMenu m = new SysMenu();
        m.setMenuId(req.getMenuId());
        m.setParentId(req.getParentId());
        m.setMenuType(req.getMenuType());
        m.setMenuName(req.getMenuName());
        m.setOrderNum(req.getOrderNum());
        m.setPath(req.getPath());
        m.setComponent(req.getComponent());
        m.setQuery(req.getQuery());
        m.setRouteName(req.getRouteName());
        m.setIsFrame(req.getIsFrame());
        m.setIsCache(req.getIsCache());
        m.setVisible(req.getVisible());
        m.setStatus(req.getStatus());
        m.setPerms(normalizePermsStorage(req.getPerms()));
        m.setIcon(req.getIcon());
        m.setRemark(req.getRemark());
        return m;
    }

    /**
     * 权限字入库格式：去空白、中文逗号转英文逗号、按段去重后以英文逗号连接；无有效段时返回 {@code null}。
     *
     * @param raw 前端或手工录入
     * @return 规范化后的存储串，或 {@code null}
     */
    static String normalizePermsStorage(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.replace('，', ',').trim();
        if (StrUtil.isBlank(s)) {
            return null;
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String part : s.split(",")) {
            String t = StrUtil.trim(part);
            if (StrUtil.isNotBlank(t)) {
                set.add(t);
            }
        }
        return set.isEmpty() ? null : String.join(",", set);
    }
}
