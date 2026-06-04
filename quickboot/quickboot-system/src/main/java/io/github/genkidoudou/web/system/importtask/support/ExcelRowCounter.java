package io.github.genkidoudou.web.system.importtask.support;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.io.InputStream;

/**
 * 仅统计 Excel 有效数据行数（不含表头）。
 */
public final class ExcelRowCounter {

    private ExcelRowCounter() {
    }

    /**
     * @param is       Excel 输入流
     * @param rowClass 行模型（与业务导入一致）
     * @return 数据行数
     */
    public static int countDataRows(InputStream is, Class<?> rowClass) {
        CountListener listener = new CountListener();
        EasyExcel.read(is, rowClass, listener).autoCloseStream(false).sheet().doRead();
        return listener.count;
    }

    private static final class CountListener extends AnalysisEventListener<Object> {
        private int count;

        @Override
        public void invoke(Object data, AnalysisContext context) {
            count++;
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
        }
    }
}
