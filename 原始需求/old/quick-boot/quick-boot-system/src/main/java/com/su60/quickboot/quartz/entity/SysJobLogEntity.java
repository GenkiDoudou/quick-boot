package com.su60.quickboot.quartz.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 定时任务调度日志表
 * </p>
 *
 * @author luyanan
 * @since 2024/11/07
 */
@Data
@Accessors(chain = true)
@TableName("sys_job_log")
public class SysJobLogEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 任务日志ID
	 *
	 * @since 2024/11/07
	 */

	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;


	/**
	 * 任务名称
	 *
	 * @since 2024/11/07
	 */
	@TableField("job_name")
	private String jobName;


	/**
	 * 任务组名
	 *
	 * @since 2024/11/07
	 */
	@TableField("job_group")
	private String jobGroup;


	/**
	 * 调用目标字符串
	 *
	 * @since 2024/11/07
	 */
	@TableField("invoke_target")
	private String invokeTarget;


	/**
	 * 参数
	 *
	 * @since 2024/11/14
	 */

	private String params;


	/**
	 * 执行状态（0正常 1失败）
	 *
	 * @since 2024/11/07
	 */
	@TableField("status")
	private String status;


	/**
	 * 异常信息
	 *
	 * @since 2024/11/07
	 */
	@TableField("exception_info")
	private String exceptionInfo;


	/**
	 * 创建时间
	 *
	 * @since 2024/11/07
	 */
	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private Date createTime;

	/**
	 * 耗时
	 *
	 * @since 2024/11/14
	 */

	private Long timeConsuming;


	/**
	 * 任务id
	 *
	 * @since 2024/11/14
	 */

	private Long jobId;

	/**
	 * 开始时间
	 *
	 * @since 2024/11/11
	 */

	private Date startTime;


	/**
	 * 结束时间
	 *
	 * @since 2024/11/11
	 */

	private Date endTime;
}
