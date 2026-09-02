package io.github.genkidoudou.quartz.internal.support;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 脚本任务持久化在 {@code params} 列中的 JSON 载荷（不含 scriptPath，路径存 invoke_target）。
 */
@Data
public class JobScriptParamsPayload {

    private List<String> args = new ArrayList<>();

    private String workDir;

    private Integer timeoutSec = 60;
}
