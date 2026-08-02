package io.github.genkidoudou.common.excel.listener;

import java.util.List;

/**
 * Excel 读取结果。
 */
public interface ExcelResult<T> {


  /**
   * 总条数
   *
   * @author luyanan
   * @since 2026/5/10
   */

  Long getTotal();


  void setTotal(Long total);

  /**
   * 成功条数
   *
   * @return
   * @since 2026/5/10
   */
  Long getSuccessCount();

  void setSuccessCount(Long successCount);

  /**
   * 失败条数
   *
   * @return
   * @since 2026/5/10
   */
  Long getFailCount();

  void setFailCount(Long failCount);

  /**
   * 失败明细
   *
   * @since 2026/5/10
   */


  List<String> getErrorList();


  /**
   * 导入回执
   *
   * @return
   * @since 2026/5/10
   */
  String getAnalysis();


  /**
   * 写入错误的文件
   *
   * @return
   * @since 2026/8/2
   */
  void writeErrorFile();
}

