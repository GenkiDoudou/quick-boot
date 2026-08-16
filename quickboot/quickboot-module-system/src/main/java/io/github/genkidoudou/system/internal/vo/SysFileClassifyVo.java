package io.github.genkidoudou.system.internal.vo;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件分类管理 VO。
 */
@Data
public class SysFileClassifyVo implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 分类主键。 */
  @NotNull(message = "主键不能为空", groups = UpdateGroup.class)
  @Null(message = "新增时主键必须为空", groups = AddGroup.class)
  private Long classifyId;

  /** 分类键（唯一，创建后不可改）。 */
  @NotBlank(message = "分类键不能为空", groups = AddGroup.class)
  private String classify;

  /** 展示名称。 */
  @NotBlank(message = "展示名不能为空", groups = {AddGroup.class, UpdateGroup.class})
  private String classifyName;

  /** 允许后缀，逗号分隔；空则用默认白名单。 */
  private String limitExt;

  /** 单文件大小上限（字节）。 */
  private Long limitSizeBytes;

  /** 单次最多上传文件数。 */
  private Integer limitCount;

  /**
   * 压缩配置位：{@code 0}/{@code 1}
   */
  private String compressEnabled;

  /** 超过该 KB 才压缩；空则用默认 200 */
  private Integer compressMinSizeKb;

  /** JPEG 质量 0.10–1.00；空则用默认 0.85 */
  private Float compressQuality;

  /** 最长边像素；空则用默认 1920；0 表示不限制 */
  private Integer compressMaxEdge;

  /**
   * 匿名上传：{@code 0}/{@code 1}
   */
  private String anonymous;

  /**
   * 状态：{@code 0} 正常 / {@code 1} 停用
   */
  private String status;

  /** 备注。 */
  private String remark;
}
