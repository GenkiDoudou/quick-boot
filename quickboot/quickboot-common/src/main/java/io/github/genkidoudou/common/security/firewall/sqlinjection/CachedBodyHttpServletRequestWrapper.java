package io.github.genkidoudou.common.security.firewall.sqlinjection;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.Part;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * 将已读取的请求体字节缓存并向下游暴露为可重复读的 {@link ServletInputStream}，
 * 供后续 Filter（如敏感词）在不丢失原始流的前提下再次读取 body。
 * <p>
 * 对 {@code multipart/form-data} 须同时实现 {@link #getParts()}：否则 Tomcat 仍从已耗尽的原始流解析，
 * Spring {@code @RequestPart} 会报 {@code MissingServletRequestPartException}。
 */
public final class CachedBodyHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cachedBody;
    private List<Part> parsedParts;

    /**
     * @param request    原始请求（用于委托 {@code getParameter} 等未缓存语义）
     * @param cachedBody 已读取的 body；{@code null} 视为空数组
     */
    public CachedBodyHttpServletRequestWrapper(HttpServletRequest request, byte[] cachedBody) {
        super(request);
        this.cachedBody = cachedBody != null ? cachedBody : new byte[0];
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream in = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override
            public int read() {
                return in.read();
            }

            @Override
            public boolean isFinished() {
                return in.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        Charset charset = resolveCharset();
        return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }

    @Override
    public int getContentLength() {
        return cachedBody.length;
    }

    @Override
    public long getContentLengthLong() {
        return cachedBody.length;
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        if (isMultipartContentType()) {
            return parsedMultipartParts();
        }
        return super.getParts();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        if (isMultipartContentType()) {
            for (Part part : parsedMultipartParts()) {
                if (name.equals(part.getName())) {
                    return part;
                }
            }
            return null;
        }
        return super.getPart(name);
    }

    private List<Part> parsedMultipartParts() {
        if (parsedParts == null) {
            parsedParts = MultipartFormDataPartsParser.parse(cachedBody, getContentType());
        }
        return parsedParts;
    }

    private boolean isMultipartContentType() {
        String ct = getContentType();
        if (ct == null || ct.isEmpty()) {
            return false;
        }
        try {
            return MediaType.parseMediaType(ct).isCompatibleWith(MediaType.MULTIPART_FORM_DATA);
        } catch (Exception ignored) {
            return ct.toLowerCase().startsWith("multipart/");
        }
    }

    private Charset resolveCharset() {
        String enc = getCharacterEncoding();
        if (enc == null || enc.isEmpty()) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(enc);
        } catch (Exception ignored) {
            return StandardCharsets.UTF_8;
        }
    }
}
