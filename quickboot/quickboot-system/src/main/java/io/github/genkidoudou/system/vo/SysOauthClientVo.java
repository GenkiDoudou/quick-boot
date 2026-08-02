package io.github.genkidoudou.system.vo;

import cn.hutool.core.date.DatePattern;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OAuth 客户端 VO。
 */
@ExcelIgnoreUnannotated
@Data
public class SysOauthClientVo implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * 管理端主键；JSON 按字符串输出，避免前端雪花精度丢失
   */
  @NotNull(message = "主键不能为空", groups = UpdateGroup.class)
  @Null(message = "新增时主键必须为空", groups = AddGroup.class)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Long id;

  @ExcelProperty(value = "客户端id", index = 0)
  @NotBlank(message = "客户端id不能为空", groups = {AddGroup.class, UpdateGroup.class})
  private String clientId;

  @Null(message = "客户端密钥必须为空", groups = AddGroup.class)
  private String clientSecret;

  @ExcelProperty(value = "客户端名称", index = 1)
  @NotBlank(message = "客户端名称不能为空", groups = AddGroup.class)
  private String clientName;

  /**
   * 允许访问的接口 path（Ant 风格，如 /system/**），逗号分隔
   */
  @ExcelProperty(value = "允许访问的接口", index = 2)
  @NotBlank(message = "允许访问的接口不能为空", groups = AddGroup.class)
  private String apiPathPatterns;

  /**
   * token 有效时间（秒）；空则走全局 sa-token 配置
   */
  @ExcelProperty(value = "token有效时间", index = 3)
  private Long tokenTimeout;

  /**
   * 是否校验验证码：{@code 0}=否，{@code 1}=是
   */
  @ExcelProperty(value = "是否校验验证码", index = 4)
  private String checkCaptcha;

  @ExcelProperty(value = "状态", index = 5)
  @NotBlank(message = "状态不能为空", groups = AddGroup.class)
  private String status;

  private String remark;

  @ExcelProperty(value = "创建时间", index = 6)
  @com.alibaba.excel.annotation.format.DateTimeFormat(value = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
  @JsonFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
  @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
  private LocalDateTime createTime;


  /**
   * 勾选主键；非空时忽略搜索条件
   */
  private List<Long> ids;
}
