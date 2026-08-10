package io.github.genkidoudou.system.internal.support;

import io.github.genkidoudou.system.internal.entity.SysDept;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeptIdExpandTest {

  @Test
  void rootIncludesAllDescendants() {
    List<SysDept> tree = List.of(
      dept(1L, 0L),
      dept(2L, 1L),
      dept(3L, 1L),
      dept(4L, 2L),
      dept(5L, 0L)
    );
    assertEquals(Set.of(1L, 2L, 3L, 4L), Set.copyOf(DeptIdExpand.includingChildren(1L, tree)));
  }

  @Test
  void leafIsSelfOnly() {
    List<SysDept> tree = List.of(
      dept(1L, 0L),
      dept(2L, 1L),
      dept(4L, 2L)
    );
    assertEquals(List.of(4L), DeptIdExpand.includingChildren(4L, tree));
  }

  @Test
  void unknownIdReturnsSelfOnly() {
    List<SysDept> tree = List.of(dept(1L, 0L));
    assertEquals(List.of(99L), DeptIdExpand.includingChildren(99L, tree));
  }

  @Test
  void nullRootReturnsEmpty() {
    assertTrue(DeptIdExpand.includingChildren(null, List.of(dept(1L, 0L))).isEmpty());
  }

  private static SysDept dept(Long id, Long parentId) {
    SysDept d = new SysDept();
    d.setDeptId(id);
    d.setParentId(parentId);
    return d;
  }
}
