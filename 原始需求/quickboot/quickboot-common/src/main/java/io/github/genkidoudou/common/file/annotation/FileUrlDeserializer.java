package io.github.genkidoudou.common.file.annotation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 文件 URL 反序列化器
 * 若值包含配置的 domain 前缀，则去掉前缀得到相对路径；否则原样返回
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
public class FileUrlDeserializer extends JsonDeserializer<String> implements ContextualDeserializer {

    private String domain;

    public FileUrlDeserializer() {
    }

    public FileUrlDeserializer(String domain) {
        this.domain = domain;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isBlank()) {
            return value;
        }
        String domainToUse = domain;
        if (domainToUse == null || domainToUse.isBlank()) {
            domainToUse = FileUrlContextHolder.getDomain();
        }
        if (domainToUse == null || domainToUse.isBlank()) {
            return value;
        }
        domainToUse = domainToUse.endsWith("/") ? domainToUse : domainToUse + "/";
        if (value.startsWith(domainToUse)) {
            return value.substring(domainToUse.length());
        }
        // 兼容 domain 不带末尾 / 的情况，如 https://cdn.com
        String domainNoSlash = domainToUse.endsWith("/") ? domainToUse.substring(0, domainToUse.length() - 1) : domainToUse;
        if (value.startsWith(domainNoSlash + "/")) {
            return value.substring(domainNoSlash.length() + 1);
        }
        if (value.equals(domainNoSlash)) {
            return "";
        }
        return value;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            FileUrl fileUrl = property.getAnnotation(FileUrl.class);
            if (fileUrl == null) {
                fileUrl = property.getContextAnnotation(FileUrl.class);
            }
            if (fileUrl != null && StringUtils.hasText(fileUrl.domain())) {
                return new FileUrlDeserializer(fileUrl.domain());
            }
        }
        return new FileUrlDeserializer();
    }
}
