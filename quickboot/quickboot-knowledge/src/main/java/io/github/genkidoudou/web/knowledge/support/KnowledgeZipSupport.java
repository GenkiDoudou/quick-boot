package io.github.genkidoudou.web.knowledge.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 知识库 ZIP 解压：仅提取允许扩展名的文件，并防御 Zip Slip。
 * <p>Windows 资源管理器等工具生成的 ZIP 文件名多为 GB18030，须对 UTF-8 解码失败时回退。</p>
 */
public final class KnowledgeZipSupport {

    /** 与 {@code qc.file.classifies[knowledge].limit-ext} 文档类型对齐（不含 zip）。 */
    public static final Set<String> ALLOWED_DOC_EXTENSIONS = Set.of("pdf", "doc", "docx", "md", "txt");

    /** 单包最多处理的文件条目数，防止压缩炸弹。 */
    public static final int MAX_ENTRIES = 50;

    /** 单条目解压后最大字节数（50MB）。 */
    public static final long MAX_ENTRY_BYTES = 50L * 1024 * 1024;

    /** Windows 中文 ZIP 常用文件名编码（GBK 超集）。 */
    private static final Charset GB18030 = Charset.forName("GB18030");

    private KnowledgeZipSupport() {
    }

    /**
     * 解压结果：成功提取的文档与跳过的条目说明。
     *
     * @param entries 可入库文件
     * @param skipped 跳过原因（展示名）
     */
    public record ZipExtractResult(List<ExtractedDoc> entries, List<String> skipped) {
    }

    /**
     * @param entryName ZIP 内文件名（不含路径）
     * @param content   文件字节
     */
    public record ExtractedDoc(String entryName, byte[] content) {
    }

    /**
     * 从 ZIP 中提取允许扩展名的文档。
     *
     * @param zipFile 上传的 zip 文件
     * @return 提取结果
     */
    public static ZipExtractResult extractDocuments(MultipartFile zipFile) {
        if (zipFile == null || zipFile.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "ZIP 文件不能为空");
        }
        String original = StrUtil.blankToDefault(zipFile.getOriginalFilename(), "").toLowerCase(Locale.ROOT);
        if (!original.endsWith(".zip")) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "请上传 .zip 格式的压缩包");
        }

        byte[] zipBytes;
        try {
            zipBytes = zipFile.getBytes();
        } catch (IOException ex) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "读取 ZIP 文件失败：" + ex.getMessage());
        }

        try {
            return extractDocuments(zipBytes, StandardCharsets.UTF_8);
        } catch (ZipNameEncodingException ex) {
            try {
                return extractDocuments(zipBytes, GB18030);
            } catch (IOException retryEx) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                    "ZIP 解压失败：" + retryEx.getMessage());
            }
        } catch (IOException ex) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "ZIP 解压失败：" + ex.getMessage());
        }
    }

    /**
     * 按指定字符集解压 ZIP 条目名与内容。
     */
    private static ZipExtractResult extractDocuments(byte[] zipBytes, Charset charset) throws IOException {
        List<ExtractedDoc> entries = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        int processedEntries = 0;

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes), charset)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                processedEntries++;
                if (processedEntries > MAX_ENTRIES) {
                    throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                        "ZIP 内文件数量超过上限（最多 " + MAX_ENTRIES + " 个）");
                }

                String safeName = safeEntryFileName(entry.getName());
                if (StrUtil.isBlank(safeName)) {
                    skipped.add(entry.getName() + "（非法路径）");
                    continue;
                }
                if (safeName.startsWith(".") || safeName.startsWith("__MACOSX")) {
                    skipped.add(safeName + "（系统文件已跳过）");
                    continue;
                }

                String ext = fileExtLower(safeName);
                if (!ALLOWED_DOC_EXTENSIONS.contains(ext)) {
                    skipped.add(safeName + "（不支持 ." + ext + "）");
                    continue;
                }

                byte[] bytes = readEntryBytes(zis, safeName);
                if (bytes.length == 0) {
                    skipped.add(safeName + "（空文件）");
                    continue;
                }
                entries.add(new ExtractedDoc(safeName, bytes));
            }
        } catch (IllegalArgumentException ex) {
            if (isZipNameEncodingError(ex)) {
                throw new ZipNameEncodingException(ex);
            }
            throw ex;
        }

        if (entries.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "ZIP 中未找到可入库的文档（支持 pdf/doc/docx/md/txt）");
        }
        return new ZipExtractResult(entries, skipped);
    }

    private static byte[] readEntryBytes(ZipInputStream zis, String safeName) throws IOException {
        byte[] buffer = new byte[8192];
        int total = 0;
        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            int read;
            while ((read = zis.read(buffer)) >= 0) {
                total += read;
                if (total > MAX_ENTRY_BYTES) {
                    throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                        "ZIP 内文件「" + safeName + "」超过大小上限（50MB）");
                }
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    private static boolean isZipNameEncodingError(Throwable ex) {
        Throwable cur = ex;
        while (cur != null) {
            if (cur instanceof MalformedInputException) {
                return true;
            }
            String msg = cur.getMessage();
            if (msg != null && msg.contains("malformed input")) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private static final class ZipNameEncodingException extends IOException {
        ZipNameEncodingException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * 仅保留文件名并校验 Zip Slip。
     */
    static String safeEntryFileName(String rawName) {
        if (StrUtil.isBlank(rawName)) {
            return "";
        }
        String normalized = rawName.replace('\\', '/');
        if (normalized.contains("..")) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "ZIP 包含非法路径: " + rawName);
        }
        int slash = normalized.lastIndexOf('/');
        String name = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        return name.trim();
    }

    private static String fileExtLower(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
