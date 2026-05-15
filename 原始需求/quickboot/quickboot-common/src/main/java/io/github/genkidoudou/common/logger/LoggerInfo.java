package io.github.genkidoudou.common.logger;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志详情
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Data
@Accessors(chain = true)
public class LoggerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 返回结果
     *
     * @since 2026/03/05
     */
    private String result;

    /**
     * 开始时间
     *
     * @since 2026/03/05
     */
    private Long startTime;

    /**
     * 结束时间
     *
     * @since 2026/03/05
     */
    private Long endTime;

    /**
     * 耗时（毫秒）
     *
     * @since 2026/03/05
     */
    private Long timeConsuming;

    /**
     * 链路id
     *
     * @since 2026/03/05
     */
    private String traceId;

    /**
     * 请求方法（GET/POST等）
     *
     * @since 2026/03/05
     */
    private String method;

    /**
     * 请求的方法名称
     *
     * @since 2026/03/05
     */
    private String methodName;

    /**
     * 方法描述
     *
     * @since 2026/03/05
     */
    private String description;

    /**
     * 错误信息
     *
     * @since 2026/03/05
     */
    private String errorMsg;

    /**
     * 来源IP
     *
     * @since 2026/03/05
     */
    private String sourceIp;

    /**
     * 请求URI
     *
     * @since 2026/03/05
     */
    private String uri;

    /**
     * 请求参数
     *
     * @since 2026/03/05
     */
    private String requestParams;

    /**
     * 扩展字段
     *
     * @since 2026/03/05
     */
    private Map<String, Object> ext = new HashMap<>();

    /**
     * 错误码
     *
     * @since 2026/03/05
     */
    private Integer code;
}
