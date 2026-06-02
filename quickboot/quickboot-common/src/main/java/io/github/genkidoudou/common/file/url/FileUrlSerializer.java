package io.github.genkidoudou.common.file.url;

import java.io.IOException;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import io.github.genkidoudou.common.file.QcFileProperties;

/**
 * {@link FileUrl} 字段序列化：{@code null} 写 JSON null；已是 http(s) 则原样；否则拼接 domain。
 */
public class FileUrlSerializer extends JsonSerializer<String> {

    private final String explicitDomain;
    private final QcFileProperties props;

    public FileUrlSerializer(String explicitDomain, QcFileProperties props) {
        this.explicitDomain = explicitDomain;
        this.props = props;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (isAbsoluteUrl(value)) {
            gen.writeString(value);
            return;
        }
        String domain = resolveDomain();
        if (!StringUtils.hasText(domain)) {
            gen.writeString(value);
            return;
        }
        gen.writeString(FileUrlSupport.join(domain, value));
    }

    private String resolveDomain() {
        if (StringUtils.hasText(explicitDomain)) {
            return explicitDomain.trim();
        }
        if (props == null) {
            return "";
        }
        if (StringUtils.hasText(props.getDomain())) {
            return props.getDomain().trim();
        }
        if (StringUtils.hasText(props.getViewUrlBase())) {
            return props.getViewUrlBase().trim();
        }
        return "";
    }

    static boolean isAbsoluteUrl(String s) {
        String t = s.trim().toLowerCase();
        return t.startsWith("http://") || t.startsWith("https://");
    }
}
