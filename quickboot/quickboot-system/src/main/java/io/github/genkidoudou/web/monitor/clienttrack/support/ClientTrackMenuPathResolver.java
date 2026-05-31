package io.github.genkidoudou.web.monitor.clienttrack.support;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.web.system.menu.domain.SysMenu;
import io.github.genkidoudou.web.system.menu.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 将前端监控 {@code pagePath}（Vue Router path，如 {@code /system/dept}）解析为 {@code sys_menu} 菜单名称。
 * <p>
 * 路径拼装规则与 {@link io.github.genkidoudou.web.system.menu.service.impl.MenuServiceImpl}、
 * quick-ui {@code permission.js} 一致：顶级带 {@code /}，子级为相对段拼接。
 */
@Component
@RequiredArgsConstructor
public class ClientTrackMenuPathResolver {

    private static final Long ROOT_PARENT_ID = -1L;
    private static final String TYPE_DIR = "M";
    private static final String TYPE_MENU = "C";

    /** 无 sys_menu 条目时的固定路由展示名（登录页、首页等） */
    private static final Map<String, MenuMatch> STATIC_PATH_LABELS = Map.of(
            "/login", new MenuMatch("登录", "登录"),
            "/index", new MenuMatch("首页", "首页"),
            "/", new MenuMatch("首页", "首页")
    );

    private final SysMenuMapper sysMenuMapper;

    /**
     * 按菜单名称或面包屑关键字反查可能的前端路由 path 列表，供监控批次 {@code pagePath} 筛选。
     *
     * @param keyword 菜单名 / 面包屑片段，模糊匹配（忽略大小写）
     * @return 规范化后的 path 列表（去重）；无匹配时空列表
     */
    public List<String> resolvePathsByMenuKeyword(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return List.of();
        }
        String kw = keyword.trim().toLowerCase();
        Map<String, MenuMatch> index = buildMenuPathIndex();
        List<String> paths = new ArrayList<>();
        for (Map.Entry<String, MenuMatch> e : index.entrySet()) {
            MenuMatch match = e.getValue();
            if (match == null) {
                continue;
            }
            boolean hit = StrUtil.containsIgnoreCase(match.menuName(), kw)
                    || StrUtil.containsIgnoreCase(match.breadcrumb(), kw);
            if (hit) {
                paths.add(e.getKey());
            }
        }
        paths.sort(Comparator.comparingInt(String::length).reversed());
        return paths;
    }

    /**
     * @param pagePath 监控批次 pagePath
     * @return 匹配结果；无匹配时 {@code null}
     */
    public MenuMatch resolve(String pagePath) {
        if (StrUtil.isBlank(pagePath)) {
            return null;
        }
        Map<String, MenuMatch> index = buildMenuPathIndex();
        return match(index, normalizePath(pagePath));
    }

    /**
     * 批量解析，复用同一份菜单索引。
     *
     * @param pagePaths 路径集合
     * @return key 为规范化 path，value 为匹配结果（可 null）
     */
    public Map<String, MenuMatch> resolveBatch(Iterable<String> pagePaths) {
        Map<String, MenuMatch> index = buildMenuPathIndex();
        Map<String, MenuMatch> out = new LinkedHashMap<>();
        if (pagePaths == null) {
            return out;
        }
        for (String raw : pagePaths) {
            if (StrUtil.isBlank(raw)) {
                continue;
            }
            String key = normalizePath(raw);
            out.put(key, match(index, key));
        }
        return out;
    }

    private Map<String, MenuMatch> buildMenuPathIndex() {
        List<SysMenu> all = sysMenuMapper.selectList(
                Wrappers.<SysMenu>lambdaQuery()
                        .in(SysMenu::getMenuType, TYPE_DIR, TYPE_MENU)
                        .eq(SysMenu::getStatus, "0")
                        .orderByAsc(SysMenu::getOrderNum));
        if (all.isEmpty()) {
            return Map.of();
        }
        Map<Long, SysMenu> byId = new HashMap<>(all.size() * 2);
        Map<Long, List<SysMenu>> childrenMap = new LinkedHashMap<>();
        for (SysMenu m : all) {
            byId.put(m.getMenuId(), m);
            Long pid = m.getParentId() != null ? m.getParentId() : ROOT_PARENT_ID;
            childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(m);
        }
        for (List<SysMenu> list : childrenMap.values()) {
            list.sort(Comparator
                    .comparing(SysMenu::getOrderNum, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(SysMenu::getMenuName, Comparator.nullsLast(String::compareTo)));
        }
        Map<String, MenuMatch> index = new LinkedHashMap<>();
        for (SysMenu root : childrenMap.getOrDefault(ROOT_PARENT_ID, Collections.emptyList())) {
            walkMenu(root, "", childrenMap, byId, index);
        }
        return index;
    }

    private void walkMenu(
            SysMenu menu,
            String parentFullPath,
            Map<Long, List<SysMenu>> childrenMap,
            Map<Long, SysMenu> byId,
            Map<String, MenuMatch> index) {
        String fullPath = joinRouterPath(menu, parentFullPath);
        if (TYPE_MENU.equals(menu.getMenuType())) {
            index.put(normalizePath(fullPath), new MenuMatch(menu.getMenuName(), buildBreadcrumb(menu, byId)));
        }
        for (SysMenu child : childrenMap.getOrDefault(menu.getMenuId(), Collections.emptyList())) {
            if (TYPE_DIR.equals(child.getMenuType()) || TYPE_MENU.equals(child.getMenuType())) {
                walkMenu(child, fullPath, childrenMap, byId, index);
            }
        }
    }

    /**
     * 与 MenuServiceImpl.normalizeRouterPath + 前端 ParentView 路径拼接一致。
     */
    private static String joinRouterPath(SysMenu menu, String parentFullPath) {
        String segment = normalizeRouterSegment(menu);
        Long parentId = menu.getParentId() != null ? menu.getParentId() : ROOT_PARENT_ID;
        if (ROOT_PARENT_ID.equals(parentId)) {
            return segment.startsWith("/") ? segment : "/" + segment;
        }
        if (StrUtil.isBlank(parentFullPath)) {
            return "/" + segment;
        }
        String rel = segment.startsWith("/") ? segment.substring(1) : segment;
        if (parentFullPath.endsWith("/")) {
            return parentFullPath + rel;
        }
        return parentFullPath + "/" + rel;
    }

    private static String normalizeRouterSegment(SysMenu menu) {
        String path = StrUtil.nullToEmpty(menu.getPath());
        if (StrUtil.isBlank(path) || path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        Long parentId = menu.getParentId() != null ? menu.getParentId() : ROOT_PARENT_ID;
        if (ROOT_PARENT_ID.equals(parentId)) {
            return path.startsWith("/") ? path : "/" + path;
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private static String buildBreadcrumb(SysMenu menu, Map<Long, SysMenu> byId) {
        List<String> names = new ArrayList<>();
        Long cur = menu.getMenuId();
        while (cur != null) {
            SysMenu node = byId.get(cur);
            if (node == null) {
                break;
            }
            if (TYPE_DIR.equals(node.getMenuType()) || TYPE_MENU.equals(node.getMenuType())) {
                if (StrUtil.isNotBlank(node.getMenuName())) {
                    names.add(node.getMenuName());
                }
            }
            Long p = node.getParentId();
            if (p == null || ROOT_PARENT_ID.equals(p)) {
                break;
            }
            cur = p;
        }
        Collections.reverse(names);
        return String.join(" / ", names);
    }

    private static MenuMatch match(Map<String, MenuMatch> index, String path) {
        if (StrUtil.isBlank(path)) {
            return null;
        }
        MenuMatch staticMatch = STATIC_PATH_LABELS.get(path);
        if (staticMatch != null) {
            return staticMatch;
        }
        if (index.isEmpty()) {
            return null;
        }
        MenuMatch exact = index.get(path);
        if (exact != null) {
            return exact;
        }
        MenuMatch best = null;
        int bestLen = -1;
        for (Map.Entry<String, MenuMatch> e : index.entrySet()) {
            String menuPath = e.getKey();
            if (path.equals(menuPath) || path.startsWith(menuPath + "/")) {
                if (menuPath.length() > bestLen) {
                    best = e.getValue();
                    bestLen = menuPath.length();
                }
            }
        }
        return best;
    }

    private static String normalizePath(String raw) {
        String p = StrUtil.trim(raw);
        if (StrUtil.isBlank(p)) {
            return "";
        }
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        while (p.length() > 1 && p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    /**
     * 菜单匹配结果。
     *
     * @param menuName   叶子菜单名（C 类型）
     * @param breadcrumb 自根目录至该菜单的面包屑，如「系统管理 / 部门管理」
     */
    public record MenuMatch(String menuName, String breadcrumb) {
    }
}
