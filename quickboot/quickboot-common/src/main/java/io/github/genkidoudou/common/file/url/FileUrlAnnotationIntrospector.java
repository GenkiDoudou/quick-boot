package io.github.genkidoudou.common.file.url;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import io.github.genkidoudou.common.file.QcFileProperties;

/**
 * 识别 {@link FileUrl}，注册专用序列化/反序列化并强制保留 null。
 */
public class FileUrlAnnotationIntrospector extends JacksonAnnotationIntrospector {

    private final QcFileProperties props;

    public FileUrlAnnotationIntrospector(@Nullable QcFileProperties props) {
        this.props = props;
    }

    @Override
    public Object findSerializer(Annotated am) {
        FileUrl ann = am.getAnnotation(FileUrl.class);
        if (ann != null) {
            String d = ann.domain();
            return new FileUrlSerializer(d, props);
        }
        return super.findSerializer(am);
    }

    @Override
    public Object findDeserializer(Annotated am) {
        FileUrl ann = am.getAnnotation(FileUrl.class);
        if (ann != null) {
            return new FileUrlDeserializer(ann.domain(), props);
        }
        return super.findDeserializer(am);
    }

    @Override
    public JsonInclude.Value findPropertyInclusion(Annotated a) {
        if (a.getAnnotation(FileUrl.class) != null) {
            return JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS);
        }
        return super.findPropertyInclusion(a);
    }
}
