package io.github.genkidoudou.quartz.internal.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 调度日志分页查询。
 */
@Data
public class SysJobLogQueryBo {

    @Min(1)
    private Integer pageNum = 1;

    @Min(1)
    private Integer pageSize = 10;

    private String jobName;

    private String jobGroup;

    private String status;

    private String beginTime;

    private String endTime;
}
