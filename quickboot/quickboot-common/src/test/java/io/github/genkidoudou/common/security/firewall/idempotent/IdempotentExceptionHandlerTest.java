package io.github.genkidoudou.common.security.firewall.idempotent;

import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.api.R;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotentExceptionHandlerTest {

    @Test
    void mapsToHttp200AndRCode() {
        IdempotentExceptionHandler h = new IdempotentExceptionHandler();
        ResponseEntity<R<Void>> entity = h.handleIdempotent(new IdempotentException("msg"));
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getCode()).isEqualTo(HttpCodes.IDEMPOTENT_REPEAT);
        assertThat(entity.getBody().getMsg()).isEqualTo("msg");
    }
}
