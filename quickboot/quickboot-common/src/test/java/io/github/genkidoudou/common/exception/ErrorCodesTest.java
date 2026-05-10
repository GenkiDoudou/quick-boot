package io.github.genkidoudou.common.exception;

import io.github.genkidoudou.common.api.HttpCodes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodesTest {

    @Test
    void shouldFollowSegmentRulesAndReuseExistingCodesWithoutConflict() {
        assertThat(ErrorCodes.Common.INVALID_PARAM).isBetween(10000, 19999);
        assertThat(ErrorCodes.Biz.STATE_NOT_ALLOWED).isBetween(20000, 29999);
        assertThat(ErrorCodes.Security.RATE_LIMITED).isBetween(30000, 39999);
        assertThat(ErrorCodes.System.INTERNAL_ERROR).isBetween(40000, 49999);
        assertThat(ErrorCodes.Biz.IDEMPOTENT_REPEAT).isEqualTo(HttpCodes.IDEMPOTENT_REPEAT);
        assertThat(ErrorCodes.Security.HOST_NOT_ALLOWED).isEqualTo(HttpCodes.HOST_NOT_ALLOWED);
    }
}
