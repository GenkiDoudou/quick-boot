package io.github.genkidoudou.common.security.firewall.idempotent;

import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.i18n.I18nUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 将 {@link IdempotentException} 转为 HTTP 200 + {@link R} 业务码 {@link io.github.genkidoudou.common.api.HttpCodes#IDEMPOTENT_REPEAT}。
 */
@RestControllerAdvice
public class IdempotentExceptionHandler {

    @ExceptionHandler(IdempotentException.class)
    public ResponseEntity<R<Void>> handleIdempotent(IdempotentException ex) {
        int code = ex.getCode();
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = I18nUtil.getMessage(String.valueOf(code), null, null);
        }
        return ResponseEntity.status(200).body(R.error(code, msg));
    }
}
