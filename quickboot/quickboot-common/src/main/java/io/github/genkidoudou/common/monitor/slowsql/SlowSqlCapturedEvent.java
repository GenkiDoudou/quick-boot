package io.github.genkidoudou.common.monitor.slowsql;

import org.springframework.context.ApplicationEvent;

/**
 * 慢 SQL 已采集、待持久化的事件。
 */
public class SlowSqlCapturedEvent extends ApplicationEvent {

    /**
     * @param payload 慢 SQL 采集载荷
     */
    public SlowSqlCapturedEvent(SlowSqlCapturePayload payload) {
        super(payload);
    }

    /**
     * @return 慢 SQL 采集载荷
     */
    public SlowSqlCapturePayload getPayload() {
        return (SlowSqlCapturePayload) getSource();
    }
}
