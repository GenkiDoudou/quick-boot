package io.github.genkidoudou.common.monitor.operlog;

import org.springframework.context.ApplicationEvent;

/**
 * 操作日志已采集、待持久化的事件（载荷为 {@link OperLogCapturePayload}）。
 */
public class OperLogCapturedEvent extends ApplicationEvent {

  /**
   * @param payload 操作日志采集载荷
   */
  public OperLogCapturedEvent(OperLogCapturePayload payload) {
    super(payload);
  }

  /**
   * @return 采集载荷
   */
  public OperLogCapturePayload getPayload() {
    return (OperLogCapturePayload) getSource();
  }
}
