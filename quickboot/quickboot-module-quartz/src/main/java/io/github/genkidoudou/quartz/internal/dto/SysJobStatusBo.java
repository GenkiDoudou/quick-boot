package io.github.genkidoudou.quartz.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改任务状态。
 */
@Data
public class SysJobStatusBo {

    @NotNull(message = "任务ID不能为空")
    private Long jobId;

    @NotBlank(message = "状态不能为空")
    private String status;
}
