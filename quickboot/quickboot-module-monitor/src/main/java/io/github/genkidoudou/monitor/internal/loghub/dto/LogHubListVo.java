package io.github.genkidoudou.monitor.internal.loghub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志中心列表响应：多来源合并后的近似分页结果。
 */
@Data
@Schema(description = "日志中心列表响应")
public class LogHubListVo {
  /** 是否为近似分页（内存合并后截断） */
  private boolean approximate = true;
  /** 合并后的日志行 */
  private List<LogHubRowVo> rows = new ArrayList<>();
}
