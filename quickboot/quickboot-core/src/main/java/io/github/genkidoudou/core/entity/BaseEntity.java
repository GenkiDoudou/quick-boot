package io.github.genkidoudou.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类：封装逻辑删除、审计字段（创建/更新人与时间）及备注。
 * <p>
 * 审计字段由 {@link io.github.genkidoudou.core.entity.mybatisplis.MyMetaObjectHandler} 在 INSERT/UPDATE 时自动填充。
 */
@Data
public class BaseEntity implements Serializable {


  /** 删除标志：{@code 0}=正常，{@code 1}=已删除（逻辑删除）。 */
  @TableLogic
  private String delFlag;

  /** 备注。 */
  private String remark;

  /** 创建者用户 ID（字符串形式）。 */
  @TableField(fill = FieldFill.INSERT)
  private String createBy;

  /** 创建时间。 */
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  /** 最后更新者用户 ID（字符串形式）。 */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private String updateBy;

  /** 最后更新时间。 */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}
