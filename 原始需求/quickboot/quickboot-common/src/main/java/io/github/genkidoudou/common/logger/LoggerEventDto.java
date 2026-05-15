package io.github.genkidoudou.common.logger;

import lombok.Data;
import lombok.experimental.Accessors;
import org.aspectj.lang.Signature;

import java.io.Serializable;

/**
 * 日志事件传输对象
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Data
@Accessors(chain = true)
public class LoggerEventDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 返回结果
     *
     * @since 2026/03/05
     */
    private Object result;

    /**
     * 请求开始时间
     *
     * @since 2026/03/05
     */
    private Long startTime;

    /**
     * 请求结束时间
     *
     * @since 2026/03/05
     */
    private Long endTime;

    /**
     * 异常
     *
     * @since 2026/03/05
     */
    private Throwable throwable;

    /**
     * 方法签名
     *
     * @since 2026/03/05
     */
    private Signature signature;

    /**
     * 方法参数
     *
     * @since 2026/03/05
     */
    private Object[] args;

    /**
     * 链路id
     *
     * @since 2026/03/05
     */
    private String traceId;

    /**
     * 日志类型
     *
     * @since 2026/03/05
     */
    private String type;
}
