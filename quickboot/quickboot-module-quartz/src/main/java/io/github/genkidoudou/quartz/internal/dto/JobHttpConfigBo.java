package io.github.genkidoudou.quartz.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 定时任务结构化配置（前端表单 ↔ 持久化前的业务对象）。
 */
@Data
public class JobHttpConfigBo {

    @NotBlank(message = "请求 URL 不能为空")
    @Size(max = 500, message = "请求 URL 过长")
    private String url;

    /** GET / POST / PUT / DELETE / PATCH，默认 GET。 */
    private String method = "GET";

    /** 可选请求头列表。 */
    private List<JobHeaderBo> headers = new ArrayList<>();

    /** 请求体（POST/PUT/PATCH 等）。 */
    private String body;

    /** 超时毫秒，默认 30000。 */
    private Integer timeoutMs = 30_000;

    /** 期望 HTTP 状态码，逗号分隔，默认 200。 */
    private String expectStatus = "200";
}
