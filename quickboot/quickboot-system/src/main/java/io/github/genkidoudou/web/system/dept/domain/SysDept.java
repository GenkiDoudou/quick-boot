package io.github.genkidoudou.web.system.dept.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门实体，与表 {@code sys_dept} 对应。
 * <p>
 * {@code parentId = -1} 表示顶级部门；{@code delFlag} 与全局 MP 逻辑删除配置一致（{@code 0} 未删 / {@code 1} 已删）。
 */
@Data
@TableName("sys_dept")
public class SysDept implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 部门主键（插入时由 MP ASSIGN_ID 生成；种子数据可使用固定 id）。 */
    @TableId(value = "dept_id", type = IdType.ASSIGN_ID)
    private Long deptId;

    /** 上级部门 id，顶级为 -1。 */
    private Long parentId;

    /** 部门名称。 */
    private String deptName;

    /** 同级显示排序，数值越小越靠前。 */
    private Integer orderNum;

    /** 负责人（文本）。 */
    private String leader;

    /** 联系电话。 */
    private String phone;

    /** 邮箱。 */
    private String email;

    /** 状态：0 正常，1 停用（与字典 sys_normal_disable 一致）。 */
    private String status;

    /** 备注。 */
    private String remark;

    /** 逻辑删除：0 未删除，1 已删除。 */
    @TableLogic
    private String delFlag;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
