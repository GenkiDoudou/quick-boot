package io.github.genkidoudou.quartz.internal.support;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.quartz.internal.config.JobMonitorProperties;
import io.github.genkidoudou.quartz.internal.dto.JobHttpConfigBo;
import io.github.genkidoudou.quartz.internal.dto.JobScriptConfigBo;
import io.github.genkidoudou.quartz.internal.dto.SysJobSaveBo;
import io.github.genkidoudou.quartz.api.ITask;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 定时任务保存前校验：Cron、Bean 存在性、HTTP SSRF、脚本白名单等。
 */
@Component
public class JobPayloadValidator {

    private static final Set<String> ALLOWED_HTTP_METHODS = Set.of(
        "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"
    );

    private final ApplicationContext applicationContext;
    private final JobMonitorProperties properties;

    public JobPayloadValidator(ApplicationContext applicationContext, JobMonitorProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    /**
     * 校验保存载荷；不通过则抛出 {@link WarningException}。
     */
    public void validate(SysJobSaveBo bo) {
        JobType type = JobType.fromCode(bo.getJobType());
        if (type == null) {
            throw WarningException.literal(ErrorCodes.Job.JOB_TYPE_INVALID, "不支持的任务类型");
        }
        switch (type) {
            case BEAN -> validateBean(bo);
            case HTTP -> validateHttp(bo.getHttpConfig());
            case SCRIPT -> validateScript(bo.getScriptConfig());
        }
    }

    /**
     * 执行前再次校验 HTTP URL（防止配置变更后仍执行旧任务）。
     */
    public void validateHttpUrl(String url) {
        if (!properties.getHttp().isEnabled()) {
            throw WarningException.literal(ErrorCodes.Job.HTTP_JOB_DISABLED, "HTTP 定时任务未启用");
        }
        assertHttpUrlAllowed(url);
    }

    /**
     * 执行前再次校验脚本路径。
     */
    public void validateScriptPath(String scriptPath) {
        if (!properties.getScript().isEnabled()) {
            throw WarningException.literal(ErrorCodes.Job.SCRIPT_JOB_DISABLED, "脚本定时任务未启用，请在配置中开启 qc.monitor.job.script.enabled");
        }
        assertScriptPathAllowed(scriptPath);
    }

    private void validateBean(SysJobSaveBo bo) {
        if (StrUtil.isBlank(bo.getInvokeTarget())) {
            throw WarningException.literal(ErrorCodes.Job.INVOKE_TARGET_NOT_FOUND, "调用目标 Bean 不能为空");
        }
        Object bean;
        try {
            bean = applicationContext.getBean(bo.getInvokeTarget().trim());
        } catch (Exception e) {
            bean = null;
        }
        if (bean == null) {
            throw WarningException.literal(ErrorCodes.Job.INVOKE_TARGET_NOT_FOUND, "调用目标 Bean 不存在");
        }
        if (!(bean instanceof ITask)) {
            throw WarningException.literal(ErrorCodes.Job.INVOKE_TARGET_NOT_TASK, "调用目标必须实现 ITask 接口");
        }
    }

    private void validateHttp(JobHttpConfigBo config) {
        if (!properties.getHttp().isEnabled()) {
            throw WarningException.literal(ErrorCodes.Job.HTTP_JOB_DISABLED, "HTTP 定时任务未启用");
        }
        if (config == null || StrUtil.isBlank(config.getUrl())) {
            throw WarningException.literal(ErrorCodes.Job.JOB_PARAMS_INVALID, "HTTP 请求 URL 不能为空");
        }
        String method = StrUtil.blankToDefault(config.getMethod(), "GET").trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_HTTP_METHODS.contains(method)) {
            throw WarningException.literal(ErrorCodes.Job.JOB_PARAMS_INVALID, "不支持的 HTTP 方法: " + method);
        }
        assertHttpUrlAllowed(config.getUrl().trim());
        if (config.getTimeoutMs() != null && (config.getTimeoutMs() < 1000 || config.getTimeoutMs() > 300_000)) {
            throw WarningException.literal(ErrorCodes.Job.JOB_PARAMS_INVALID, "HTTP 超时应在 1000~300000 毫秒之间");
        }
    }

    private void validateScript(JobScriptConfigBo config) {
        if (!properties.getScript().isEnabled()) {
            throw WarningException.literal(ErrorCodes.Job.SCRIPT_JOB_DISABLED,
                "脚本定时任务未启用，请在配置中开启 qc.monitor.job.script.enabled");
        }
        if (config == null || StrUtil.isBlank(config.getScriptPath())) {
            throw WarningException.literal(ErrorCodes.Job.JOB_PARAMS_INVALID, "脚本路径不能为空");
        }
        assertScriptPathAllowed(config.getScriptPath().trim());
        if (config.getTimeoutSec() != null && (config.getTimeoutSec() < 1 || config.getTimeoutSec() > 3600)) {
            throw WarningException.literal(ErrorCodes.Job.JOB_PARAMS_INVALID, "脚本超时应在 1~3600 秒之间");
        }
    }

    private void assertHttpUrlAllowed(String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (Exception e) {
            throw WarningException.literal(ErrorCodes.Job.JOB_PARAMS_INVALID, "URL 格式不正确");
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw WarningException.literal(ErrorCodes.Job.JOB_PARAMS_INVALID, "仅支持 http/https URL");
        }
        String host = uri.getHost();
        if (StrUtil.isBlank(host)) {
            throw WarningException.literal(ErrorCodes.Job.JOB_PARAMS_INVALID, "URL 缺少主机名");
        }
        if (properties.getHttp().isBlockPrivateNetwork() && isPrivateOrLocalHost(host)) {
            throw WarningException.literal(ErrorCodes.Job.HTTP_JOB_SSRF_BLOCKED, "禁止访问内网或本机地址: " + host);
        }
        if (!properties.getHttp().getAllowedHosts().isEmpty()) {
            String normalized = host.toLowerCase(Locale.ROOT);
            boolean allowed = properties.getHttp().getAllowedHosts().stream()
                .map(h -> h.toLowerCase(Locale.ROOT))
                .anyMatch(h -> h.equals(normalized) || normalized.endsWith("." + h));
            if (!allowed) {
                throw WarningException.literal(ErrorCodes.Job.HTTP_JOB_SSRF_BLOCKED, "主机不在 HTTP 白名单: " + host);
            }
        }
    }

    private void assertScriptPathAllowed(String scriptPath) {
        Path path = Paths.get(scriptPath).toAbsolutePath().normalize();
        if (!path.toFile().exists()) {
            throw WarningException.literal(ErrorCodes.Job.SCRIPT_JOB_PATH_DENIED, "脚本文件不存在: " + path);
        }
        if (!path.toFile().isFile()) {
            throw WarningException.literal(ErrorCodes.Job.SCRIPT_JOB_PATH_DENIED, "脚本路径必须是文件: " + path);
        }
        if (properties.getScript().getAllowedDirs().isEmpty()) {
            throw WarningException.literal(ErrorCodes.Job.SCRIPT_JOB_PATH_DENIED,
                "未配置脚本目录白名单 qc.monitor.job.script.allowed-dirs");
        }
        boolean underAllowed = false;
        for (String dir : properties.getScript().getAllowedDirs()) {
            if (StrUtil.isBlank(dir)) {
                continue;
            }
            Path allowed = Paths.get(dir.trim()).toAbsolutePath().normalize();
            if (path.startsWith(allowed)) {
                underAllowed = true;
                break;
            }
        }
        if (!underAllowed) {
            throw WarningException.literal(ErrorCodes.Job.SCRIPT_JOB_PATH_DENIED, "脚本不在允许目录内: " + path);
        }
    }

    /**
     * 解析期望 HTTP 状态码集合。
     */
    public static Set<Integer> parseExpectStatus(String expectStatus) {
        Set<Integer> codes = new HashSet<>();
        if (StrUtil.isBlank(expectStatus)) {
            codes.add(200);
            return codes;
        }
        for (String part : expectStatus.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                codes.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException ignored) {
                // 跳过非法片段
            }
        }
        if (codes.isEmpty()) {
            codes.add(200);
        }
        return codes;
    }

    private static boolean isPrivateOrLocalHost(String host) {
        String lower = host.toLowerCase(Locale.ROOT);
        if ("localhost".equals(lower) || lower.endsWith(".localhost")) {
            return true;
        }
        if (NetUtil.isInnerIP(host)) {
            return true;
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            return Arrays.stream(addresses).anyMatch(addr ->
                addr.isAnyLocalAddress()
                    || addr.isLoopbackAddress()
                    || addr.isLinkLocalAddress()
                    || addr.isSiteLocalAddress()
                    || NetUtil.isInnerIP(addr.getHostAddress())
            );
        } catch (UnknownHostException e) {
            return true;
        }
    }
}
