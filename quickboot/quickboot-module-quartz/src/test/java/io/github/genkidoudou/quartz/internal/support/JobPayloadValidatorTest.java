package io.github.genkidoudou.quartz.internal.support;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JobPayloadValidator#parseExpectStatus} 单元测试。
 */
class JobPayloadValidatorTest {

    @Test
    void parseExpectStatus_defaultsTo200WhenBlank() {
        Set<Integer> codes = JobPayloadValidator.parseExpectStatus(null);
        assertEquals(Set.of(200), codes);
    }

    @Test
    void parseExpectStatus_parsesMultiple() {
        Set<Integer> codes = JobPayloadValidator.parseExpectStatus("200, 201 ,204");
        assertTrue(codes.containsAll(Set.of(200, 201, 204)));
    }
}
