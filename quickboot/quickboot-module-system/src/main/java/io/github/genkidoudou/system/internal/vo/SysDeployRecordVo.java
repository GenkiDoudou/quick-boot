package io.github.genkidoudou.system.internal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发布记录列表 / 详情 VO。
 */
@Data
@Schema(description = "发布记录")
public class SysDeployRecordVo {

  /** 主键。 */
  private Long recordId;

  /** 应用名。 */
  private String appName;

  /** 环境。 */
  private String env;

  /** 操作。 */
  private String operate;

  /** 分支。 */
  private String branch;

  /** 主机。 */
  private String hosts;

  /** 构建号。 */
  private String buildNumber;

  /** 构建 URL。 */
  private String buildUrl;

  /** Git commit。 */
  private String gitCommit;

  /** 发版说明。 */
  private String releaseNotes;

  /** 状态：0=成功。 */
  private String status;

  /** 创建时间。 */
  private LocalDateTime createTime;

  /** 列表筛选：应用名（模糊）。 */
  @Schema(hidden = true)
  private String appNameLike;
}
