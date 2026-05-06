package io.github.genkidoudou.common.file.url;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import io.github.genkidoudou.common.file.QcFileProperties;

/**
 * {@link FileUrl} 与全局 non_null 并存时的序列化语义（不启动 Spring）。
 */
class FileUrlJacksonTest {

    static class Dto {
        @FileUrl
        public String path;
    }

    private static ObjectMapper mapper(QcFileProperties props) {
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        AnnotationIntrospector primary = new FileUrlAnnotationIntrospector(props);
        AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
        om.setAnnotationIntrospector(AnnotationIntrospector.pair(primary, secondary));
        return om;
    }

    @Test
    void serialize_null_keeps_field() throws Exception {
        QcFileProperties props = new QcFileProperties();
        props.setDomain("https://cdn.example.com");
        ObjectMapper om = mapper(props);
        Dto d = new Dto();
        d.path = null;
        String json = om.writeValueAsString(d);
        assertThat(json).contains("\"path\":null");
    }

    @Test
    void serialize_prepends_domain() throws Exception {
        QcFileProperties props = new QcFileProperties();
        props.setDomain("https://cdn.example.com");
        ObjectMapper om = mapper(props);
        Dto d = new Dto();
        d.path = "img/2026/01/x.png";
        assertThat(om.writeValueAsString(d)).contains("https://cdn.example.com/img/2026/01/x.png");
    }

    @Test
    void deserialize_strips_domain() throws Exception {
        QcFileProperties props = new QcFileProperties();
        props.setDomain("https://cdn.example.com");
        ObjectMapper om = mapper(props);
        Dto d = om.readValue("{\"path\":\"https://cdn.example.com/a/b.png\"}", Dto.class);
        assertThat(d.path).isEqualTo("a/b.png");
    }
}
