package io.github.genkidoudou.web.monitor.job.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 定时任务分页查询。
 */
@Data
public class SysJobQueryBo {

    @Min(1)
    private Integer pageNum = 1;

    @Min(1)
    private Integer pageSize = 10;

    private String jobName;

    private String jobGroup;

    private String status;
}
