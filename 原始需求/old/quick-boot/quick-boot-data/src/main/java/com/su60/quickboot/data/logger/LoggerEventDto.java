package com.su60.quickboot.data.logger;

import lombok.Data;
import lombok.experimental.Accessors;
import org.aspectj.lang.Signature;

/**
 * 日志事件传输对象
 *
 * @author luyanan
 * @since 2023/10/08
 **/

@Data
@Accessors(chain = true)
public class LoggerEventDto {
	private static final long serialVersionUID = 1L;


	/**
	 * 返回结果
	 *
	 * @author luyanan
	 * @since 2023/10/08
	 */
	private Object result;


	/**
	 * 请求开始时间
	 *
	 * @author luyanan
	 * @since 2023/10/08
	 */
	private Long startTime;


	/**
	 * 请求结束时间
	 *
	 * @author luyanan
	 * @since 2023/10/08
	 */
	private Long endTime;


	/**
	 * 异常
	 *
	 * @author luyanan
	 * @since 2023/10/08
	 */
	private Throwable throwable;


	/**
	 * 方法签名
	 *
	 * @author luyanan
	 * @since 2023/10/08
	 */
	private Signature signature;


	/**
	 * 方法参数
	 *
	 * @author luyanan
	 * @since 2023/10/08
	 */
	private Object[] args;


	/**
	 * 链路id
	 *
	 * @author luyanan
	 * @since 2023/10/08
	 */
	private String traceId;

	/**
	 * 日志类型
	 *
	 * @since 2022/11/22
	 */

	private String type;
}
