package io.github.genkidoudou.quartz.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地脚本定时任务结构化配置。
 */
@Data
public class JobScriptConfigBo {

    @NotBlank(message = "脚本路径不能为空")
    @Size(max = 500, message = "脚本路径过长")
    private String scriptPath;

    /** 脚本参数列表。 */
    private List<String> args = new ArrayList<>();

    /** 工作目录，空则使用脚本所在目录。 */
    private String workDir;

    /** 超时秒数，默认 60。 */
    private Integer timeoutSec = 60;
}
