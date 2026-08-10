package io.github.genkidoudou.system.internal.support;

import io.github.genkidoudou.common.monitor.operlog.OperLogCapturedEvent;
import io.github.genkidoudou.system.internal.entity.SysOperLog;
import io.github.genkidoudou.system.internal.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 操作日志落库逻辑（供同步/异步监听器复用）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperLogPersistSupport {

  private final SysOperLogMapper operLogMapper;
  private final OperLogAssembler operLogAssembler;

  /**
   * 将事件载荷写入 {@code sys_oper_log}；失败仅记日志。
   *
   * @param event 采集事件
   */
  public void persist(OperLogCapturedEvent event) {
    try {
      SysOperLog row = operLogAssembler.assemble(event.getPayload());
      operLogMapper.insert(row);
    } catch (Exception ex) {
      log.error("persist oper log failed", ex);
    }
  }
}
