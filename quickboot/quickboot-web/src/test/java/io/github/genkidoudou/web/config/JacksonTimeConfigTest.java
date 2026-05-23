package io.github.genkidoudou.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.web.monitor.operlog.domain.SysOperLog;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 校验 JSR-310 全局日期格式与 {@code application.yml} 中 {@code spring.jackson.date-format} 一致。
 */
class JacksonTimeConfigTest {

    @Test
    void localDateTimeUsesConfiguredPatternNotIso() throws Exception {
        JacksonProperties props = new JacksonProperties();
        props.setDateFormat("yyyy-MM-dd HH:mm:ss");

        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonTimeConfig().javaTimeModuleCustomizer(props, "yyyy-MM-dd").customize(builder);
        ObjectMapper mapper = builder.build();

        SysOperLog row = new SysOperLog();
        row.setOperTime(LocalDateTime.of(2026, 5, 16, 13, 31, 39, 337124000));

        String json = mapper.writeValueAsString(row);

        assertThat(json).contains("\"operTime\":\"2026-05-16 13:31:39\"");
        assertThat(json).doesNotContain("T13:31");
    }

    @Test
    void localDateTimeDeserializesFromConfiguredPattern() throws Exception {
        JacksonProperties props = new JacksonProperties();
        props.setDateFormat("yyyy-MM-dd HH:mm:ss");

        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonTimeConfig().javaTimeModuleCustomizer(props, "yyyy-MM-dd").customize(builder);
        ObjectMapper mapper = builder.build();

        SysOperLog row = mapper.readValue("{\"operTime\":\"2026-05-16 13:31:39\"}", SysOperLog.class);

        assertThat(row.getOperTime()).isEqualTo(LocalDateTime.of(2026, 5, 16, 13, 31, 39));
    }
}
