package com.su60.quickboot.common.sensitive;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 从资源路径读取敏感词/关键词文件。
 */
@Slf4j
public class SensitiveWordFileLoader {

    private static final DefaultResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();

    private SensitiveWordFileLoader() {
    }

    public static List<String> load(String path) {
        if (StrUtil.isBlank(path)) {
            return new ArrayList<>();
        }
        try {
            Resource resource = RESOURCE_LOADER.getResource(path);
            if (!resource.exists()) {
                log.warn("敏感词文件不存在: {}", path);
                return new ArrayList<>();
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                List<String> lines = reader.lines()
                        .map(String::trim)
                        .filter(StrUtil::isNotBlank)
                        .distinct()
                        .collect(Collectors.toList());
                return CollUtil.isEmpty(lines) ? new ArrayList<>() : lines;
            }
        } catch (Exception e) {
            log.error("读取敏感词文件失败: {}", path, e);
            return new ArrayList<>();
        }
    }
}
