package io.github.genkidoudou.system.internal.support;

import io.github.genkidoudou.system.internal.entity.SysDept;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 按 {@code parentId} 展开部门自身及全部子孙 ID（含停用节点）。
 */
public final class DeptIdExpand {

  private DeptIdExpand() {
  }

  /**
   * @param rootId 选中部门；为 {@code null} 时返回空列表
   * @param depts  全部或候选部门（至少含 deptId、parentId）
   * @return 含 rootId 及全部子孙；root 不在 depts 中时仍返回仅含 rootId 的列表
   */
  public static List<Long> includingChildren(Long rootId, Collection<SysDept> depts) {
    if (rootId == null) {
      return Collections.emptyList();
    }
    Map<Long, List<Long>> children = new HashMap<>();
    if (depts != null) {
      for (SysDept d : depts) {
        if (d == null || d.getDeptId() == null) {
          continue;
        }
        Long parentId = d.getParentId() == null ? 0L : d.getParentId();
        children.computeIfAbsent(parentId, k -> new ArrayList<>()).add(d.getDeptId());
      }
    }
    List<Long> out = new ArrayList<>();
    Set<Long> visited = new HashSet<>();
    ArrayDeque<Long> queue = new ArrayDeque<>();
    queue.add(rootId);
    while (!queue.isEmpty()) {
      Long id = queue.poll();
      if (!visited.add(id)) {
        continue;
      }
      out.add(id);
      List<Long> kids = children.get(id);
      if (kids != null) {
        queue.addAll(kids);
      }
    }
    return out;
  }
}
