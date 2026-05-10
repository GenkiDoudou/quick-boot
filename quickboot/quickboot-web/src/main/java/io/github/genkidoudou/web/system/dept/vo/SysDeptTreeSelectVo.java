package io.github.genkidoudou.web.system.dept.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门下拉树节点（{@code GET /system/dept/treeselect} 成功载荷 {@code data} 的元素类型）。
 * <p>
 * {@code id} 对应 {@code dept_id}，{@code label} 对应 {@code dept_name}；{@code children} 规则同列表树。
 */
@Data
public class SysDeptTreeSelectVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 部门 id（等于 {@code deptId}）。 */
    private Long id;

    /** 展示文案（等于部门名称）。 */
    private String label;

    /** 子节点；无子为 {@code []}。 */
    private List<SysDeptTreeSelectVo> children = new ArrayList<>();
}
