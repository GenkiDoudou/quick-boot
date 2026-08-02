package io.github.genkidoudou.system.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * OAuth 客户端导入行（无 secret / id / createTime）。
 */
@ExcelIgnoreUnannotated
@Data
public class SysOauthClientImportRow implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @ExcelProperty(value = "客户端id", index = 0)
  private String clientId;

  @ExcelProperty(value = "客户端名称", index = 1)
  private String clientName;

  @ExcelProperty(value = "允许访问的接口", index = 2)
  private String apiPathPatterns;

  @ExcelProperty(value = "token有效时间", index = 3)
  private Long tokenTimeout;

  @ExcelProperty(value = "是否校验验证码", index = 4)
  private String checkCaptcha;

  @ExcelProperty(value = "状态", index = 5)
  private String status;

  @ExcelProperty(value = "备注", index = 6)
  private String remark;
}
