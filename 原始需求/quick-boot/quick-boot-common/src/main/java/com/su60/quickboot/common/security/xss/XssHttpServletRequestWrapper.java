package com.su60.quickboot.common.security.xss;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * XSS 清洗请求包装器。
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] escaped = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            escaped[i] = HtmlUtil.cleanHtmlTag(values[i]).trim();
        }
        return escaped;
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value == null) {
            return null;
        }
        return HtmlUtil.cleanHtmlTag(value).trim();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!isJsonRequest()) {
            return super.getInputStream();
        }
        String json = IoUtil.read(super.getInputStream(), StandardCharsets.UTF_8);
        if (StrUtil.isBlank(json)) {
            return super.getInputStream();
        }
        json = cleanJson(json);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return bais.available() == 0;
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

    private String cleanJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object obj = mapper.readValue(json, Object.class);
            Object cleaned = cleanObject(obj);
            return mapper.writeValueAsString(cleaned);
        } catch (Exception e) {
            return HtmlUtil.cleanHtmlTag(json).trim();
        }
    }

    private Object cleanObject(Object obj) {
        if (obj instanceof String s) {
            return HtmlUtil.cleanHtmlTag(s).trim();
        } else if (obj instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((k, v) -> result.put(String.valueOf(k), cleanObject(v)));
            return result;
        } else if (obj instanceof List<?> list) {
            List<Object> result = new ArrayList<>();
            list.forEach(item -> result.add(cleanObject(item)));
            return result;
        }
        return obj;
    }

    private boolean isJsonRequest() {
        String header = super.getHeader(HttpHeaders.CONTENT_TYPE);
        return header != null && header.contains(MediaType.APPLICATION_JSON_VALUE);
    }
}
