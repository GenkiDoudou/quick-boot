package io.github.genkidoudou.common.excel.listener;

import java.util.List;

/**
 * Excel 读取结果。
 */
public interface ExcelResult<T> {


  /**
   * @return 读取总条数（含成功与失败）
   */
  Long getTotal();

  /**
   * @param total 总条数
   */
  void setTotal(Long total);

  /**
   * @return 成功条数
   */
  Long getSuccessCount();

  /**
   * @param successCount 成功条数
   */
  void setSuccessCount(Long successCount);

  /**
   * @return 失败条数
   */
  Long getFailCount();

  /**
   * @param failCount 失败条数
   */
  void setFailCount(Long failCount);

  /**
   * @return 失败明细文案列表（按行）
   */
  List<String> getErrorList();


  /**
   * @return 导入结果摘要文案
   */
  String getAnalysis();


  /**
   * 将 {@link #getErrorList()} 编码为 Base64 错误文件，供前端下载。
   */
  void writeErrorFile();
}

