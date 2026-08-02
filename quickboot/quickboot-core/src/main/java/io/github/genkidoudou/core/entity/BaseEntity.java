package io.github.genkidoudou.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {


  @TableLogic
  private String delFlag;

  private String remark;

  @TableField(fill = FieldFill.INSERT)
  private String createBy;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private String updateBy;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}
