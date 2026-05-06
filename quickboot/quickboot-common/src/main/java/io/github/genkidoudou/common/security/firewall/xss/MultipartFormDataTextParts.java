package io.github.genkidoudou.common.security.firewall.xss;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从 {@code multipart/form-data} 的<strong>原始 body 字节</strong>中提取文本字段：
 * {@link jakarta.servlet.http.Part} 带 {@code filename=} 的部件<strong>整段跳过</strong>（不对文件二进制做 XSS 扫描）。
 * <p>
 * 解析失败时返回空列表（调用方可继续依赖 query 与其它分支）；大文件部件已在跳过逻辑中排除。
 * </p>
 */
final class MultipartFormDataTextParts {

    private static final byte[] CRLFCRLF = {'\r', '\n', '\r', '\n'};
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "(?i)\\bname\\s*=\\s*(\"([^\"]*)\"|([^\";\\s]+))");
    private static final Pattern FILENAME_PATTERN = Pattern.compile("(?i)\\bfilename\\s*=");
    private static final Pattern CHARSET_PATTERN = Pattern.compile("(?i)charset\\s*=\\s*([^;\\s]+)");

    record NamedText(String name, String text) {
    }

    static List<NamedText> extract(byte[] raw, String contentTypeHeader) {
        List<NamedText> out = new ArrayList<>();
        if (raw == null || raw.length == 0) {
            return out;
        }
        String boundary = extractBoundary(contentTypeHeader);
        if (boundary == null || boundary.isEmpty()) {
            return out;
        }
        byte[] dashBoundary = ("--" + boundary).getBytes(StandardCharsets.US_ASCII);
        int searchFrom = 0;
        while (true) {
            int p = indexOf(raw, dashBoundary, searchFrom);
            if (p < 0) {
                break;
            }
            int afterBoundary = p + dashBoundary.length;
            if (afterBoundary + 1 < raw.length && raw[afterBoundary] == '-' && raw[afterBoundary + 1] == '-') {
                break;
            }
            if (afterBoundary + 2 > raw.length || raw[afterBoundary] != '\r' || raw[afterBoundary + 1] != '\n') {
                searchFrom = p + 1;
                continue;
            }
            int hdrStart = afterBoundary + 2;
            int hdrEnd = indexOf(raw, CRLFCRLF, hdrStart);
            if (hdrEnd < 0) {
                break;
            }
            String headers = new String(raw, hdrStart, hdrEnd - hdrStart, StandardCharsets.UTF_8);
            int bodyStart = hdrEnd + 4;
            int nextBoundary = indexOf(raw, dashBoundary, bodyStart);
            if (nextBoundary < 0) {
                break;
            }
            int bodyEnd = nextBoundary;
            if (bodyEnd >= 2 && raw[bodyEnd - 2] == '\r' && raw[bodyEnd - 1] == '\n') {
                bodyEnd -= 2;
            }
            String disposition = headerValue(headers, "Content-Disposition");
            if (disposition != null && FILENAME_PATTERN.matcher(disposition).find()) {
                searchFrom = nextBoundary;
                continue;
            }
            String name = extractName(disposition);
            if (name == null || name.isEmpty()) {
                searchFrom = nextBoundary;
                continue;
            }
            Charset cs = charsetFromContentTypeHeader(headers);
            String text = new String(raw, bodyStart, Math.max(0, bodyEnd - bodyStart), cs);
            out.add(new NamedText(name, text));
            searchFrom = nextBoundary;
        }
        return out;
    }

    private static String extractBoundary(String contentTypeHeader) {
        if (contentTypeHeader == null) {
            return null;
        }
        for (String part : contentTypeHeader.split(";")) {
            String p = part.trim();
            if (p.regionMatches(true, 0, "boundary=", 0, 9)) {
                String b = p.substring(9).trim();
                if (b.length() >= 2 && b.startsWith("\"") && b.endsWith("\"")) {
                    b = b.substring(1, b.length() - 1);
                }
                return b;
            }
        }
        return null;
    }

    private static String headerValue(String block, String headerName) {
        String prefix = headerName + ":";
        for (String line : block.split("\\r\\n")) {
            if (line.regionMatches(true, 0, prefix, 0, prefix.length())) {
                return line.substring(prefix.length()).trim();
            }
        }
        return null;
    }

    private static String extractName(String disposition) {
        if (disposition == null) {
            return null;
        }
        Matcher m = NAME_PATTERN.matcher(disposition);
        if (!m.find()) {
            return null;
        }
        return m.group(2) != null ? m.group(2) : m.group(3);
    }

    private static Charset charsetFromContentTypeHeader(String headers) {
        String ct = headerValue(headers, "Content-Type");
        if (ct != null) {
            Matcher m = CHARSET_PATTERN.matcher(ct);
            if (m.find()) {
                try {
                    return Charset.forName(m.group(1).trim().replace("\"", ""));
                } catch (Exception ignored) {
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

    private static int indexOf(byte[] data, byte[] pattern, int from) {
        outer:
        for (int i = Math.max(0, from); i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
