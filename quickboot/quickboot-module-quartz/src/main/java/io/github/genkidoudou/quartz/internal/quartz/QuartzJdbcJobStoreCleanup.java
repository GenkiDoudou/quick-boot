package io.github.genkidoudou.quartz.internal.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 直接清理 Quartz JDBC 表中的半删元数据；在 {@code Scheduler.deleteJob} 因孤儿触发器失败时使用。
 */
@Slf4j
final class QuartzJdbcJobStoreCleanup {

    /**
     * 查找本项目托管触发器（{@code TASK_CLASS_NAME_%}）在任意 {@code sched_name} 下缺少 CRON 扩展行的记录。
     */
    private static final String SQL_ORPHAN_MANAGED_TRIGGERS = """
        SELECT t.sched_name, t.trigger_name, t.trigger_group
        FROM QRTZ_TRIGGERS t
        WHERE t.trigger_name LIKE ?
          AND NOT EXISTS (
            SELECT 1 FROM QRTZ_CRON_TRIGGERS c
            WHERE c.sched_name = t.sched_name
              AND c.trigger_name = t.trigger_name
              AND c.trigger_group = t.trigger_group
          )
        """;

    private QuartzJdbcJobStoreCleanup() {
    }

    /**
     * 清理 CRON 触发器主表存在但扩展表缺失的记录，并删除无触发器挂靠的 Job。
     *
     * @return 清理的触发器条数
     */
    static int cleanBrokenCronMetadata(JdbcTemplate jdbc, String schedName) {
        List<Map<String, Object>> orphans = jdbc.queryForList(
            SQL_ORPHAN_MANAGED_TRIGGERS, ScheduleUtils.TASK_CLASS_NAME + "%");
        Set<String> deleted = new HashSet<>();
        int count = 0;
        for (Map<String, Object> row : orphans) {
            String rowSchedName = column(row, "sched_name");
            String triggerName = column(row, "trigger_name");
            String triggerGroup = column(row, "trigger_group");
            if (rowSchedName == null) {
                rowSchedName = schedName;
            }
            String key = rowSchedName + "|" + triggerGroup + "." + triggerName;
            if (!deleted.add(key)) {
                continue;
            }
            deleteTriggerRows(jdbc, rowSchedName, triggerName, triggerGroup);
            count++;
            log.warn("已 JDBC 清理孤儿 CRON 触发器 {}.{} (sched={})",
                triggerGroup, triggerName, rowSchedName);
        }

        int orphanJobs = jdbc.update("""
            DELETE FROM QRTZ_JOB_DETAILS
            WHERE sched_name = ?
              AND job_name LIKE ?
              AND NOT EXISTS (
                SELECT 1 FROM QRTZ_TRIGGERS t
                WHERE t.sched_name = QRTZ_JOB_DETAILS.sched_name
                  AND t.job_name = QRTZ_JOB_DETAILS.job_name
                  AND t.job_group = QRTZ_JOB_DETAILS.job_group
              )
            """, schedName, ScheduleUtils.TASK_CLASS_NAME + "%");

        if (count > 0 || orphanJobs > 0) {
            log.warn("JDBC 清理 Quartz 脏数据：orphanTriggers={} orphanJobs={}", count, orphanJobs);
        }
        return count;
    }

    /**
     * 绕过 Scheduler API，按 JobKey / TriggerKey 删除 JDBC 中该任务的全部触发器与 Job 行。
     */
    static void forceDeleteJob(JdbcTemplate jdbc, String schedName, JobKey jobKey) {
        String jobName = jobKey.getName();
        String jobGroup = jobKey.getGroup();

        deleteTriggerRows(jdbc, schedName, jobName, jobGroup);

        List<Map<String, Object>> linkedTriggers = jdbc.queryForList("""
            SELECT sched_name, trigger_name, trigger_group
            FROM QRTZ_TRIGGERS
            WHERE job_name = ? AND job_group = ?
            """, jobName, jobGroup);
        for (Map<String, Object> row : linkedTriggers) {
            String rowSched = column(row, "sched_name");
            if (rowSched == null) {
                rowSched = schedName;
            }
            deleteTriggerRows(jdbc, rowSched, column(row, "trigger_name"), column(row, "trigger_group"));
        }

        jdbc.update("""
            DELETE FROM QRTZ_JOB_DETAILS
            WHERE job_name = ? AND job_group = ?
            """, jobName, jobGroup);
    }

    /**
     * 优先 {@link Scheduler#deleteJob(JobKey)}；失败或仍存在脏行时回退 JDBC 删除。
     */
    static void deleteJobSafely(Scheduler scheduler, JdbcTemplate jdbc, JobKey jobKey) throws SchedulerException {
        String schedName = ScheduleUtils.SCHEDULER_NAME;
        if (jdbc != null) {
            cleanBrokenCronMetadata(jdbc, schedName);
        }
        if (!scheduler.checkExists(jobKey)) {
            if (jdbc != null) {
                forceDeleteJob(jdbc, schedName, jobKey);
            }
            return;
        }
        try {
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException ex) {
            if (jdbc == null) {
                throw ex;
            }
            log.warn("Scheduler.deleteJob 失败，改用 JDBC 强制删除 {}.{}: {}",
                jobKey.getGroup(), jobKey.getName(), ex.getMessage());
            forceDeleteJob(jdbc, schedName, jobKey);
        }
        if (jdbc != null && scheduler.checkExists(jobKey)) {
            forceDeleteJob(jdbc, schedName, jobKey);
        }
    }

    private static void deleteTriggerRows(JdbcTemplate jdbc, String schedName, String triggerName, String triggerGroup) {
        if (triggerName == null || triggerGroup == null) {
            return;
        }
        jdbc.update("""
            DELETE FROM QRTZ_FIRED_TRIGGERS
            WHERE sched_name = ? AND trigger_name = ? AND trigger_group = ?
            """, schedName, triggerName, triggerGroup);
        jdbc.update("""
            DELETE FROM QRTZ_CRON_TRIGGERS
            WHERE sched_name = ? AND trigger_name = ? AND trigger_group = ?
            """, schedName, triggerName, triggerGroup);
        jdbc.update("""
            DELETE FROM QRTZ_SIMPLE_TRIGGERS
            WHERE sched_name = ? AND trigger_name = ? AND trigger_group = ?
            """, schedName, triggerName, triggerGroup);
        jdbc.update("""
            DELETE FROM QRTZ_SIMPROP_TRIGGERS
            WHERE sched_name = ? AND trigger_name = ? AND trigger_group = ?
            """, schedName, triggerName, triggerGroup);
        jdbc.update("""
            DELETE FROM QRTZ_BLOB_TRIGGERS
            WHERE sched_name = ? AND trigger_name = ? AND trigger_group = ?
            """, schedName, triggerName, triggerGroup);
        jdbc.update("""
            DELETE FROM QRTZ_TRIGGERS
            WHERE sched_name = ? AND trigger_name = ? AND trigger_group = ?
            """, schedName, triggerName, triggerGroup);
    }

    private static String column(Map<String, Object> row, String name) {
        Object value = row.get(name);
        if (value == null) {
            value = row.get(name.toUpperCase());
        }
        return value == null ? null : value.toString();
    }
}
