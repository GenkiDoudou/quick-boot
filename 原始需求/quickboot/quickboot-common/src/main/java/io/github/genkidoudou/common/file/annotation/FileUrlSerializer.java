package io.github.genkidoudou.common.file.annotation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 文件 URL 序列化器
 * 若值为空或已是完整 URL，直接返回；否则拼接 domain + "/" + value
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
public class FileUrlSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private String domain;

    public FileUrlSerializer() {
    }

    public FileUrlSerializer(String domain) {
        this.domain = domain;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isBlank()) {
            gen.writeString(value);
            return;
        }
        if (isFullUrl(value)) {
            gen.writeString(value);
            return;
        }
        String domainToUse = domain;
        if (domainToUse == null || domainToUse.isBlank()) {
            domainToUse = FileUrlContextHolder.getDomain();
        }
        if (domainToUse == null || domainToUse.isBlank()) {
            gen.writeString(value);
            return;
        }
        domainToUse = domainToUse.endsWith("/") ? domainToUse.substring(0, domainToUse.length() - 1) : domainToUse;
        String path = value.startsWith("/") ? value : "/" + value;
        gen.writeString(domainToUse + path);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            FileUrl fileUrl = property.getAnnotation(FileUrl.class);
            if (fileUrl == null) {
                fileUrl = property.getContextAnnotation(FileUrl.class);
            }
            if (fileUrl != null && StringUtils.hasText(fileUrl.domain())) {
                return new FileUrlSerializer(fileUrl.domain());
            }
        }
        return new FileUrlSerializer();
    }

    private boolean isFullUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }
}
