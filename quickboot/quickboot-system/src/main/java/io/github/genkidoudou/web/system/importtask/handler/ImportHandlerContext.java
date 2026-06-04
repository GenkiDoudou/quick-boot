package io.github.genkidoudou.web.system.importtask.handler;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 导入 Handler 共享上下文（预加载缓存等）。
 */
@Data
public class ImportHandlerContext {

    private final Long taskId;
    private final String bizType;
    private final Map<String, Object> attributes = new HashMap<>();

    public ImportHandlerContext(Long taskId, String bizType) {
        this.taskId = taskId;
        this.bizType = bizType;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
}
