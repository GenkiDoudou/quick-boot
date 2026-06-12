package io.github.genkidoudou.web.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.web.workflow.config.WorkflowProperties;
import io.github.genkidoudou.web.workflow.stream.WorkflowStreamEmitter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 工作流 SSE 流式输出接口。
 */
@Tag(name = "工作流流式输出")
@Validated
@RestController
@RequestMapping("/workflow/run")
@RequiredArgsConstructor
public class WfStreamController {

    private final WorkflowStreamEmitter streamEmitter;
    private final WorkflowProperties properties;

    /**
     * 订阅运行实例的 SSE 事件流。
     *
     * @param runId 运行 ID
     * @return SSE 发射器
     */
    @Operation(summary = "SSE 流式订阅")
    @SaCheckPermission("workflow:run")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
        @Parameter(description = "运行ID") @RequestParam @Min(1) Long runId) {
        SseEmitter emitter = new SseEmitter(properties.getAsyncTimeoutMs());
        streamEmitter.register(runId, emitter);
        return emitter;
    }
}
