package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Jenkins 发布记录，表 {@code sys_deploy_record}。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_deploy_record")
public class SysDeployRecord extends BaseEntity {

  /** 主键。 */
  @TableId(value = "record_id", type = IdType.ASSIGN_ID)
  private Long recordId;

  /** 应用名，如 quickboot。 */
  private String appName;

  /** 环境：test / prod / dev。 */
  private String env;

  /** 操作：deploy / rollback。 */
  private String operate;

  /** Git 分支。 */
  private String branch;

  /** 部署主机，逗号分隔。 */
  private String hosts;

  /** Jenkins 构建号。 */
  private String buildNumber;

  /** 构建链接。 */
  private String buildUrl;

  /** Git commit。 */
  private String gitCommit;

  /** 发版说明（手填 + git log）。 */
  private String releaseNotes;

  /** 状态：0=成功（本期仅写成功）。 */
  private String status;
}
