package io.github.genkidoudou.web.workflow.stream;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 工作流 SSE 事件发布器（P0 单实例内存队列）。
 * <p>
 * 支持 {@code step_start}、{@code llm_delta}、{@code step_end}、{@code done}、{@code error}、{@code heartbeat} 事件。
 */
@Component
public class WorkflowStreamEmitter {

    private final Map<Long, RunChannel> channels = new ConcurrentHashMap<>();

    /**
     * 注册运行实例的 SSE 订阅通道。
     *
     * @param runId   运行 ID
     * @param emitter SSE 发射器
     */
    public void register(Long runId, SseEmitter emitter) {
        RunChannel channel = channels.computeIfAbsent(runId, id -> new RunChannel());
        channel.emitters.add(emitter);
        emitter.onCompletion(() -> channel.emitters.remove(emitter));
        emitter.onTimeout(() -> channel.emitters.remove(emitter));
        emitter.onError(ex -> channel.emitters.remove(emitter));
    }

    /**
     * 推送步骤开始事件。
     */
    public void emitStepStart(Long runId, String nodeId, String nodeType, int orderNo) {
        publish(runId, "step_start", Map.of(
            "runId", runId,
            "nodeId", nodeId,
            "nodeType", nodeType,
            "orderNo", orderNo
        ));
    }

    /**
     * 推送 LLM 流式 delta。
     */
    public void emitLlmDelta(Long runId, String nodeId, String delta, String accumulated) {
        publish(runId, "llm_delta", Map.of(
            "runId", runId,
            "nodeId", nodeId,
            "delta", delta == null ? "" : delta,
            "accumulated", accumulated == null ? "" : accumulated
        ));
    }

    /**
     * 推送循环单轮迭代事件（流式调试）。
     *
     * @param runId      运行 ID
     * @param loopNodeId 循环节点 ID
     * @param iteration  轮次索引（从 0 开始）
     * @param phase      start / end
     * @param payload    附加数据
     */
    public void emitLoopIteration(Long runId, String loopNodeId, int iteration, String phase,
                                    Map<String, Object> payload) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("runId", runId);
        data.put("loopNodeId", loopNodeId);
        data.put("iteration", iteration);
        data.put("phase", phase == null ? "end" : phase);
        if (payload != null) {
            data.putAll(payload);
        }
        publish(runId, "loop_iteration", data);
    }

    /**
     * 推送步骤结束事件。
     */
    public void emitStepEnd(Long runId, String nodeId, String status, long durationMs,
                            Map<String, Object> inputsSummary, Map<String, Object> outputsSummary) {
        publish(runId, "step_end", Map.of(
            "runId", runId,
            "nodeId", nodeId,
            "status", status,
            "durationMs", durationMs,
            "inputs", inputsSummary == null ? Map.of() : inputsSummary,
            "outputs", outputsSummary == null ? Map.of() : outputsSummary
        ));
    }

    /**
     * 推送运行完成事件。
     */
    public void emitDone(Long runId, String status, Map<String, Object> outputs) {
        publish(runId, "done", Map.of(
            "runId", runId,
            "status", status,
            "outputs", outputs == null ? Map.of() : outputs
        ));
        close(runId);
    }

    /**
     * 推送错误事件。
     */
    public void emitError(Long runId, String message, String nodeId) {
        publish(runId, "error", Map.of(
            "runId", runId,
            "message", message == null ? "" : message,
            "nodeId", nodeId == null ? "" : nodeId
        ));
        close(runId);
    }

    /**
     * 推送心跳。
     */
    public void emitHeartbeat(Long runId) {
        publish(runId, "heartbeat", Map.of("ts", System.currentTimeMillis()));
    }

    /**
     * 关闭并移除运行通道。
     *
     * @param runId 运行 ID
     */
    public void close(Long runId) {
        RunChannel channel = channels.remove(runId);
        if (channel != null) {
            for (SseEmitter emitter : channel.emitters) {
                emitter.complete();
            }
            channel.emitters.clear();
        }
    }

    /**
     * 判断运行是否仍有 SSE 订阅者。
     *
     * @param runId 运行 ID
     * @return true 表示有订阅
     */
    public boolean hasSubscribers(Long runId) {
        RunChannel channel = channels.get(runId);
        return channel != null && !channel.emitters.isEmpty();
    }

    private void publish(Long runId, String eventName, Map<String, Object> payload) {
        RunChannel channel = channels.get(runId);
        if (channel == null) {
            return;
        }
        for (SseEmitter emitter : channel.emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException ex) {
                channel.emitters.remove(emitter);
            }
        }
        channel.queue.offer(new StreamEvent(eventName, payload));
    }

    /**
     * 阻塞等待下一个事件（供 SSE Controller 轮询备用）。
     */
    public StreamEvent pollEvent(Long runId, long timeoutMs) throws InterruptedException {
        RunChannel channel = channels.computeIfAbsent(runId, id -> new RunChannel());
        return channel.queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    @Getter
    public static class StreamEvent {
        private final String name;
        private final Map<String, Object> payload;

        public StreamEvent(String name, Map<String, Object> payload) {
            this.name = name;
            this.payload = payload;
        }
    }

    private static final class RunChannel {
        private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
        private final BlockingQueue<StreamEvent> queue = new LinkedBlockingQueue<>();
    }
}
