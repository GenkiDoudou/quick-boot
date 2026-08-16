package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件管理上传返回值。
 */
@Data
public class SysFileUploadVo implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 文件主键。 */
  private Long fileId;

  /** 原始文件名 */
  private String fileName;

  /** 存储相对路径。 */
  private String relativePath;

  /** 对外可访问的绝对 URL */
  private String absolutePath;

  /** 上传分类键。 */
  private String classify;
}
