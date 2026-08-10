package io.github.genkidoudou.tool.internal.gen.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 将生成结果写入自定义磁盘路径。
 */
@Component
public class GenCodePathWriter {

    /**
     * 写入生成文件。
     *
     * @param genPath 用户配置的根目录（绝对或相对用户目录）
     * @param files   相对路径 -> 内容
     * @return 实际写入的根路径
     */
    public String write(String genPath, Map<String, String> files) throws IOException {
        if (StrUtil.isBlank(genPath)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "自定义路径不能为空");
        }
        if (files == null || files.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "无生成内容");
        }
        Path base = Paths.get(genPath.trim()).normalize().toAbsolutePath();
        if (base.toString().contains("..")) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "非法路径");
        }
        Files.createDirectories(base);
        for (Map.Entry<String, String> entry : files.entrySet()) {
            Path target = base.resolve(entry.getKey().replace('\\', '/')).normalize();
            if (!target.startsWith(base)) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "非法生成路径: " + entry.getKey());
            }
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.writeString(target, entry.getValue(), StandardCharsets.UTF_8);
        }
        return base.toString();
    }
}
