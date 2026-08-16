package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统参数配置，表 {@code sys_config}。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {

  /** 参数主键。 */
  @TableId(value = "config_id", type = IdType.ASSIGN_ID)
  private Long configId;

  /** 参数名称。 */
  private String configName;

  /** 参数键名，唯一。 */
  private String configKey;

  /** 参数键值。 */
  private String configValue;

  /** 系统内置：0=否，1=是。 */
  private String configType;
}
