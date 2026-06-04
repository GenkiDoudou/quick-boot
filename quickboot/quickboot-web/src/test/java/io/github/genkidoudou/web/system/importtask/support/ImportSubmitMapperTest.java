package io.github.genkidoudou.web.system.importtask.support;

import io.github.genkidoudou.common.excel.ExcelImportResult;
import io.github.genkidoudou.web.system.importtask.dto.ImportSubmitResultVo;
import io.github.genkidoudou.web.system.user.dto.UserImportResultVo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 平台导入结果与业务 VO 映射单测。
 */
class ImportSubmitMapperTest {

    @Test
    void toUserImportResult_syncWithErrorFile_mapsFilePrefix() {
        ImportSubmitResultVo src = new ImportSubmitResultVo();
        src.setMode(ImportMode.SYNC);
        src.setTotal(10L);
        src.setSuccessCount(8L);
        src.setFailCount(2L);
        src.setErrorFileId(99L);

        UserImportResultVo vo = ImportSubmitMapper.toUserImportResult(src);

        assertThat(vo.getTotal()).isEqualTo(10);
        assertThat(vo.getSuccess()).isEqualTo(8);
        assertThat(vo.getFailure()).isEqualTo(2);
        assertThat(vo.getErrorKey()).isEqualTo("file:99");
    }

    @Test
    void toUserImportResult_async_mapsTaskPrefix() {
        ImportSubmitResultVo src = new ImportSubmitResultVo();
        src.setMode(ImportMode.ASYNC);
        src.setTaskId(1001L);
        src.setTotal(5000L);

        UserImportResultVo vo = ImportSubmitMapper.toUserImportResult(src);

        assertThat(vo.getErrorKey()).isEqualTo("task:1001");
        assertThat(vo.getFailureMessages()).anyMatch(m -> m.contains("1001"));
    }

    @Test
    void toExcelImportResult_async_setsTaskId() {
        ImportSubmitResultVo src = new ImportSubmitResultVo();
        src.setMode(ImportMode.ASYNC);
        src.setTaskId(42L);

        ExcelImportResult vo = ImportSubmitMapper.toExcelImportResult(src);

        assertThat(vo.getTaskId()).isEqualTo(42L);
        assertThat(vo.getErrorKey()).isEqualTo("task:42");
    }
}
