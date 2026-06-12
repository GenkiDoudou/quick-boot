package io.github.genkidoudou.web.knowledge.ingest.source;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.constants.KbDocSourceType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 按 {@code source_type} 路由到对应 {@link DocumentSourceAdapter}。
 */
@Component
public class DocumentSourceAdapterRegistry {

    private final Map<String, DocumentSourceAdapter> adapters;
    private final DocumentSourceAdapter defaultAdapter;

    public DocumentSourceAdapterRegistry(List<DocumentSourceAdapter> adapterList) {
        Map<String, DocumentSourceAdapter> map = new HashMap<>();
        DocumentSourceAdapter fileAdapter = null;
        for (DocumentSourceAdapter adapter : adapterList) {
            map.put(adapter.sourceType(), adapter);
            if (KbDocSourceType.FILE.equals(adapter.sourceType())) {
                fileAdapter = adapter;
            }
        }
        this.adapters = Map.copyOf(map);
        this.defaultAdapter = fileAdapter != null ? fileAdapter : adapterList.get(0);
    }

    /**
     * 解析来源适配器；空或未知类型回退为 FILE 适配器（兼容 P0 历史数据）。
     *
     * @param sourceType 文档来源类型
     * @return 适配器实例
     */
    public DocumentSourceAdapter resolve(String sourceType) {
        if (StrUtil.isBlank(sourceType)) {
            return defaultAdapter;
        }
        DocumentSourceAdapter adapter = adapters.get(sourceType);
        if (adapter == null) {
            return defaultAdapter;
        }
        return adapter;
    }
}
