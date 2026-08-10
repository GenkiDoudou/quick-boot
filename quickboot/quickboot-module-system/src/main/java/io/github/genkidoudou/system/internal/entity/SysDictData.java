package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据，表 {@code sys_dict_data}。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

  /** 主键。 */
  @TableId(value = "dict_code", type = IdType.ASSIGN_ID)
  private Long dictCode;

  /** 排序。 */
  private Integer dictSort;

  /** 字典标签。 */
  private String dictLabel;

  /** 字典键值。 */
  private String dictValue;

  /** 字典类型。 */
  private String dictType;

  /** CSS 样式类。 */
  private String cssClass;

  /** 回显样式。 */
  private String listClass;

  /** 是否默认：0=否，1=是。 */
  private String isDefault;

  /** 状态：0=正常，1=停用。 */
  private String status;
}
