package io.github.genkidoudou.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson 序列化与 JVM 默认时区配置。
 * <p>
 * 将 {@code spring.jackson.date-format} / {@code spring.mvc.format.date} 应用到 JSR-310 类型；
 * 同时将 {@code Long}/{@code long} 统一序列化为字符串，规避前端 JavaScript Number 精度丢失。
 * <p>
 * {@code spring.jackson.time-zone} 除作用于 Jackson 外，还会在容器启动早期同步为 JVM 默认时区，
 * 使 {@link LocalDateTime#now()}、MyBatis 自动填充与 JDBC {@code TIMESTAMP} 读写与 API 展示一致。
 */
@Configuration
public class JacksonTimeConfig {

  private static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
  private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

  /**
   * 将 {@code spring.jackson.time-zone} 同步为 JVM 默认时区。
   * <p>
   * {@link LocalDateTime} 无时区信息，业务写入依赖 {@link LocalDateTime#now()} 与 JDBC 会话时区；
   * 生产机若未设置 {@code TZ} / {@code user.timezone}，仅靠 Jackson 的 defaultTimeZone 无法修正入库时间。
   *
   * @param jacksonProperties 读取 {@code spring.jackson.time-zone}
   * @return 占位 Bean，仅用于触发构造时副作用
   */
  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public Object jvmDefaultTimeZoneSync(JacksonProperties jacksonProperties) {
    TimeZone zone = jacksonProperties.getTimeZone();
    if (zone != null) {
      TimeZone.setDefault(zone);
    }
    return new Object();
  }

  /**
   * 注册带 pattern 的 JSR-310 序列化器，并关闭时间戳输出；Long 输出为字符串。
   *
   * @param jacksonProperties 读取 {@code spring.jackson.date-format}、{@code time-zone}
   * @param datePattern       读取 {@code spring.mvc.format.date}，用于 {@link LocalDate}
   * @return Jackson 自定义器
   */
  @Bean
  public JsonMapperBuilderCustomizer javaTimeModuleCustomizer(
    JacksonProperties jacksonProperties,
    @Value("${spring.mvc.format.date:" + DEFAULT_DATE_PATTERN + "}") String datePattern) {
    String dateTimePattern = jacksonProperties.getDateFormat() != null
      ? jacksonProperties.getDateFormat()
      : DEFAULT_DATE_TIME_PATTERN;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern);

    SimpleModule timeModule = new SimpleModule("quickboot-java-time")
      .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
      .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter))
      .addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter))
      .addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

    SimpleModule longModule = new SimpleModule("quickboot-long-as-string")
      .addSerializer(Long.class, ToStringSerializer.instance)
      .addSerializer(Long.TYPE, ToStringSerializer.instance);

    return builder -> {
      builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
      builder.addModule(timeModule);
      builder.addModule(longModule);
      if (jacksonProperties.getTimeZone() != null) {
        builder.defaultTimeZone(jacksonProperties.getTimeZone());
      }
    };
  }
}
