package io.github.genkidoudou.quartz.internal.executor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.quartz.internal.quartz.JobTaskSnapshot;
import io.github.genkidoudou.quartz.internal.support.JobPayloadValidator;
import io.github.genkidoudou.quartz.internal.support.JobScriptParamsPayload;
import io.github.genkidoudou.quartz.internal.support.JobType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 脚本模式：在白名单目录内通过 {@link ProcessBuilder} 执行本地脚本。
 */
@Component
@RequiredArgsConstructor
public class ScriptJobExecutor implements JobExecutor {

    private final JobPayloadValidator payloadValidator;

    @Override
    public boolean supports(JobType jobType) {
        return jobType == JobType.SCRIPT;
    }

    @Override
    public void execute(JobTaskSnapshot snapshot) {
        String scriptPath = StrUtil.trim(snapshot.getInvokeTarget());
        payloadValidator.validateScriptPath(scriptPath);
        JobScriptParamsPayload cfg = parseParams(snapshot.getParams());
        int timeoutSec = cfg.getTimeoutSec() != null && cfg.getTimeoutSec() > 0 ? cfg.getTimeoutSec() : 60;

        List<String> command = new ArrayList<>();
        command.add(scriptPath);
        if (cfg.getArgs() != null) {
            for (String arg : cfg.getArgs()) {
                if (StrUtil.isNotBlank(arg)) {
                    command.add(arg);
                }
            }
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        if (StrUtil.isNotBlank(cfg.getWorkDir())) {
            pb.directory(new File(cfg.getWorkDir().trim()));
        }

        Process process;
        try {
            process = pb.start();
        } catch (Exception e) {
            throw WarningException.literal(ErrorCodes.Job.SCRIPT_JOB_PATH_DENIED,
                "启动脚本失败: " + e.getMessage());
        }

        String output;
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            output = reader.lines().reduce((a, b) -> a + "\n" + b).orElse("");
        } catch (Exception e) {
            process.destroyForcibly();
            throw WarningException.literal(ErrorCodes.Job.SCRIPT_JOB_PATH_DENIED,
                "读取脚本输出失败: " + e.getMessage());
        }

        boolean finished;
        try {
            finished = process.waitFor(timeoutSec, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw WarningException.literal(ErrorCodes.Job.SCRIPT_JOB_PATH_DENIED, "脚本执行被中断");
        }
        if (!finished) {
            process.destroyForcibly();
            throw WarningException.literal(ErrorCodes.Job.SCRIPT_JOB_PATH_DENIED,
                "脚本执行超时（" + timeoutSec + " 秒）");
        }
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            String snippet = StrUtil.sub(output, 0, 500);
            throw WarningException.literal(ErrorCodes.Job.SCRIPT_JOB_PATH_DENIED,
                "脚本退出码 " + exitCode + (StrUtil.isBlank(snippet) ? "" : "，输出: " + snippet));
        }
    }

    private static JobScriptParamsPayload parseParams(String params) {
        if (StrUtil.isBlank(params) || !JSONUtil.isTypeJSON(params)) {
            return new JobScriptParamsPayload();
        }
        return JSONUtil.toBean(params, JobScriptParamsPayload.class);
    }
}
