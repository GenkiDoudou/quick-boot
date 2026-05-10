package io.github.genkidoudou.web.system.dept;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.dept.domain.SysDept;
import io.github.genkidoudou.web.system.dept.dto.SysDeptSaveRequest;
import io.github.genkidoudou.web.system.dept.mapper.SysDeptMapper;
import io.github.genkidoudou.web.system.dept.service.impl.DeptServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeptServiceImplTest {

    @Test
    void shouldBuildFullTreeAndPruneByDeptName() {
        SysDeptMapper mapper = mock(SysDeptMapper.class);
        when(mapper.selectList(ArgumentMatchers.<Wrapper<SysDept>>any())).thenReturn(sample());
        DeptServiceImpl service = new DeptServiceImpl(mapper);

        var all = service.listTree(null, null, null);
        var pruned = service.listTree("研发", null, null);

        assertThat(all).hasSize(1);
        assertThat(all.get(0).getChildren()).hasSize(2);
        assertThat(pruned).hasSize(1);
        assertThat(pruned.get(0).getChildren()).hasSize(1);
        assertThat(pruned.get(0).getChildren().get(0).getDeptName()).isEqualTo("研发部");
    }

    @Test
    void shouldRejectCycleWhenUpdatingParent() {
        SysDeptMapper mapper = mock(SysDeptMapper.class);
        when(mapper.selectById(2L)).thenReturn(dept(2L, 1L, "研发部"));
        when(mapper.selectById(3L)).thenReturn(dept(3L, 2L, "后端组"));
        when(mapper.selectList(ArgumentMatchers.<Wrapper<SysDept>>any())).thenReturn(sample());
        DeptServiceImpl service = new DeptServiceImpl(mapper);

        SysDeptSaveRequest req = new SysDeptSaveRequest();
        req.setDeptId(2L);
        req.setParentId(3L);
        req.setDeptName("研发部");
        req.setOrderNum(1);
        req.setStatus("0");

        assertThatThrownBy(() -> service.update(req))
                .isInstanceOf(WarningException.class)
                .hasMessageContaining("形成环");
    }

    @Test
    void shouldRejectDeleteWhenHasChildren() {
        SysDeptMapper mapper = mock(SysDeptMapper.class);
        when(mapper.selectById(1L)).thenReturn(dept(1L, -1L, "总公司"));
        when(mapper.selectCount(ArgumentMatchers.<Wrapper<SysDept>>any())).thenReturn(2L);
        DeptServiceImpl service = new DeptServiceImpl(mapper);

        assertThatThrownBy(() -> service.remove(1L))
                .isInstanceOf(WarningException.class)
                .hasMessageContaining("子部门");
    }

    private List<SysDept> sample() {
        return List.of(
                dept(1L, -1L, "总公司"),
                dept(2L, 1L, "研发部"),
                dept(3L, 2L, "后端组"),
                dept(4L, 1L, "财务部")
        );
    }

    private SysDept dept(Long id, Long parentId, String name) {
        SysDept d = new SysDept();
        d.setDeptId(id);
        d.setParentId(parentId);
        d.setDeptName(name);
        d.setOrderNum(0);
        d.setStatus("0");
        return d;
    }
}
