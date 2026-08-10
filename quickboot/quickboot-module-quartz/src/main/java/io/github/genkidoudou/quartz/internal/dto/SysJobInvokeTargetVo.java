package io.github.genkidoudou.quartz.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 可选的定时任务调用目标（Spring Bean 名）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "定时任务调用目标候选项")
public class SysJobInvokeTargetVo {

    @Schema(description = "Bean 名称，写入 invoke_target")
    private String beanName;

    @Schema(description = "展示标签")
    private String label;
}
