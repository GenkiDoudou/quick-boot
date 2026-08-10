package io.github.genkidoudou.common.excel.template;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 根据 Jakarta Validation（及 Hibernate {@link Length}）组装 Excel 输入提示文案。
 *
 * <p>拼接顺序：必填 → 格式/长度 → 其它。
 */
public final class ValidationPromptBuilder {

  private ValidationPromptBuilder() {
  }

  /**
   * @param field 字段
   * @return 提示文案；无约束时返回 {@code null}
   */
  public static String build(Field field) {
    if (field == null) {
      return null;
    }
    List<String> required = new ArrayList<>();
    List<String> format = new ArrayList<>();
    List<String> other = new ArrayList<>();

    NotBlank notBlank = field.getAnnotation(NotBlank.class);
    if (notBlank != null) {
      required.add(msgOr(notBlank.message(), "该列不能为空", "{jakarta.validation.constraints.NotBlank.message}"));
    }
    NotNull notNull = field.getAnnotation(NotNull.class);
    if (notNull != null) {
      required.add(msgOr(notNull.message(), "该列不能为空", "{jakarta.validation.constraints.NotNull.message}"));
    }

    Pattern pattern = field.getAnnotation(Pattern.class);
    if (pattern != null) {
      String base = msgOr(pattern.message(), "请按约定格式填写", "{jakarta.validation.constraints.Pattern.message}");
      String regexp = pattern.regexp();
      if (StringUtils.isNotBlank(regexp) && regexp.length() <= 64) {
        format.add(base + "（期望匹配：" + regexp + "）");
      } else {
        format.add(base);
      }
    }

    Size size = field.getAnnotation(Size.class);
    if (size != null) {
      format.add(msgOr(size.message(),
        "长度须在 " + size.min() + "～" + size.max(),
        "{jakarta.validation.constraints.Size.message}"));
    }
    Length length = field.getAnnotation(Length.class);
    if (length != null) {
      format.add(msgOr(length.message(),
        "长度须在 " + length.min() + "～" + length.max(),
        "{org.hibernate.validator.constraints.Length.message}"));
    }

    Email email = field.getAnnotation(Email.class);
    if (email != null) {
      other.add(msgOr(email.message(), "请填写邮箱", "{jakarta.validation.constraints.Email.message}"));
    }

    Class<?> type = field.getType();
    if (LocalDate.class.isAssignableFrom(type)
      || LocalDateTime.class.isAssignableFrom(type)
      || Date.class.isAssignableFrom(type)) {
      other.add("请填写日期，建议格式 yyyy-MM-dd");
    }

    List<String> parts = new ArrayList<>();
    parts.addAll(required);
    parts.addAll(format);
    parts.addAll(other);
    if (parts.isEmpty()) {
      return null;
    }
    return String.join("；", parts);
  }

  /**
   * @param field 字段
   * @return Size/Length 的 max；无则 {@code null}
   */
  public static Integer resolveMaxLength(Field field) {
    if (field == null) {
      return null;
    }
    Size size = field.getAnnotation(Size.class);
    if (size != null && size.max() < Integer.MAX_VALUE) {
      return size.max();
    }
    Length length = field.getAnnotation(Length.class);
    if (length != null && length.max() < Integer.MAX_VALUE) {
      return length.max();
    }
    return null;
  }

  private static String msgOr(String message, String fallback, String placeholder) {
    if (StringUtils.isBlank(message) || message.equals(placeholder) || message.startsWith("{")) {
      return fallback;
    }
    return message;
  }
}
