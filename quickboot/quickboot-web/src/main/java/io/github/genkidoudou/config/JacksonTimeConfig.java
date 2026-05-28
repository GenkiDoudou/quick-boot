package io.github.genkidoudou.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

/**
 * Jackson 序列化配置。
 * <p>
 * 将 {@code spring.jackson.date-format} / {@code spring.mvc.format.date} 应用到 JSR-310 类型，
 * 避免 {@link LocalDateTime} 默认输出 ISO-8601（如 {@code 2026-05-16T13:31:39.337124}）。
 * 同时将 {@code Long}/{@code long} 统一序列化为字符串，规避前端 JavaScript Number 精度丢失。
 */
@Configuration
public class JacksonTimeConfig {

    private static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 注册带 pattern 的 {@link JavaTimeModule}，并关闭时间戳输出。
     *
     * @param jacksonProperties 读取 {@code spring.jackson.date-format}、{@code time-zone}
     * @param datePattern       读取 {@code spring.mvc.format.date}，用于 {@link LocalDate}
     * @return Jackson 自定义器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer javaTimeModuleCustomizer(
            JacksonProperties jacksonProperties,
            @Value("${spring.mvc.format.date:" + DEFAULT_DATE_PATTERN + "}") String datePattern) {
        String dateTimePattern = jacksonProperties.getDateFormat() != null
                ? jacksonProperties.getDateFormat()
                : DEFAULT_DATE_TIME_PATTERN;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern);

        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(dateTimeFormatter);
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(dateTimeFormatter);
        LocalDateSerializer localDateSerializer = new LocalDateSerializer(dateFormatter);
        LocalDateDeserializer localDateDeserializer = new LocalDateDeserializer(dateFormatter);

        return builder -> {
            builder.featuresToDisable(WRITE_DATES_AS_TIMESTAMPS);
            // 通过 serializerByType 覆盖 Boot 默认 JavaTimeModule 的 ISO 输出
            builder.serializerByType(LocalDateTime.class, localDateTimeSerializer);
            builder.deserializerByType(LocalDateTime.class, localDateTimeDeserializer);
            builder.serializerByType(LocalDate.class, localDateSerializer);
            builder.deserializerByType(LocalDate.class, localDateDeserializer);
            if (jacksonProperties.getTimeZone() != null) {
                builder.timeZone(jacksonProperties.getTimeZone());
            }
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
