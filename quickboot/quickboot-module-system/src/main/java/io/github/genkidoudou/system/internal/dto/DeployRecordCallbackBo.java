package io.github.genkidoudou.system.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Jenkins 发布成功回调入参。
 */
@Data
@Schema(description = "发布记录回调")
public class DeployRecordCallbackBo {

  /** 应用名，如 quickboot。 */
  @NotBlank
  @Schema(description = "应用名")
  private String appName;

  /** 环境。 */
  @NotBlank
  @Schema(description = "环境")
  private String env;

  /** deploy / rollback。 */
  @NotBlank
  @Schema(description = "操作")
  private String operate;

  /** Git 分支。 */
  @Schema(description = "分支")
  private String branch;

  /** 主机列表。 */
  @Schema(description = "主机")
  private String hosts;

  /** 构建号。 */
  @Schema(description = "构建号")
  private String buildNumber;

  /** 构建 URL。 */
  @Schema(description = "构建 URL")
  private String buildUrl;

  /** Git commit。 */
  @Schema(description = "Git commit")
  private String gitCommit;

  /** 发版说明。 */
  @Schema(description = "发版说明")
  private String releaseNotes;
}
