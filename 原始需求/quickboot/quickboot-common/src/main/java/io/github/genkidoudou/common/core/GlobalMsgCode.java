package io.github.genkidoudou.common.core;

/**
 * 全局消息码
 *
 * @author genkidoudou
 * @since 2023/09/09
 */
public interface GlobalMsgCode {

    /**
     * 成功
     */
    Integer SUCCESS = 200;

    /**
     * 失败
     */
    Integer ERROR = 500;

    /**
     * 内部服务器错误
     */
    Integer INTERNAL_SERVER_ERROR = 500;

    /**
     * 参数错误
     */
    Integer BAD_REQUEST = 400;

    /**
     * 未授权
     */
    Integer UNAUTHORIZED = 401;

    /**
     * 禁止访问
     */
    Integer FORBIDDEN = 403;

    /**
     * 未找到
     */
    Integer NOT_FOUND = 404;

    /**
     * 请求超时
     */
    Integer REQUEST_TIMEOUT = 408;

    /**
     * 服务不可用
     */
    Integer SERVICE_UNAVAILABLE = 503;

    /**
     * 网关超时
     */
    Integer GATEWAY_TIMEOUT = 504;
}
