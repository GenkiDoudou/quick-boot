package io.github.genkidoudou.system.internal.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * OAuth 客户端导入失败明细行。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysOauthClientImportErrorRow implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @ExcelProperty("行号")
  private Integer rowNum;

  @ExcelProperty("客户端id")
  private String clientId;

  @ExcelProperty("失败原因")
  private String reason;
}
