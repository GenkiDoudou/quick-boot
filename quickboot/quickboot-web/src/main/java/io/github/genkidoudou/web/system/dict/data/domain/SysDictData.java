package io.github.genkidoudou.web.system.dict.data.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 字典项实体。 */
@Data
@TableName("sys_dict_data")
public class SysDictData implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @TableId(value = "dict_code", type = IdType.ASSIGN_ID)
    private Long dictCode;
    private Integer dictSort;
    private String dictLabel;
    private String dictValue;
    private String dictType;
    private String cssClass;
    private String listClass;
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
