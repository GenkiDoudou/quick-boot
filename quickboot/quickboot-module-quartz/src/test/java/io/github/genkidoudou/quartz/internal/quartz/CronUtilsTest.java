package io.github.genkidoudou.quartz.internal.quartz;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CronUtilsTest {

    @Test
    void fixSecondWildcardWithRestrictedMinute() {
        assertEquals("0 0/1 * * * ?", CronUtils.fixSecondWildcardWithRestrictedMinute("* 0/1 * * * ?"));
    }

    @Test
    void everyMinuteFiresOncePerMinuteNotEverySecond() {
        List<Date> next = CronUtils.getNextExecutions("0 0/1 * * * ?", 3);
        assertEquals(3, next.size());
        long gap0 = next.get(1).getTime() - next.get(0).getTime();
        long gap1 = next.get(2).getTime() - next.get(1).getTime();
        assertTrue(gap0 >= 59_000, "间隔应约 1 分钟");
        assertTrue(gap1 >= 59_000);
    }

    @Test
    void wildcardSecondFiresEverySecond() {
        List<Date> next = CronUtils.getNextExecutions("* 0/1 * * * ?", 3);
        assertEquals(3, next.size());
        long gap = next.get(1).getTime() - next.get(0).getTime();
        assertTrue(gap < 2_000, "秒=* 时应约每秒");
    }

    @Test
    void describeMisconfiguration() {
        assertNotNull(CronUtils.describe("* 0/1 * * * ?"));
        assertTrue(CronUtils.describe("* 0/1 * * * ?").contains("每秒"));
    }

    @Test
    void invalidCronThrowsWarningException() {
        WarningException ex = assertThrows(WarningException.class, () -> CronUtils.getNextExecution("not a cron"));
        assertEquals(ErrorCodes.Job.CRON_INVALID, ex.getCode());
    }
}
