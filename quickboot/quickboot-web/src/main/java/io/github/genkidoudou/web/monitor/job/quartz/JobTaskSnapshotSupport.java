package io.github.genkidoudou.web.monitor.job.quartz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.monitor.job.domain.SysJob;
import io.github.genkidoudou.web.monitor.job.mapper.SysJobMapper;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

import java.util.Map;

/**
 * JobDataMap 与 {@link JobTaskSnapshot} 互转；兼容 JDBC 持久化后类型变为 Map / JSON 字符串的情况。
 */
public final class JobTaskSnapshotSupport {

    /** JobDataMap 中存放 JSON 快照，避免 JDBC BLOB 反序列化类型丢失。 */
    public static final String TASK_PROPERTIES_JSON = "TASK_PROPERTIES_JSON";

    private JobTaskSnapshotSupport() {
    }

    /**
     * 写入 JobDataMap（对象 + JSON 双写，供 Quartz 持久化后读取）。
     */
    public static void putTo(JobDataMap map, JobTaskSnapshot snapshot) {
        map.put(JobTaskSnapshot.TASK_PROPERTIES, snapshot);
        map.put(TASK_PROPERTIES_JSON, JSONUtil.toJsonStr(snapshot));
    }

    /**
     * 从 Quartz 执行上下文解析快照；必要时按 JobKey 回源 {@code sys_job}。
     */
    public static JobTaskSnapshot resolve(JobExecutionContext context) {
        JobDataMap merged = context.getMergedJobDataMap();
        JobTaskSnapshot snapshot = coerce(merged.get(JobTaskSnapshot.TASK_PROPERTIES));
        if (snapshot == null) {
            String json = merged.getString(TASK_PROPERTIES_JSON);
            if (StrUtil.isNotBlank(json)) {
                snapshot = JSONUtil.toBean(json, JobTaskSnapshot.class);
            }
        }
        if (snapshot != null) {
            return snapshot;
        }
        Long jobId = parseJobId(context.getJobDetail().getKey());
        if (jobId == null) {
            return null;
        }
        SysJobMapper mapper = JobExecutionBridge.getJobMapper();
        if (mapper == null) {
            return null;
        }
        SysJob job = mapper.selectById(jobId);
        return job == null ? null : JobTaskSnapshot.from(job);
    }

    static Long parseJobId(JobKey jobKey) {
        if (jobKey == null || jobKey.getName() == null) {
            return null;
        }
        String prefix = ScheduleUtils.TASK_CLASS_NAME;
        String name = jobKey.getName();
        if (!name.startsWith(prefix)) {
            return null;
        }
        try {
            return Long.parseLong(name.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static JobTaskSnapshot coerce(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof JobTaskSnapshot snapshot) {
            return snapshot;
        }
        if (raw instanceof Map<?, ?> map) {
            return BeanUtil.toBean(map, JobTaskSnapshot.class);
        }
        if (raw instanceof String str && StrUtil.isNotBlank(str) && JSONUtil.isTypeJSON(str)) {
            return JSONUtil.toBean(str, JobTaskSnapshot.class);
        }
        return null;
    }
}
