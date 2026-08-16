package io.github.genkidoudou.common.excel.listener;

import com.alibaba.excel.read.listener.ReadListener;

/**
 * 统一 Excel 监听器接口。
 */
public interface ExcelListener<T> extends ReadListener<T> {

  /**
   * @return 读取过程中的统计与错误明细
   */
  ExcelResult<T> getExcelResult();
}

