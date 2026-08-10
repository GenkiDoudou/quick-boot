package io.github.genkidoudou.common.monitor.slowsql;

import org.springframework.context.ApplicationEvent;

/**
 * 慢 SQL 已采集、待持久化的事件。
 */
public class SlowSqlCapturedEvent extends ApplicationEvent {

    public SlowSqlCapturedEvent(SlowSqlCapturePayload payload) {
        super(payload);
    }

    public SlowSqlCapturePayload getPayload() {
        return (SlowSqlCapturePayload) getSource();
    }
}
