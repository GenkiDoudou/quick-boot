package io.github.genkidoudou.system.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * OAuth 客户端同步导入结果（不含 secret）。
 */
@Data
public class SysOauthClientImportResult implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String mode = "sync";

  private long total;

  private long successCount;

  private long failCount;

  private String errorFileBase64;

  private String errorFileName;
}
