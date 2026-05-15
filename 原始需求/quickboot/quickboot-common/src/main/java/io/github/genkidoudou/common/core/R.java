package io.github.genkidoudou.common.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.trace.TraceUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.Serializable;

/**
 * 通用返回结果
 *
 * @param <T> 数据类型
 * @author genkidoudou
 * @since 2023/09/09
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功的返回状态码
     *
     * @since 2023/09/09
     */
    private static final Integer SUCCESS_STATUS = GlobalMsgCode.SUCCESS;

    /**
     * 默认失败的返回状态码
     *
     * @since 2023/09/09
     */
    private static final Integer ERROR_STATUS = GlobalMsgCode.INTERNAL_SERVER_ERROR;

    /**
     * 状态码
     *
     * @since 2023/09/09
     */
    private Integer code;

    /**
     * 提示消息
     *
     * @since 2023/09/09
     */
    private String msg;

    /**
     * 返回数据
     *
     * @since 2023/09/09
     */
    private T data;

    /**
     * 链路id
     *
     * @since 2026/1/18
     */
    private String traceId;

    /**
     * 时间戳
     *
     * @since 2026/1/18
     */
    private Long timestamp;

    public R() {
        this.timestamp = System.currentTimeMillis();
        this.traceId = TraceUtil.getTraceId();
    }

    public R(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        this.traceId = TraceUtil.getTraceId();
    }

    /**
     * 成功返回
     *
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> R<T> ok() {
        return new R<>(SUCCESS_STATUS, "success", null);
    }

    /**
     * 成功返回
     *
     * @param msg 消息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> R<T> ok(String msg) {
        return new R<>(SUCCESS_STATUS, msg, null);
    }

    /**
     * 成功返回
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 返回结果
     */
    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS_STATUS, "success", data);
    }

    /**
     * 成功返回
     *
     * @param msg  消息
     * @param data 数据
     * @param <T>  数据类型
     * @return 返回结果
     */
    public static <T> R<T> ok(String msg, T data) {
        return new R<>(SUCCESS_STATUS, msg, data);
    }

    /**
     * 失败返回
     *
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> R<T> error() {
        return new R<>(ERROR_STATUS, "error", null);
    }

    /**
     * 失败返回
     *
     * @param msg 消息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> R<T> error(String msg) {
        return new R<>(ERROR_STATUS, msg, null);
    }

    /**
     * 失败返回
     *
     * @param code 状态码
     * @param msg  消息
     * @param <T>  数据类型
     * @return 返回结果
     */
    public static <T> R<T> error(Integer code, String msg) {
        return new R<>(code, msg, null);
    }

    /**
     * 失败返回
     *
     * @param code 状态码
     * @param msg  消息
     * @param data 数据
     * @param <T>  数据类型
     * @return 返回结果
     */
    public static <T> R<T> error(Integer code, String msg, T data) {
        return new R<>(code, msg, data);
    }

    /**
     * 根据状态返回
     *
     * @param flag 状态
     * @param <T>  数据类型
     * @return 返回结果
     */
    public static <T> R<T> status(boolean flag) {
        return flag ? ok() : error();
    }

    /**
     * 根据状态返回
     *
     * @param flag 状态
     * @param msg  消息
     * @param <T>  数据类型
     * @return 返回结果
     */
    public static <T> R<T> status(boolean flag, String msg) {
        return flag ? ok(msg) : error(msg);
    }

    /**
     * 根据状态返回
     *
     * @param flag 状态
     * @param data 数据
     * @param <T>  数据类型
     * @return 返回结果
     */
    public static <T> R<T> status(boolean flag, T data) {
        return flag ? ok(data) : error();
    }

    /**
     * 判断是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return SUCCESS_STATUS.equals(this.code);
    }

    /**
     * 判断是否失败
     *
     * @return 是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }



}
