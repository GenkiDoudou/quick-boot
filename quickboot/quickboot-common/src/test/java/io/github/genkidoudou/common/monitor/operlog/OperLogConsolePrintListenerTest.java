package io.github.genkidoudou.common.monitor.operlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link OperLogConsolePrintListener} 基本行为。
 */
class OperLogConsolePrintListenerTest {

    @Test
    void onOperLogCaptured_nullEvent_noThrow() {
        OperLogConsolePrintListener listener = new OperLogConsolePrintListener(new ObjectMapper());
        listener.onOperLogCaptured(null);
    }

    @Test
    void onOperLogCaptured_minimalPayload_noThrow() {
        OperLogConsolePrintListener listener = new OperLogConsolePrintListener(new ObjectMapper());
        Signature signature = mock(Signature.class);
        OperLogCapturePayload payload = OperLogCapturePayload.builder()
            .startTimeMs(100L)
            .endTimeMs(150L)
            .traceId("t-1")
            .signature(signature)
            .build();
        listener.onOperLogCaptured(new OperLogCapturedEvent(payload));
        assertTrue(true);
    }
}
