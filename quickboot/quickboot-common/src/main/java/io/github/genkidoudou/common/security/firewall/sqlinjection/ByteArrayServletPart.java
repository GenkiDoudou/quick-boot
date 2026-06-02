package io.github.genkidoudou.common.security.firewall.sqlinjection;

import jakarta.servlet.http.Part;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于内存字节的 {@link Part}，供 {@link CachedBodyHttpServletRequestWrapper} 在 body 被防火墙预读后仍能解析 multipart。
 */
final class ByteArrayServletPart implements Part {

    private final String name;
    private final String submittedFileName;
    private final String contentType;
    private final byte[] content;
    private final Map<String, List<String>> headerMap;

    ByteArrayServletPart(String name, String submittedFileName, String contentType, byte[] content,
                         Map<String, List<String>> headerMap) {
        this.name = name;
        this.submittedFileName = submittedFileName;
        this.contentType = contentType;
        this.content = content != null ? content : new byte[0];
        this.headerMap = headerMap != null ? headerMap : Map.of();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSubmittedFileName() {
        return submittedFileName;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public void write(String fileName) throws IOException {
        throw new UnsupportedOperationException("read-only cached part");
    }

    @Override
    public void delete() throws IOException {
        // no-op: in-memory only
    }

    @Override
    public String getHeader(String name) {
        List<String> values = headerMap.get(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        List<String> values = headerMap.get(name);
        return values == null ? List.of() : List.copyOf(values);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return Collections.unmodifiableSet(headerMap.keySet());
    }

    static Map<String, List<String>> headersFromBlock(String headerBlock) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        if (headerBlock == null || headerBlock.isEmpty()) {
            return map;
        }
        for (String line : headerBlock.split("\\r\\n")) {
            int colon = line.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            map.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(value);
        }
        return map;
    }
}
