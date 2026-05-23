package com.su60.quickboot.system.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 操作日志记录
 * </p>
 *
 * @author luyanan
 * @since 2024/11/15
 */
@Data
@Accessors(chain = true)
@TableName("sys_oper_log")
public class SysOperLogEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 日志主键
	 *
	 * @since 2024/11/15
	 */

	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;


	/**
	 * 方法名称
	 *
	 * @since 2024/11/15
	 */
	@TableField("method")
	private String method;


	/**
	 * 请求方式
	 *
	 * @since 2024/11/15
	 */
	@TableField("request_method")
	private String requestMethod;


	/**
	 * 操作人员
	 *
	 * @since 2024/11/15
	 */
	@TableField("oper_name")
	private String operName;


	/**
	 * 请求URL
	 *
	 * @since 2024/11/15
	 */
	@TableField("oper_url")
	private String operUrl;


	/**
	 * 主机地址
	 *
	 * @since 2024/11/15
	 */
	@TableField("oper_ip")
	private String operIp;


	/**
	 * 操作地点
	 *
	 * @since 2024/11/15
	 */
	@TableField("oper_location")
	private String operLocation;


	/**
	 * 请求参数
	 *
	 * @since 2024/11/15
	 */
	@TableField("oper_param")
	private String operParam;


	/**
	 * 返回参数
	 *
	 * @since 2024/11/15
	 */
	@TableField("json_result")
	private String jsonResult;


	/**
	 * 操作状态（0正常 1异常）
	 *
	 * @since 2024/11/15
	 */
	@TableField("status")
	private Integer status;


	/**
	 * 操作时间
	 *
	 * @since 2024/11/15
	 */
	@TableField("oper_time")
	private Date operTime;


	/**
	 * 消耗时间
	 *
	 * @since 2024/11/15
	 */
	@TableField("cost_time")
	private Long costTime;

	/**
	 * 客户端
	 *
	 * @since 2024/11/15
	 */

	private String clientId;

	/**
	 * 人员id
	 *
	 * @since 2024/11/15
	 */

	private Long userId;
}
