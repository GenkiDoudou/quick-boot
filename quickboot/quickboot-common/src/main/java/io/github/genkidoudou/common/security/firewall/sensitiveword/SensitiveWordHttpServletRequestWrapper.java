package io.github.genkidoudou.common.security.firewall.sensitiveword;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 包装请求：可选重写 body；对 query/form 参数按策略做敏感词替换或 THROW。
 */
class SensitiveWordHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cachedBody;
    private final SensitiveWordEngine engine;
    private final SensitiveWordFirewallStrategy strategy;

    SensitiveWordHttpServletRequestWrapper(HttpServletRequest request,
                                          byte[] cachedBody,
                                          SensitiveWordEngine engine,
                                          SensitiveWordFirewallStrategy strategy) {
        super(request);
        this.cachedBody = cachedBody;
        this.engine = engine;
        this.strategy = strategy;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cachedBody == null) {
            return super.getInputStream();
        }
        ByteArrayInputStream in = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
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
        if (cachedBody == null) {
            return super.getReader();
        }
        Charset charset = resolveCharset();
        return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }

    @Override
    public int getContentLength() {
        if (cachedBody != null) {
            return cachedBody.length;
        }
        return super.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        if (cachedBody != null) {
            return cachedBody.length;
        }
        return super.getContentLengthLong();
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

    private String processValue(String v) {
        if (v == null || v.isEmpty()) {
            return v;
        }
        if (strategy == SensitiveWordFirewallStrategy.REPLACE) {
            return engine.replace(v);
        }
        engine.assertNotContains(v);
        return v;
    }

    @Override
    public String getParameter(String name) {
        String v = super.getParameter(name);
        return v == null ? null : processValue(v);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] v = super.getParameterValues(name);
        if (v == null) {
            return null;
        }
        String[] c = new String[v.length];
        for (int i = 0; i < v.length; i++) {
            c[i] = processValue(v[i]);
        }
        return c;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> orig = super.getParameterMap();
        Map<String, String[]> out = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> e : orig.entrySet()) {
            String[] vals = e.getValue();
            if (vals == null) {
                out.put(e.getKey(), null);
            } else {
                String[] c = new String[vals.length];
                for (int i = 0; i < vals.length; i++) {
                    c[i] = processValue(vals[i]);
                }
                out.put(e.getKey(), c);
            }
        }
        return Collections.unmodifiableMap(out);
    }
}
