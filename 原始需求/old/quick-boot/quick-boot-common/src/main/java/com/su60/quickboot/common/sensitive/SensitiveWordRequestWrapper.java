package com.su60.quickboot.common.sensitive;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 对请求参数和 body 进行敏感词处理的包装器。
 */
public class SensitiveWordRequestWrapper extends HttpServletRequestWrapper {

    private final SensitiveWordService sensitiveWordService;
    private final SensitiveWordStrategy strategy;
    private byte[] cachedBody;
    private Map<String, String[]> cachedParams;

    public SensitiveWordRequestWrapper(HttpServletRequest request,
                                       SensitiveWordService sensitiveWordService,
                                       SensitiveWordStrategy strategy) throws IOException {
        super(request);
        this.sensitiveWordService = sensitiveWordService;
        this.strategy = strategy;
        cacheBodyIfJson(request);
    }

    private void cacheBodyIfJson(HttpServletRequest request) throws IOException {
        if (!isJsonRequest()) {
            return;
        }
        String body = IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8);
        // 即使 body 为空，也要缓存，避免原始 InputStream 被读取后无法再次读取
        if (StrUtil.isBlank(body)) {
            this.cachedBody = new byte[0];
            return;
        }
        // 先缓存原始 body，确保即使处理过程中出现异常，也能提供请求体
        byte[] originalBody = body.getBytes(StandardCharsets.UTF_8);
        try {
            // 处理 body（检测或替换敏感词）
            processBody(body);
        } catch (SensitiveWordException e) {
            // 敏感词异常直接抛出，由 Filter 处理
            throw e;
        } catch (Exception e) {
            // 其他异常（如 JSON 解析异常），使用原始 body
            this.cachedBody = originalBody;
            // 不抛出异常，让请求继续处理
        }
    }

    private void processBody(String body) {
        // 检测敏感词
        if (strategy == SensitiveWordStrategy.THROW  ) {
			String text = sensitiveWordService.getText(body);
			if (StrUtil.isNotBlank(text)){
				throw new SensitiveWordException("请求体包含敏感词：" + text);
			}
        }
        // 替换敏感词或保持原样
        String processed = strategy == SensitiveWordStrategy.REPLACE
                ? sensitiveWordService.replace(body)
                : body;
        // 缓存处理后的 body
        this.cachedBody = processed.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 如果是 JSON 请求，必须使用缓存的 body（即使为空），因为原始 InputStream 已被读取
        if (isJsonRequest() && cachedBody != null) {
            final ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
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
        // 非 JSON 请求或未缓存，使用原始 InputStream
        return super.getInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (cachedParams == null) {
            cachedParams = new HashMap<>(super.getParameterMap());
            cachedParams.replaceAll((k, v) -> processValues(v));
        }
        return cachedParams;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] original = super.getParameterValues(name);
        if (original == null) {
            return null;
        }
        return processValues(original);
    }

    private String[] processValues(String[] values) {
        String[] processed = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            String val = values[i];
            if (StrUtil.isBlank(val)) {
                processed[i] = val;
                continue;
            }
            if (strategy == SensitiveWordStrategy.THROW ) {
				String text = sensitiveWordService.getText(val);
				if (StrUtil.isNotBlank(text)){
					throw new SensitiveWordException("请求体包含敏感词：" + text);
				}
            }
            processed[i] = strategy == SensitiveWordStrategy.REPLACE
                    ? sensitiveWordService.replace(val)
                    : val;
        }
        return processed;
    }

    private boolean isJsonRequest() {
        String header = super.getHeader(HttpHeaders.CONTENT_TYPE);
        return header != null && header.contains(MediaType.APPLICATION_JSON_VALUE);
    }
}
