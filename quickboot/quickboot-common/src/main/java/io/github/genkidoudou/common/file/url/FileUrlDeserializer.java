package io.github.genkidoudou.common.file.url;

import java.io.IOException;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import io.github.genkidoudou.common.file.QcFileProperties;

/**
 * {@link FileUrl} 字段反序列化：剥掉配置/注解 domain 前缀，存相对路径。
 */
public class FileUrlDeserializer extends JsonDeserializer<String> {

    private final String explicitDomain;
    private final QcFileProperties props;

    public FileUrlDeserializer(String explicitDomain, QcFileProperties props) {
        this.explicitDomain = explicitDomain;
        this.props = props;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.getCurrentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        String raw = p.getValueAsString();
        if (raw == null) {
            return null;
        }
        String domain = resolveDomain();
        if (!StringUtils.hasText(domain)) {
            return raw.trim();
        }
        return FileUrlSupport.stripDomainIfPresent(raw.trim(), domain);
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
}
