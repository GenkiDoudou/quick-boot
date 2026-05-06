package io.github.genkidoudou.common.security.firewall.sensitiveword;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 从 {@link ResourceLoader} 加载词表行：跳过空行与 {@code #} 注释。
 */
final class SensitiveWordListLoader {

    private SensitiveWordListLoader() {
    }

    /**
     * 加载多个资源并合并为去重前的线性表（顺序：按路径顺序追加）。
     */
    static List<String> loadAll(ResourceLoader resourceLoader, List<String> locations) throws IOException {
        if (locations == null || locations.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> all = new ArrayList<>();
        for (String loc : locations) {
            if (loc == null || loc.isBlank()) {
                continue;
            }
            all.addAll(loadOne(resourceLoader, loc.trim()));
        }
        return all;
    }

    private static List<String> loadOne(ResourceLoader resourceLoader, String location) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new IOException("敏感词资源不存在: " + location);
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) {
                    continue;
                }
                lines.add(t);
            }
        }
        return lines;
    }
}
