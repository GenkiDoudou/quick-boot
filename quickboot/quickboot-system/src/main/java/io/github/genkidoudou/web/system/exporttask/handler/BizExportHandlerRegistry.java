package io.github.genkidoudou.web.system.exporttask.handler;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link BizExportHandler} 注册表。
 */
@Component
public class BizExportHandlerRegistry {

    private final Map<String, BizExportHandler> byBizType;

    public BizExportHandlerRegistry(List<BizExportHandler> handlers) {
        Map<String, BizExportHandler> map = new HashMap<>();
        if (handlers != null) {
            for (BizExportHandler h : handlers) {
                map.put(h.bizType(), h);
            }
        }
        this.byBizType = Map.copyOf(map);
    }

    public BizExportHandler require(String bizType) {
        BizExportHandler h = byBizType.get(bizType);
        if (h == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "未注册的导出业务编码: " + bizType);
        }
        return h;
    }
}
