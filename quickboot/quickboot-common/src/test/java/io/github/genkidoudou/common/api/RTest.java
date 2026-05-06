package io.github.genkidoudou.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void okWithMsgAndData_serializes() throws Exception {
        Map<String, Object> dto = Map.of("id", 1);
        R<Map<String, Object>> r = R.ok("操作成功", dto);
        assertThat(r.isSuccess()).isTrue();
        assertThat(r.isError()).isFalse();
        assertThat(r.getCode()).isEqualTo(HttpCodes.OK);
        assertThat(r.getTimestamp()).isGreaterThan(0);

        ObjectMapper om = new ObjectMapper();
        Map<?, ?> json = om.readValue(om.writeValueAsString(r), Map.class);
        assertThat(json.get("code")).isEqualTo(200);
        assertThat(json.get("msg")).isEqualTo("操作成功");
        assertThat(json.get("data")).isNotNull();
    }

    @Test
    void errorWithCode() {
        R<Void> r = R.error(400, "参数无效");
        assertThat(r.isSuccess()).isFalse();
        assertThat(r.isError()).isTrue();
        assertThat(r.getCode()).isEqualTo(400);
    }

    @Test
    void traceId_fromMdc_whenPresent() {
        MDC.put(TraceIds.MDC_KEY, "trace-abc");
        R<Void> r = R.ok();
        assertThat(r.getTraceId()).isEqualTo("trace-abc");
    }

    @Test
    void traceId_null_whenMdcAbsent() {
        R<Void> r = R.ok();
        assertThat(r.getTraceId()).isNull();
    }
}
