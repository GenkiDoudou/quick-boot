package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {

  @TableId(value = "config_id", type = IdType.ASSIGN_ID)
  private Long configId;

  private String configName;

  private String configKey;

  private String configValue;

  /** 系统内置：0=否，1=是。 */
  private String configType;
}
