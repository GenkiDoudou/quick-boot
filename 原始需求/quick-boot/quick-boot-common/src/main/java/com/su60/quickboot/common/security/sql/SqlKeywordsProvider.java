package com.su60.quickboot.common.security.sql;

import cn.hutool.core.collection.CollUtil;
import com.su60.quickboot.common.security.config.SecurityProperties;
import com.su60.quickboot.common.sensitive.SensitiveWordFileLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SQL 关键词提供者，支持从文件加载并缓存。
 */
@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "security.sql-inject", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SqlKeywordsProvider {

    private final SecurityProperties securityProperties;

    private final AtomicReference<List<String>> cache = new AtomicReference<>();

    public List<String> getKeywords() {
        List<String> keywords = cache.get();
        if (keywords == null) {
            keywords = load();
            cache.set(keywords);
        }
        return keywords;
    }

    public List<String> refresh() {
        List<String> keywords = load();
        cache.set(keywords);
        return keywords;
    }

    private List<String> load() {
        String path = securityProperties.getSqlInject().getKeywordsPath();
        List<String> lines = SensitiveWordFileLoader.load(path);
        if (CollUtil.isEmpty(lines)) {
            log.warn("SQL 关键词文件为空或不存在: {}", path);
        }
        return lines;
    }
}
