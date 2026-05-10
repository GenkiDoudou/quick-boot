package io.github.genkidoudou.web.system.dept.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门树节点（{@code GET /system/dept/list} 成功载荷 {@code data} 的元素类型）。
 * <p>
 * 与表字段对齐的 camelCase 属性 + 嵌套 {@code children}；无子节点时 {@code children} 为<strong>空数组</strong>。
 */
@Data
public class SysDeptTreeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 部门主键。 */
    private Long deptId;

    /** 上级部门 id，顶级为 -1。 */
    private Long parentId;

    /** 部门名称。 */
    private String deptName;

    /** 显示排序。 */
    private Integer orderNum;

    /** 负责人。 */
    private String leader;

    /** 联系电话。 */
    private String phone;

    /** 邮箱。 */
    private String email;

    /** 状态：0 正常，1 停用。 */
    private String status;

    /** 备注。 */
    private String remark;

    /** 创建者。 */
    private String createBy;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 子部门列表；叶子节点为 {@code []}。 */
    private List<SysDeptTreeVo> children = new ArrayList<>();
}
