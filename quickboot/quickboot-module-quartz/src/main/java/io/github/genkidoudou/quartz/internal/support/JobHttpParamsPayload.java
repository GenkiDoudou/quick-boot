package io.github.genkidoudou.quartz.internal.support;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 任务持久化在 {@code params} 列中的 JSON 载荷（不含 url，url 存 invoke_target）。
 */
@Data
public class JobHttpParamsPayload {

    private String method = "GET";

    private List<JobHeaderEntry> headers = new ArrayList<>();

    private String body;

    private Integer timeoutMs = 30_000;

    private String expectStatus = "200";

    @Data
    public static class JobHeaderEntry {
        private String key;
        private String value;
    }
}
