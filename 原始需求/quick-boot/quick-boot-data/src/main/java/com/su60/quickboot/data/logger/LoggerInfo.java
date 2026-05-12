package com.su60.quickboot.data.logger;


import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志详情
 *
 * @author luyanan
 * @since 2023/05/21
 **/
@Data
@Accessors(chain = true)
public class LoggerInfo implements Serializable {
	private static final long serialVersionUID = 1L;


	/**
	 * 返回结果
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private String result;

	/**
	 * 开始时间
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private Long startTime;

	/**
	 * 结束时间
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private Long endTime;


	/**
	 * 耗时
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private Long timeConsuming;

	/**
	 * 链路id
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private String traceId;

	/**
	 * 请求方法
	 *
	 * @since 2024/11/15
	 */

	private String method;

	/**
	 * 请求的方法名称
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private String methodName;

	/**
	 * 方法介绍
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private String desp;


	/**
	 * 错误信息
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private String errorMsg;

	/**
	 * 来源ip
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private String sourceIp;

	/**
	 * 请求参数
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private String uri;


	/**
	 * 请求参数
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private String requestParams;


	/**
	 * 扩展
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private Map<String, Object> ext = new HashMap<>();

	/**
	 * 错误code
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	private Integer code;
}
