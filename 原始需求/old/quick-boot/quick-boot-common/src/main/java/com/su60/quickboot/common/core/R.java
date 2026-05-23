package com.su60.quickboot.common.core;

import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import com.su60.quickboot.common.exception.GlobalMsgCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 统一返回
 *
 * @param <T> 返回泛型
 * @author luyanan
 * @since 2023/09/09
 **/
@Data
public class R<T> {

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

	/**
	 * 成功的返回
	 *
	 * @param <T>  返回泛型
	 * @param data 数据
	 * @return R对象
	 * @since 2023/09/09
	 */
	public static <T> R<T> ok(T data) {
		return restResult(SUCCESS_STATUS, null, data);
	}

	/**
	 * 成功的返回
	 *
	 * @param <T> 返回泛型
	 * @return R对象
	 * @since 2023/09/09
	 */
	public static <T> R<T> ok() {
		return ok(null);
	}


	/**
	 * 失败的返回
	 *
	 * @param status 状态码
	 * @param msg    提示消息
	 * @return R
	 * @since 2023/09/09
	 */
	public static R failed(Integer status, String msg) {

		return restResult(status, msg, null);
	}

	/**
	 * 失败的返回
	 *
	 * @param msg 提示消息
	 * @return R对象
	 * @since 2023/09/09
	 */
	public static R failed(String msg) {
		return failed(ERROR_STATUS, msg);
	}

	/**
	 * 失败的返回
	 *
	 * @return R对象
	 * @since 2023/09/09
	 */
	public static R failed() {
		return failed(null);
	}


	public static void failed(HttpServletResponse response, Integer status, String message) {
		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		R<?> result = R.failed(status, message);
		try {
			response.getWriter().write(JSONUtil.toJsonStr(result));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 设置R对象
	 *
	 * @param <T>    返回泛型
	 * @param status 状态码
	 * @param msg    提示消息
	 * @param data   数据
	 * @return R对象
	 * @since 2023/09/09
	 */
	private static <T> R<T> restResult(Integer status, String msg, T data) {
		R<T> r = new R<>();
		r.setCode(status);
		r.setMsg(msg);
		r.setData(data);
		r.setTimestamp(System.currentTimeMillis());
		r.setTraceId(MDC.get("traceId"));
		return r;
	}


}
