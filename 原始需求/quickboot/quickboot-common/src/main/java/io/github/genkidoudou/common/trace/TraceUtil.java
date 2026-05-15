package io.github.genkidoudou.common.trace;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

@UtilityClass
public class TraceUtil {


    public String getTraceId() {
        return MDC.get("traceId");
    }
}
