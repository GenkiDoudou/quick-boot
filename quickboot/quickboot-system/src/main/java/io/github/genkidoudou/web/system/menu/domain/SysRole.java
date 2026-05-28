package io.github.genkidoudou.web.system.menu.domain;

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
 * 角色实体，与表 {@code sys_role} 对应；含数据权限字段 {@code dataScope}，主键插入策略为 ASSIGN_ID。
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "role_id", type = IdType.ASSIGN_ID)
    private Long roleId;

    private String roleName;

    private String roleKey;

    private Integer roleSort;

    private String status;

    private String remark;

    /**
     * 数据范围：1 全部 2 自定义 3 本部门 4 本部门及以下 5 仅本人（与若依语义一致）。
     */
    private String dataScope;

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
