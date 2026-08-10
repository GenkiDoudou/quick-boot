package io.github.genkidoudou.system.internal.support;

import io.github.genkidoudou.common.monitor.operlog.OperLogCapturedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 异步监听 {@link OperLogCapturedEvent} 并写入 {@code sys_oper_log}。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.monitor.operlog", name = "async-enabled", havingValue = "true", matchIfMissing = true)
public class OperLogAsyncPersistListener {

  private final OperLogPersistSupport persistSupport;

  /**
   * @param event 采集事件
   */
  @Async("operLogTaskExecutor")
  @EventListener
  public void onOperLogCaptured(OperLogCapturedEvent event) {
    persistSupport.persist(event);
  }
}
