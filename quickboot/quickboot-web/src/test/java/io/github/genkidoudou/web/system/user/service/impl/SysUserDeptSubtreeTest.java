package io.github.genkidoudou.web.system.user.service.impl;

import io.github.genkidoudou.web.system.dept.DeptSubtreeHelper;
import io.github.genkidoudou.web.system.dept.domain.SysDept;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 部门子树 id 计算（与列表筛选逻辑一致）。
 */
class SysUserDeptSubtreeTest {

    @Test
    void collectDeptSubtreeIds_includesSelfAndDescendants() {
        SysDept root = dept(1L, -1L, "总部");
        SysDept a = dept(10L, 1L, "分部A");
        SysDept b = dept(11L, 10L, "分部A-子");
        List<SysDept> all = List.of(root, a, b);
        Set<Long> ids = DeptSubtreeHelper.collectDeptSubtreeIds(all, 10L);
        assertThat(ids).containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    void collectDeptSubtreeIds_unknownRoot_returnsEmpty() {
        SysDept root = dept(1L, -1L, "总部");
        Set<Long> ids = DeptSubtreeHelper.collectDeptSubtreeIds(List.of(root), 999L);
        assertThat(ids).isEmpty();
    }

    private static SysDept dept(Long id, Long parentId, String name) {
        SysDept d = new SysDept();
        d.setDeptId(id);
        d.setParentId(parentId);
        d.setDeptName(name);
        d.setOrderNum(0);
        return d;
    }
}
