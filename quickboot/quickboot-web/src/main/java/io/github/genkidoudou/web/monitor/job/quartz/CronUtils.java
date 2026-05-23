package io.github.genkidoudou.web.monitor.job.quartz;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Cron 表达式工具。
 */
public final class CronUtils {

  private CronUtils() {
  }

  /**
   * 校验 Cron 是否合法。
   */
  public static boolean isValid(String cronExpression) {
    return CronExpression.isValidExpression(cronExpression);
  }

  /**
   * 下一次执行时间。
   */
  public static Date getNextExecution(String cronExpression) {
    try {
      CronExpression cron = new CronExpression(cronExpression);
      return cron.getNextValidTimeAfter(new Date());
    } catch (ParseException e) {
      throwCronInvalid(e);
    }
    return null;
  }

  /**
   * 未来若干次执行时间。
   */
  public static List<Date> getNextExecutions(String cronExpression, int count) {
    List<Date> dates = new ArrayList<>();
    Date cursor = new Date();
    try {
      CronExpression cron = new CronExpression(cronExpression);
      for (int i = 0; i < count; i++) {
        Date next = cron.getNextValidTimeAfter(cursor);
        if (next == null) {
          break;
        }
        dates.add(next);
        cursor = next;
      }
    } catch (ParseException e) {
      throwCronInvalid(e);
      return dates;
    }
    return dates;
  }

  private static void throwCronInvalid(ParseException e) {
    String detail = e.getMessage() == null ? "Cron 表达式不正确" : e.getMessage();
    throw new WarningException(ErrorCodes.Job.CRON_INVALID, detail);
  }

  /**
   * 秒字段为 {@code *} 且分钟字段已限定（非 {@code *}）时，实际会<strong>每秒</strong>触发，常被误当成「每分钟」。
   * 将秒字段改为 {@code 0}，保留其余字段，得到「每分钟的第 0 秒执行」。
   *
   * @param cronExpression 原表达式
   * @return 修正后的表达式；无需修正时返回 {@code null}
   */
  public static String fixSecondWildcardWithRestrictedMinute(String cronExpression) {
    if (StrUtil.isBlank(cronExpression)) {
      return null;
    }
    String[] parts = cronExpression.trim().split("\\s+");
    if (parts.length < 6) {
      return null;
    }
    if (!"*".equals(parts[0]) || "*".equals(parts[1])) {
      return null;
    }
    parts[0] = "0";
    return String.join(" ", parts);
  }

  /**
   * 人类可读说明（Quartz 六段：秒 分 时 日 月 周）。
   */
  public static String describe(String cronExpression) {
    if (StrUtil.isBlank(cronExpression) || !isValid(cronExpression)) {
      return "";
    }
    String[] parts = cronExpression.trim().split("\\s+");
    if (parts.length < 6) {
      return "";
    }
    if ("*".equals(parts[0]) && "*".equals(parts[1])) {
      return "约每秒执行一次（秒、分均为 *）";
    }
    if ("*".equals(parts[0]) && !"*".equals(parts[1])) {
      return "约每秒执行一次（秒为 *；若需「每分钟」请将秒改为 0，例如：0 "
        + parts[1] + " " + String.join(" ", Arrays.copyOfRange(parts, 2, parts.length)) + "）";
    }
    if ("0".equals(parts[0]) && parts[1].contains("/")) {
      return "每分钟的第 0 秒执行（分钟字段 " + parts[1] + "）";
    }
    if ("0".equals(parts[0]) && "*".equals(parts[1])) {
      return "每小时的第 0 分 0 秒执行";
    }
    return "按 Cron 规则调度（秒=" + parts[0] + "，分=" + parts[1] + "）";
  }
}
