package io.github.genkidoudou.web.system.dict.type.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 字典类型实体。 */
@Data
@TableName("sys_dict_type")
public class SysDictType implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @TableId(value = "dict_id", type = IdType.ASSIGN_ID)
    private Long dictId;
    private String dictName;
    private String dictType;
    private String status;
    private String remark;
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
