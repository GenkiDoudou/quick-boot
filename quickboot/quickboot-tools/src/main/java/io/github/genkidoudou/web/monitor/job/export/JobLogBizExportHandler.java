package io.github.genkidoudou.web.monitor.job.export;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.monitor.job.dto.SysJobLogQueryBo;
import io.github.genkidoudou.web.monitor.job.service.SysJobLogService;
import io.github.genkidoudou.web.system.exporttask.handler.BizExportHandler;
import org.springframework.stereotype.Component;

/**
 * 调度日志导出 {@code monitor:jobLog}。
 */
@Component
public class JobLogBizExportHandler implements BizExportHandler {

    public static final String BIZ_TYPE = "monitor:jobLog";

    private final SysJobLogService jobLogService;

    public JobLogBizExportHandler(SysJobLogService jobLogService) {
        this.jobLogService = jobLogService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public long countRows(String queryJson) {
        return jobLogService.countExportRows(parseQuery(queryJson));
    }

    @Override
    public byte[] writeExcelBytes(String queryJson, int maxRows) {
        return jobLogService.exportExcelBytes(parseQuery(queryJson), maxRows);
    }

    @Override
    public String defaultFileName() {
        return "job-log-export.xlsx";
    }

    private SysJobLogQueryBo parseQuery(String queryJson) {
        return JSONUtil.toBean(queryJson, SysJobLogQueryBo.class);
    }
}
