package com.epsoft.common.excel;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.epsoft.common.utils.JsonUtils;
import com.epsoft.common.utils.StreamUtils;
import com.epsoft.common.utils.ValidatorUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Map;
import java.util.Set;

/**
 * excel监听(带回调)
 *
 * @author luyanan
 * @since 2025/3/6
 */

@Slf4j
public abstract class DefaultExcelListenerCallback<T> extends AnalysisEventListener<T> implements ExcelListener<T> {


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

    public DefaultExcelListenerCallback(boolean isValidate) {
        this.excelResult = new DefautExcelResult<>();
        this.isValidate = isValidate;
    }

    /**
     * 处理异常
     *
     * @param exception ExcelDataConvertException
     * @param context   Excel 上下文
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        String errMsg = null;
        if (exception instanceof ExcelDataConvertException) {
            // 如果是某一个单元格的转换异常 能获取到具体行号
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException) exception;
            Integer rowIndex = excelDataConvertException.getRowIndex();
            Integer columnIndex = excelDataConvertException.getColumnIndex();
            errMsg = StrUtil.format("第{}行-第{}列-表头{}: 解析异常<br/>",
                rowIndex + 1, columnIndex + 1, headMap.get(columnIndex));
            if (log.isDebugEnabled()) {
                log.error(errMsg);
            }
        }
        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) exception;
            Set<ConstraintViolation<?>> constraintViolations = constraintViolationException.getConstraintViolations();
            String constraintViolationsMsg = StreamUtils.join(constraintViolations, ConstraintViolation::getMessage, ", ");
            errMsg = StrUtil.format("第{}行数据校验异常: {}", context.readRowHolder().getRowIndex() + 1, constraintViolationsMsg);
            if (log.isDebugEnabled()) {
                log.error(errMsg);
            }
        }
        if (exception instanceof ExcelDataCheckException) {
            ExcelDataCheckException excelDataCheckException = (ExcelDataCheckException) exception;
            errMsg = StrUtil.format("第{}行数据校验异常: {}", context.readRowHolder().getRowIndex() + 1, excelDataCheckException.getMessage());
            if (log.isDebugEnabled()) {
                log.error(errMsg);
            }
        }
        excelResult.getErrorList().add(errMsg);
//        throw new ExcelAnalysisException(errMsg);
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = headMap;
        log.debug("解析到一条表头数据: {}", JsonUtils.toJsonString(headMap));
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        if (isValidate) {
            ValidatorUtils.validate(data);
        }
        callback(data, context);
        excelResult.getList().add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.debug("所有数据解析完成！");
    }

    @Override
    public ExcelResult<T> getExcelResult() {
        return excelResult;
    }


    protected abstract void callback(T data, AnalysisContext context);
}
