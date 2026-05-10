package io.github.genkidoudou.web.common.exception;

import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.ErrorException;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.firewall.idempotent.IdempotentException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldMapWarningToBadRequestByDefault() {
        WarningException ex = new WarningException(ErrorCodes.Common.INVALID_PARAM, "参数错误");
        ResponseEntity<R<Void>> entity = handler.handleWarningException(ex);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getCode()).isEqualTo(ErrorCodes.Common.INVALID_PARAM);
        assertThat(entity.getBody().getMsg()).isEqualTo("参数不合法");
    }

    @Test
    void shouldMapSecurityWarningsTo401403429() {
        ResponseEntity<R<Void>> unauthorized = handler.handleWarningException(new WarningException(ErrorCodes.Security.UNAUTHORIZED, "x"));
        ResponseEntity<R<Void>> forbidden = handler.handleWarningException(new WarningException(ErrorCodes.Security.FORBIDDEN, "x"));
        ResponseEntity<R<Void>> tooMany = handler.handleWarningException(new WarningException(ErrorCodes.Biz.IDEMPOTENT_REPEAT, "x"));

        assertThat(unauthorized.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(tooMany.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void shouldMapErrorExceptionTo500AndKeepCode() {
        ErrorException ex = new ErrorException(ErrorCodes.System.DEPENDENCY_UNAVAILABLE, "依赖不可用");
        ResponseEntity<R<Void>> entity = handler.handleErrorException(ex);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getCode()).isEqualTo(ErrorCodes.System.DEPENDENCY_UNAVAILABLE);
        assertThat(entity.getBody().getMsg()).isEqualTo("依赖服务暂不可用");
    }

    @Test
    void shouldMapUnhandledThrowableTo500AndMaskMessage() {
        ResponseEntity<R<Void>> entity = handler.handleThrowable(new RuntimeException("SQL syntax error"));
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getCode()).isEqualTo(ErrorCodes.System.INTERNAL_ERROR);
        assertThat(entity.getBody().getMsg()).isEqualTo("系统繁忙，请稍后再试");
    }

    @Test
    void shouldMapNotLoginAndIdempotentTo401And429() {
        ResponseEntity<R<Void>> notLogin = handler.handleNotLogin(null);
        ResponseEntity<R<Void>> idempotent = handler.handleIdempotent(new IdempotentException("重复请求"));

        assertThat(notLogin.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(idempotent.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(idempotent.getBody()).isNotNull();
        assertThat(idempotent.getBody().getCode()).isEqualTo(ErrorCodes.Biz.IDEMPOTENT_REPEAT);
    }

    @Test
    void shouldFallbackToDefaultMessageWhenWarningMsgIsBlank() {
        WarningException ex = new WarningException(ErrorCodes.Common.REQUEST_BODY_INVALID, " ");
        ResponseEntity<R<Void>> entity = handler.handleWarningException(ex);
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getMsg()).isEqualTo("请求体格式错误");
    }
}
