package io.github.genkidoudou.common.excel.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认 Excel 读取结果。
 */
@Data
public class DefaultExcelResult<T> implements ExcelResult<T> {

  private Long total;
  private Long successCount;
  private Long failCount;
  private String failRows;

  /**
   * 错误明细文件的 Base64 内容（由 {@link #writeErrorFile()} 生成）。
   */
  private String errorFileBase64;


  private String errorFileName;
  private List<String> errorList = new ArrayList<>();

  @Override
  public String getAnalysis() {
    if (failCount == 0) {
      return StrUtil.format("全部读取成功，共{}条", successCount);
    }
    return StrUtil.format("读取完成，成功{}条，失败{}条", successCount, failCount);
  }

  @Override
  public void writeErrorFile() {
    if (CollectionUtil.isNotEmpty(this.errorList)) {
      String string = String.join("\n", this.errorList);
      this.errorFileBase64 = Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
      this.errorFileName = "失败明细.txt";
    }
  }

}

