package io.github.genkidoudou.system.internal.vo;

import io.github.genkidoudou.common.file.url.FileUrl;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件管理列表 / 详情 VO。
 */
@Data
public class SysFileVo implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 文件主键。 */
  private Long fileId;

  /** 存储相对路径；序列化时拼装可访问 URL */
  @FileUrl
  private String relativePath;

  /** 原始文件名。 */
  private String originalName;

  /** 上传分类键。 */
  private String classify;

  /** 扩展名（小写、无点）。 */
  private String ext;

  /** 文件大小（字节）。 */
  private Long sizeBytes;

  /** Content-Type。 */
  private String contentType;

  /** 上传人用户 ID。 */
  private Long uploaderUserId;

  /** 上传人用户名。 */
  private String uploaderUserName;

  /** 上传时间。 */
  private LocalDateTime uploadTime;
}
