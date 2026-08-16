package io.github.genkidoudou.common.excel.listener;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import io.github.genkidoudou.common.excel.exception.ExcelException;
import io.github.genkidoudou.common.validation.ValidatorUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 默认回调式 Excel 监听器。
 *
 * @param <T> 行模型类型
 */
@Slf4j
public abstract class ExcelListenerCallback<T> extends AnalysisEventListener<T> implements ExcelListener<T> {

  /**
   * 是否Validator检验，默认为是
   */
  private Boolean isValidate = Boolean.TRUE;

  /**
   * excel 表头数据
   */
  private Map<Integer, String> headMap;

  /**
   * 导入回执
   */
  private ExcelResult<T> excelResult;

  private List<T> dataList = new ArrayList<>();

  private Long failNum = 0L;

  private Long totalNum = 0L;
  /**
   * 每批处理的条数；默认 {@link Long#MAX_VALUE} 表示整表一批。
   */
  private long batchSize = Long.MAX_VALUE;


  /**
   * @param isValidate 是否对每行执行 Bean Validation
   */
  public ExcelListenerCallback(boolean isValidate) {
    this.excelResult = new DefaultExcelResult<>();
    this.isValidate = isValidate;
  }

  /**
   * @param isValidate 是否校验
   * @param batchSize  批大小，{@code null} 时不覆盖默认
   */
  public ExcelListenerCallback(boolean isValidate, Long batchSize) {
    this.excelResult = new DefaultExcelResult<>();
    this.isValidate = isValidate;
    if (null != batchSize) {
      this.batchSize = batchSize;
    }
  }

  @Override
  public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
    this.headMap = headMap;
  }


  @Override
  public void onException(Exception exception, AnalysisContext context) throws Exception {
    String errMsg = null;
    if (exception instanceof ExcelDataConvertException excelDataConvertException) {
      // 如果是某一个单元格的转换异常 能获取到具体行号
      Integer rowIndex = excelDataConvertException.getRowIndex();
      Integer columnIndex = excelDataConvertException.getColumnIndex();
      errMsg = StrUtil.format("第{}行-第{}列-表头{}: 解析异常<br/>",
        rowIndex + 1, columnIndex + 1, headMap.get(columnIndex));
      if (log.isDebugEnabled()) {
        log.error(errMsg);
      }
    }
    if (exception instanceof ConstraintViolationException constraintViolationException) {
      Set<ConstraintViolation<?>> constraintViolations = constraintViolationException.getConstraintViolations();
      String constraintViolationsMsg = Opt.ofEmptyAble(constraintViolations).map(a -> a.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(","))).orElse("");
      errMsg = StrUtil.format("第{}行数据校验异常: {}", context.readRowHolder().getRowIndex() + 1, constraintViolationsMsg);
      if (log.isDebugEnabled()) {
        log.error(errMsg);
      }
    }
    if (exception instanceof ExcelDataCheckException excelDataCheckException) {
      errMsg = StrUtil.format("第{}行数据校验异常: {}", context.readRowHolder().getRowIndex() + 1, excelDataCheckException.getMessage());
      if (log.isDebugEnabled()) {
        log.error(errMsg);
      }
    }
    if (StrUtil.isBlank(errMsg)) {
      errMsg = StrUtil.blankToDefault(exception.getMessage(), "Excel 解析异常");
      if (log.isDebugEnabled()) {
        log.error(errMsg, exception);
      }
    }
    excelResult.getErrorList().add(errMsg);
    failNum++;
  }

  @Override
  public void invoke(T data, AnalysisContext context) {
    totalNum++;
    if (isValidate) {
      ValidatorUtils.validate(data);
    }
    callback(data, context);
    dataList.add(data);
    if (dataList.size() >= batchSize) {
      getList(dataList, context);
    }
    if (dataList.size() >= batchSize) {
      dataList.clear();
    }

  }

  @Override
  public void doAfterAllAnalysed(AnalysisContext context) {
    if (!dataList.isEmpty()) {
      try {
        getList(new ArrayList<>(dataList), context);
      } catch (Exception e) {
        throw new ExcelException("批次收尾处理失败", e);
      } finally {
        dataList.clear();
      }
    }
    log.debug("所有数据解析完成！");
  }

  @Override
  public ExcelResult<T> getExcelResult() {
    excelResult.setFailCount(failNum);
    excelResult.setSuccessCount(totalNum - failNum);
    excelResult.setTotal(totalNum);
    return excelResult;
  }

  /**
   * 行回调，业务可在此做自定义校验与聚合。
   */
  protected abstract void callback(T data, AnalysisContext context);

  /**
   * 批次回调：达到 {@link #batchSize} 或读取结束时触发，由子类实现持久化等逻辑。
   *
   * @param list    本批成功行（已通过校验）
   * @param context EasyExcel 上下文
   */
  protected abstract void getList(List<T> list, AnalysisContext context);
}
