package io.github.genkidoudou.common.exception;

import io.github.genkidoudou.common.api.HttpCodes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseExceptionTest {

    @Test
    void shouldFallbackToInternalCodeWhenCodeIsNull() {
        BaseException ex = new BaseException(null, "msg");
        assertThat(ex.getCode()).isEqualTo(HttpCodes.INTERNAL_ERROR);
        assertThat(ex.getMsg()).isEqualTo("msg");
    }

    @Test
    void shouldPreserveArgsAndCause() {
        IllegalStateException cause = new IllegalStateException("bad");
        Object[] args = new Object[]{"a", 1};
        BaseException ex = new BaseException(20001, "biz", args, cause);
        assertThat(ex.getCode()).isEqualTo(20001);
        assertThat(ex.getArgs()).containsExactly("a", 1);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void warningAndErrorShouldInheritBaseException() {
        WarningException warning = new WarningException(20001, "warn");
        ErrorException error = new ErrorException(40001, "err");
        assertThat(warning).isInstanceOf(BaseException.class);
        assertThat(error).isInstanceOf(BaseException.class);
    }
}
