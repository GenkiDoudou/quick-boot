package io.github.genkidoudou.web.monitor.job.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 立即执行任务。
 */
@Data
public class SysJobRunBo {

    @NotNull(message = "任务ID不能为空")
    private Long jobId;
}
