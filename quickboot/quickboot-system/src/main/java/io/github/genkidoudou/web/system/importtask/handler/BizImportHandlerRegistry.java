package io.github.genkidoudou.web.system.importtask.handler;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link BizImportHandler} 注册表。
 */
@Component
public class BizImportHandlerRegistry {

    private final Map<String, BizImportHandler> byBizType;

    public BizImportHandlerRegistry(List<BizImportHandler> handlers) {
        Map<String, BizImportHandler> map = new HashMap<>();
        if (handlers != null) {
            for (BizImportHandler h : handlers) {
                map.put(h.bizType(), h);
            }
        }
        this.byBizType = Map.copyOf(map);
    }

    public BizImportHandler require(String bizType) {
        BizImportHandler h = byBizType.get(bizType);
        if (h == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "未注册的导入业务编码: " + bizType);
        }
        return h;
    }

    public boolean contains(String bizType) {
        return byBizType.containsKey(bizType);
    }
}
