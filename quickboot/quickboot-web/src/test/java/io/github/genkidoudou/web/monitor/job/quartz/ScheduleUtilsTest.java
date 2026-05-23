package io.github.genkidoudou.web.monitor.job.quartz;

import org.junit.jupiter.api.Test;
import org.quartz.Job;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleUtilsTest {

    @Test
    void getQuartzJobClass_allowConcurrent() {
        Class<? extends Job> jobClass = ScheduleUtils.getQuartzJobClass("0");
        assertThat(jobClass).isEqualTo(QuartzJobExecution.class);
    }

    @Test
    void getQuartzJobClass_disallowConcurrent() {
        Class<? extends Job> jobClass = ScheduleUtils.getQuartzJobClass("1");
        assertThat(jobClass).isEqualTo(QuartzDisallowConcurrentExecution.class);
    }
}
