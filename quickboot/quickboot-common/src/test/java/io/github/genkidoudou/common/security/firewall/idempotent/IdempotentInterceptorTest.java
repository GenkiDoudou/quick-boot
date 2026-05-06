package io.github.genkidoudou.common.security.firewall.idempotent;

import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotentInterceptorTest {

    private IdempotentProperties props;
    private CaffeineIdempotentStore store;

    @BeforeEach
    void setUp() {
        props = new IdempotentProperties();
        props.setEnabled(true);
        props.setKeyPrefix("p:");
        props.setTokenHeader("X-Idempotent-Token");
        props.setExpireTime(60);
        props.setExpireTimeUnit(TimeUnit.SECONDS);
        store = new CaffeineIdempotentStore();
    }

    @Test
    void blankTokenSkipsIdempotent() throws Exception {
        IdempotentInterceptor cut = new IdempotentInterceptor(props, store);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        HandlerMethod hm = handlerMethod(SampleController.class, "annotated");
        assertThat(cut.preHandle(req, new MockHttpServletResponse(), hm)).isTrue();
        assertThat(req.getAttribute(IdempotentInterceptor.REQ_ATTR_SLOT_HELD)).isNull();
    }

    @Test
    void annotatedWithTokenPlacesSlot() throws Exception {
        IdempotentInterceptor cut = new IdempotentInterceptor(props, store);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        req.addHeader("X-Idempotent-Token", "abc");
        HandlerMethod hm = handlerMethod(SampleController.class, "annotated");
        assertThat(cut.preHandle(req, new MockHttpServletResponse(), hm)).isTrue();
        assertThat(req.getAttribute(IdempotentInterceptor.REQ_ATTR_SLOT_HELD)).isEqualTo(Boolean.TRUE);
    }

    @Test
    void duplicateThrowsIdempotentException() throws Exception {
        IdempotentInterceptor cut = new IdempotentInterceptor(props, store);
        HandlerMethod hm = handlerMethod(SampleController.class, "annotated");
        MockHttpServletRequest req1 = new MockHttpServletRequest("POST", "/api/x");
        req1.addHeader("X-Idempotent-Token", "dup");
        assertThat(cut.preHandle(req1, new MockHttpServletResponse(), hm)).isTrue();

        MockHttpServletRequest req2 = new MockHttpServletRequest("POST", "/api/x");
        req2.addHeader("X-Idempotent-Token", "dup");
        assertThatThrownBy(() -> cut.preHandle(req2, new MockHttpServletResponse(), hm))
                .isInstanceOf(IdempotentException.class);
    }

    @Test
    void excludeUrlSkips() throws Exception {
        props.setExcludeUrls(List.of("/pub/**"));
        IdempotentInterceptor cut = new IdempotentInterceptor(props, store);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/pub/hook");
        req.addHeader("X-Idempotent-Token", "t");
        HandlerMethod hm = handlerMethod(SampleController.class, "annotated");
        assertThat(cut.preHandle(req, new MockHttpServletResponse(), hm)).isTrue();
        assertThat(req.getAttribute(IdempotentInterceptor.REQ_ATTR_SLOT_HELD)).isNull();
    }

    @Test
    void interceptMethodsWithoutAnnotation() throws Exception {
        props.setInterceptMethods(List.of("POST"));
        IdempotentInterceptor cut = new IdempotentInterceptor(props, store);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/raw");
        req.addHeader("X-Idempotent-Token", "m1");
        HandlerMethod hm = handlerMethod(SampleController.class, "raw");
        assertThat(cut.preHandle(req, new MockHttpServletResponse(), hm)).isTrue();
        assertThat(req.getAttribute(IdempotentInterceptor.REQ_ATTR_SLOT_HELD)).isEqualTo(Boolean.TRUE);
    }

    @Test
    void afterCompletionDeletesOnBusinessException() throws Exception {
        IdempotentInterceptor cut = new IdempotentInterceptor(props, store);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        req.addHeader("X-Idempotent-Token", "ex");
        HandlerMethod hm = handlerMethod(SampleController.class, "annotated");
        assertThat(cut.preHandle(req, new MockHttpServletResponse(), hm)).isTrue();
        String key = (String) req.getAttribute(IdempotentInterceptor.REQ_ATTR_SLOT_KEY);
        cut.afterCompletion(req, new MockHttpServletResponse(), hm, new RuntimeException("boom"));
        assertThat(store.setIfAbsent(key, java.time.Duration.ofSeconds(60))).isTrue();
    }

    @Test
    void afterCompletionDeletesWhenDeleteAfterExecution() throws Exception {
        IdempotentInterceptor cut = new IdempotentInterceptor(props, store);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        req.addHeader("X-Idempotent-Token", "da");
        HandlerMethod hm = handlerMethod(SampleController.class, "shortWindow");
        assertThat(cut.preHandle(req, new MockHttpServletResponse(), hm)).isTrue();
        String key = (String) req.getAttribute(IdempotentInterceptor.REQ_ATTR_SLOT_KEY);
        cut.afterCompletion(req, new MockHttpServletResponse(), hm, null);
        assertThat(store.setIfAbsent(key, java.time.Duration.ofSeconds(60))).isTrue();
    }

    private static HandlerMethod handlerMethod(Class<?> c, String methodName) throws Exception {
        return new HandlerMethod(c.getDeclaredConstructor().newInstance(),
                c.getMethod(methodName));
    }

    @SuppressWarnings("unused")
    @RestController
    static class SampleController {

        @PostMapping("/annotated")
        @Idempotent
        public void annotated() {
        }

        @PostMapping("/short")
        @Idempotent(deleteAfterExecution = true)
        public void shortWindow() {
        }

        @PostMapping("/raw")
        public void raw() {
        }
    }
}
