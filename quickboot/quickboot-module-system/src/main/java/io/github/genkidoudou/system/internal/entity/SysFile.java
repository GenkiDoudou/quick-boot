package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统文件元数据（管理端登记），表 {@code sys_file}。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_file")
public class SysFile extends BaseEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 主键（雪花） */
  @TableId(value = "file_id", type = IdType.ASSIGN_ID)
  private Long fileId;

  /** 原始文件名 */
  private String originalName;

  /** 扩展名（小写、无点） */
  private String ext;

  /** 大小（字节） */
  private Long sizeBytes;

  /** Content-Type，可空 */
  private String contentType;

  /** 上传分类键 */
  private String classify;

  /** 存储相对路径（唯一） */
  private String relativePath;

  /** 上传人用户 ID（无登录为 0） */
  private Long uploaderUserId;

  /** 上传人用户名 */
  private String uploaderUserName;

  /** 上传时间 */
  private LocalDateTime uploadTime;
}
