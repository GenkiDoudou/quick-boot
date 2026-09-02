package io.github.genkidoudou.quartz.internal.support;

import cn.hutool.core.util.StrUtil;

/**
 * 定时任务执行类型，与字典 {@code sys_job_type} 及 {@code sys_job.job_type} 一致。
 */
public enum JobType {

    /** Spring Bean，调用 {@link io.github.genkidoudou.quartz.api.ITask}。 */
    BEAN("0"),
    /** 出站 HTTP 请求。 */
    HTTP("1"),
    /** 本地脚本（ProcessBuilder）。 */
    SCRIPT("2");

    private final String code;

    JobType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 解析任务类型；空值视为 Bean（兼容历史数据）。
     */
    public static JobType fromCode(String code) {
        if (StrUtil.isBlank(code) || BEAN.code.equals(code)) {
            return BEAN;
        }
        for (JobType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
