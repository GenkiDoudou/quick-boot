package io.github.genkidoudou.common.excel;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认 Excel 读取结果。
 */
@Data
public class DefaultExcelResult<T> implements ExcelResult<T> {

  @Schema(description = "总条数")
  private Long total;
  @Schema(description = "成功条数")
  private Long successCount;
  @Schema(description = "失败条数")
  private Long failCount;
  @Schema(description = "失败行号（逗号分隔）")
  private String failRows;



  private List<String> errorList = new ArrayList<>();

  @Override
  public String getAnalysis() {
    if (failCount == 0) {
      return StrUtil.format("全部读取成功，共{}条", successCount);
    }
    return StrUtil.format("读取完成，成功{}条，失败{}条", successCount, failCount);
  }
}

