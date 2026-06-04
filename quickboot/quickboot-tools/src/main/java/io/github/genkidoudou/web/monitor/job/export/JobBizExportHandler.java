package io.github.genkidoudou.web.monitor.job.export;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.monitor.job.dto.SysJobQueryBo;
import io.github.genkidoudou.web.monitor.job.service.SysJobService;
import io.github.genkidoudou.web.system.exporttask.handler.BizExportHandler;
import org.springframework.stereotype.Component;

/**
 * 定时任务导出 {@code monitor:job}。
 */
@Component
public class JobBizExportHandler implements BizExportHandler {

    public static final String BIZ_TYPE = "monitor:job";

    private final SysJobService jobService;

    public JobBizExportHandler(SysJobService jobService) {
        this.jobService = jobService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public long countRows(String queryJson) {
        return jobService.countExportRows(parseQuery(queryJson));
    }

    @Override
    public byte[] writeExcelBytes(String queryJson, int maxRows) {
        return jobService.exportExcelBytes(parseQuery(queryJson), maxRows);
    }

    @Override
    public String defaultFileName() {
        return "job-export.xlsx";
    }

    private SysJobQueryBo parseQuery(String queryJson) {
        return JSONUtil.toBean(queryJson, SysJobQueryBo.class);
    }
}
