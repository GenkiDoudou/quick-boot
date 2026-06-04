package io.github.genkidoudou.web.system.exporttask.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.servlet.ServletUtils;
import io.github.genkidoudou.web.system.exporttask.dto.ExportSubmitResultVo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 将 {@link ExportSubmitOutcome} 写入 HTTP 响应（同步 xlsx 或异步 JSON）。
 */
@UtilityClass
public class ExportSubmitResponseWriter {

    /**
     * 按编排结果写出响应体。
     */
    public static void write(HttpServletResponse response, ExportSubmitOutcome outcome) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        if (outcome instanceof ExportSubmitOutcome.SyncStream sync) {
            writeSyncStream(response, sync.bytes(), sync.fileName());
            return;
        }
        if (outcome instanceof ExportSubmitOutcome.AsyncAccepted async) {
            writeAsyncJson(response, async.vo());
        }
    }

    private static void writeSyncStream(HttpServletResponse response, byte[] bytes, String fileName)
        throws IOException {
        String baseName = StrUtil.blankToDefault(fileName, "export");
        if (baseName.endsWith(".xlsx")) {
            baseName = baseName.substring(0, baseName.length() - 5);
        }
        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        ExcelUtils.setAttachmentResponseHeader(response, ExcelUtils.encodingFilename(baseName));
        try (OutputStream os = response.getOutputStream()) {
            os.write(bytes);
            os.flush();
        }
    }

    private static void writeAsyncJson(HttpServletResponse response, ExportSubmitResultVo vo) throws IOException {
        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(ServletUtils.CONTENT_TYPE_JSON_UTF8);
        R<ExportSubmitResultVo> body = R.ok(vo);
        resolveObjectMapper().writeValue(response.getWriter(), body);
        response.getWriter().flush();
    }

    private static ObjectMapper resolveObjectMapper() {
        try {
            return SpringUtil.getBean(ObjectMapper.class);
        } catch (Throwable ignored) {
            return new ObjectMapper();
        }
    }
}
