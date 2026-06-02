package io.github.genkidoudou.web.system.file.support;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.file.FileUploadAfterContext;
import io.github.genkidoudou.common.file.FileUploadHook;
import io.github.genkidoudou.web.system.file.domain.SysFile;
import io.github.genkidoudou.web.system.file.mapper.SysFileMapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 全系统上传文件自动登记：在 {@link io.github.genkidoudou.common.file.FileTemplate#upload} 成功后写入 {@code sys_file}。
 */
@Component
@Order(0)
public class SysFileRegisterHook implements FileUploadHook {

    private final SysFileMapper mapper;

    public SysFileRegisterHook(SysFileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void afterUpload(FileUploadAfterContext ctx) {
        String relativePath = ctx.getRelativePath();
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        // 幂等：relative_path 已存在则跳过（同一路径不应重复登记）
        SysFile exists = mapper.selectOne(
            Wrappers.<SysFile>lambdaQuery().eq(SysFile::getRelativePath, relativePath).last("LIMIT 1")
        );
        if (exists != null) {
            return;
        }

        SysFile row = new SysFile();
        row.setOriginalName(resolveOriginalName(ctx));
        row.setExt(resolveExt(row.getOriginalName()));
        row.setSizeBytes(resolveSizeBytes(ctx));
        row.setContentType(resolveContentType(ctx));
        row.setClassify(ctx.getBefore().getClassify());
        row.setRelativePath(relativePath.trim());
        row.setUploadTime(LocalDateTime.now());
        row.setDeleted(0);
        fillUploader(row);
        try {
            mapper.insert(row);
        } catch (Exception e) {
            // 让 DefaultFileTemplate 感知失败并回滚对象
            throw new WarningException(ErrorCodes.System.INTERNAL_ERROR, "文件登记失败: " + e.getMessage());
        }
    }

    private static String resolveOriginalName(FileUploadAfterContext ctx) {
        var before = ctx.getBefore();
        if (before.getMultipart() != null && before.getMultipart().getOriginalFilename() != null) {
            return before.getMultipart().getOriginalFilename();
        }
        if (before.getFilename() != null) {
            return before.getFilename();
        }
        return "";
    }

    private static Long resolveSizeBytes(FileUploadAfterContext ctx) {
        var before = ctx.getBefore();
        return before.getSize();
    }

    private static String resolveContentType(FileUploadAfterContext ctx) {
        var before = ctx.getBefore();
        return before.getContentType();
    }

    private static String resolveExt(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "";
        }
        String name = originalName.trim();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase();
    }

    private static void fillUploader(SysFile row) {
        try {
            if (StpUtil.isLogin()) {
                Object loginId = StpUtil.getLoginId();
                String s = String.valueOf(loginId);
                row.setUploaderUserName(s);
                try {
                    row.setUploaderUserId(Long.parseLong(s));
                } catch (NumberFormatException ignore) {
                    row.setUploaderUserId(0L);
                }
                return;
            }
        } catch (Exception ignored) {
            // 非 Web 线程或未登录
        }
        row.setUploaderUserId(0L);
        row.setUploaderUserName("");
    }
}

