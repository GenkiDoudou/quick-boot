package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件上传分类配置，表 {@code sys_file_classify}。
 * <p>主键 {@code classifyId}；业务键 {@code classify} 创建后不可改。</p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_file_classify")
public class SysFileClassify extends BaseEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 主键（雪花） */
  @TableId(value = "classify_id", type = IdType.ASSIGN_ID)
  private Long classifyId;

  /** 分类键（唯一，创建后不可改；不可含斜杠） */
  private String classify;

  /** 展示名 */
  private String classifyName;

  /** 允许后缀，逗号分隔；空=内置默认白名单 */
  private String limitExt;

  /** 单文件上限（字节） */
  private Long limitSizeBytes;

  /** 单次最多文件数 */
  private Integer limitCount;

  /**
   * 是否开启压缩配置：{@code 0}=否，{@code 1}=是
   */
  private String compressEnabled;

  /** 超过该 KB 才压缩 */
  private Integer compressMinSizeKb;

  /** JPEG 质量 0.10–1.00 */
  private Float compressQuality;

  /** 最长边像素；0 表示不限制边长 */
  private Integer compressMaxEdge;

  /**
   * 是否允许匿名上传：{@code 0}=否，{@code 1}=是
   */
  private String anonymous;

  /**
   * 状态：{@code 0}=正常，{@code 1}=停用
   */
  private String status;
}
