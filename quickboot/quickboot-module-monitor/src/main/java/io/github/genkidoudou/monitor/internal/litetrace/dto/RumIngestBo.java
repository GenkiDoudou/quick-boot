package io.github.genkidoudou.monitor.internal.litetrace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Lite RUM 批量上报请求体。
 */
@Data
@Schema(description = "Lite RUM 批量上报")
public class RumIngestBo {

    /** 应用标识，须在白名单内 */
    @NotBlank
    @Size(max = 64)
    private String appId;

    /** SDK 版本号 */
    @Size(max = 32)
    private String sdkVersion;

    /** 客户端上报时刻（毫秒时间戳） */
    private Long clientTime;

    /** 环境信息，如 ua、屏幕尺寸等 */
    private Map<String, Object> env;

    /** 事件列表，每项含 type、traceId、page 等字段 */
    @NotEmpty
    private List<Map<String, Object>> events;
}
