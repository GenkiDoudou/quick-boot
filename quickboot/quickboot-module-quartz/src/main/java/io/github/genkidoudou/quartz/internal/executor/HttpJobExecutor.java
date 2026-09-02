package io.github.genkidoudou.quartz.internal.executor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.quartz.internal.quartz.JobTaskSnapshot;
import io.github.genkidoudou.quartz.internal.support.JobHttpParamsPayload;
import io.github.genkidoudou.quartz.internal.support.JobPayloadValidator;
import io.github.genkidoudou.quartz.internal.support.JobType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Set;

/**
 * HTTP 模式：出站 REST 调用；URL 存 {@code invoke_target}，其余配置存 {@code params} JSON。
 */
@Component
@RequiredArgsConstructor
public class HttpJobExecutor implements JobExecutor {

    private final JobPayloadValidator payloadValidator;

    @Override
    public boolean supports(JobType jobType) {
        return jobType == JobType.HTTP;
    }

    @Override
    public void execute(JobTaskSnapshot snapshot) {
        String url = StrUtil.trim(snapshot.getInvokeTarget());
        payloadValidator.validateHttpUrl(url);
        JobHttpParamsPayload cfg = parseParams(snapshot.getParams());
        int timeoutMs = cfg.getTimeoutMs() != null && cfg.getTimeoutMs() > 0 ? cfg.getTimeoutMs() : 30_000;
        RestClient client = buildClient(timeoutMs);
        HttpMethod method = HttpMethod.valueOf(StrUtil.blankToDefault(cfg.getMethod(), "GET").toUpperCase());
        RestClient.RequestBodySpec spec = client.method(method).uri(url);
        if (cfg.getHeaders() != null) {
            for (JobHttpParamsPayload.JobHeaderEntry h : cfg.getHeaders()) {
                if (h != null && StrUtil.isNotBlank(h.getKey())) {
                    spec = spec.header(h.getKey(), h.getValue() != null ? h.getValue() : "");
                }
            }
        }
        RestClient.ResponseSpec response;
        if (requiresBody(method) && StrUtil.isNotBlank(cfg.getBody())) {
            response = spec.body(cfg.getBody()).retrieve();
        } else {
            response = spec.retrieve();
        }
        int status = response.toBodilessEntity().getStatusCode().value();
        Set<Integer> expected = JobPayloadValidator.parseExpectStatus(cfg.getExpectStatus());
        if (!expected.contains(status)) {
            throw WarningException.literal(ErrorCodes.Job.JOB_PARAMS_INVALID,
                "HTTP 响应状态码 " + status + " 不在期望范围: " + expected);
        }
    }

    private static JobHttpParamsPayload parseParams(String params) {
        if (StrUtil.isBlank(params) || !JSONUtil.isTypeJSON(params)) {
            return new JobHttpParamsPayload();
        }
        return JSONUtil.toBean(params, JobHttpParamsPayload.class);
    }

    private static RestClient buildClient(int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder().requestFactory(factory).build();
    }

    private static boolean requiresBody(HttpMethod method) {
        return method == HttpMethod.POST
            || method == HttpMethod.PUT
            || method == HttpMethod.PATCH;
    }
}
