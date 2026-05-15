package io.github.genkidoudou.common.excel;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Excel 导入结果。
 */
@Data
@Schema(description = "Excel 导入结果")
public class ExcelImportResult {

  @Schema(description = "总条数")
  private Long total;

  @Schema(description = "成功条数")
  private Long successCount;

  @Schema(description = "失败条数")
  private Long failCount;


  @Schema(description = "失败文件名")
  private String errorFileName = "失败导入.txt";

  @Schema(description = "失败文件 Base64")
  private String errorFileBase64;


  public static ExcelImportResult build(ExcelResult excelResult) {
    ExcelImportResult result = new ExcelImportResult();
    result.setTotal(excelResult.getTotal());
    result.setSuccessCount(excelResult.getSuccessCount());
    result.setFailCount(excelResult.getFailCount());
    List<String> errorList = excelResult.getErrorList();
    result.setErrorFileName("失败导入.txt");
    if (CollectionUtil.isNotEmpty(errorList)) {
      String content = StrUtil.join(System.lineSeparator(), errorList);
      result.setErrorFileBase64(Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)));
    } else {
      result.setErrorFileBase64("");
    }
    return result;
  }


}
