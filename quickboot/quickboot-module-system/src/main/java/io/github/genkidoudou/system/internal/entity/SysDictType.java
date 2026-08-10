package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型，表 {@code sys_dict_type}。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {

  /** 主键。 */
  @TableId(value = "dict_id", type = IdType.ASSIGN_ID)
  private Long dictId;

  /** 字典名称。 */
  private String dictName;

  /** 字典类型（唯一）。 */
  private String dictType;

  /** 状态：0=正常，1=停用。 */
  private String status;
}
