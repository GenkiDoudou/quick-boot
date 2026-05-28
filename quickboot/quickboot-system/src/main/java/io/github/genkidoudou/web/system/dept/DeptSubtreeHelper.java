package io.github.genkidoudou.web.system.dept;

import io.github.genkidoudou.web.system.dept.domain.SysDept;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门树子集计算（用户列表按部门筛选与数据权限「本部门及以下」共用）。
 */
public final class DeptSubtreeHelper {

    private DeptSubtreeHelper() {
    }

    /**
     * 计算某部门节点及其全部子孙部门 id（含自身）。
     *
     * @param all         全量部门列表（通常未删除行）
     * @param rootDeptId 根部门 id
     * @return 子树 id 集合；根不存在时返回空集
     */
    public static Set<Long> collectDeptSubtreeIds(List<SysDept> all, Long rootDeptId) {
        if (rootDeptId == null || all == null || all.isEmpty()) {
            return Collections.emptySet();
        }
        Map<Long, List<SysDept>> childrenMap = new LinkedHashMap<>();
        for (SysDept d : all) {
            Long pid = d.getParentId() != null ? d.getParentId() : -1L;
            childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(d);
        }
        if (!existsDept(all, rootDeptId)) {
            return Collections.emptySet();
        }
        Set<Long> out = new HashSet<>();
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(rootDeptId);
        while (!stack.isEmpty()) {
            Long id = stack.pop();
            if (!out.add(id)) {
                continue;
            }
            for (SysDept ch : childrenMap.getOrDefault(id, Collections.emptyList())) {
                stack.push(ch.getDeptId());
            }
        }
        return out;
    }

    private static boolean existsDept(List<SysDept> all, Long deptId) {
        for (SysDept d : all) {
            if (deptId.equals(d.getDeptId())) {
                return true;
            }
        }
        return false;
    }
}
