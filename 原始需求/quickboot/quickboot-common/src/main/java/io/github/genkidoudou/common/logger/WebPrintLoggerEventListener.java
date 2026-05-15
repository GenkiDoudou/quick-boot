package io.github.genkidoudou.common.logger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Web 日志打印监听器
 * 监听日志事件，打印日志到控制台
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.logger", name = "print", havingValue = "true", matchIfMissing = true)
public class WebPrintLoggerEventListener extends AbstractLoggerParserHandler {

    /**
     * 监听日志事件
     *
     * @param loggerEvent 日志事件
     * @since 2026/03/05
     */
    @EventListener
    public void onLoggerEvent(LoggerEvent loggerEvent) {
        if (loggerEvent == null || !(loggerEvent.getSource() instanceof LoggerEventDto)) {
            return;
        }

        LoggerEventDto loggerEventDto = (LoggerEventDto) loggerEvent.getSource();
        LoggerInfo loggerInfo = parseToLoggerInfo(loggerEventDto);

        if (loggerInfo == null) {
            return;
        }

        // 构建日志输出
        StringBuilder sb = new StringBuilder();
        sb.append("\n[-----------------------------------").append("\n")
                .append("methodName: ").append(loggerInfo.getMethodName()).append("\n")
                .append("sourceIp: ").append(loggerInfo.getSourceIp()).append("\n")
                .append("description: ").append(loggerInfo.getDescription()).append("\n")
                .append("uri: ").append(loggerInfo.getMethod()).append(" ").append(loggerInfo.getUri()).append("\n");

        if (loggerInfo.getRequestParams() != null) {
            sb.append("requestParams: ").append(loggerInfo.getRequestParams()).append("\n");
        }

        if (loggerInfo.getTimeConsuming() != null) {
            sb.append("timeConsuming: ").append(loggerInfo.getTimeConsuming()).append("ms\n");
        }

        if (loggerInfo.getTraceId() != null) {
            sb.append("traceId: ").append(loggerInfo.getTraceId()).append("\n");
        }

        if (loggerInfo.getResult() != null) {
            // 限制返回结果长度，避免日志过长
            String result = loggerInfo.getResult();
            if (result.length() > 500) {
                result = result.substring(0, 500) + "...";
            }
            sb.append("result: ").append(result).append("\n");
        }

        if (loggerInfo.getErrorMsg() != null) {
            sb.append("errorMsg: ").append(loggerInfo.getErrorMsg()).append("\n");
        }

        sb.append("------------------------------]");

        // 根据是否有错误选择日志级别
        if (loggerInfo.getErrorMsg() != null) {
            log.error(sb.toString());
        } else {
            log.info(sb.toString());
        }
    }
}
