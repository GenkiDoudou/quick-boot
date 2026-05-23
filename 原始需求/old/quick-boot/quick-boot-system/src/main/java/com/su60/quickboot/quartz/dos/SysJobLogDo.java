package com.su60.quickboot.quartz.dos;

import cn.hutool.core.date.DatePattern;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class SysJobLogDo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 任务日志ID
	 *
	 * @since 2024/11/07
	 */
	@NotNull(groups = UpdateGroup.class, message = "任务日志ID 不能为空")
	private Long id;


	/**
	 * 任务名称
	 *
	 * @since 2024/11/07
	 */
	@NotBlank(groups = AddGroup.class, message = "任务名称不能为空")
	private String jobName;


	/**
	 * 任务组名
	 *
	 * @since 2024/11/07
	 */
	@NotBlank(groups = AddGroup.class, message = "任务组名不能为空")
	private String jobGroup;


	/**
	 * 调用目标字符串
	 *
	 * @since 2024/11/07
	 */
	@NotBlank(groups = AddGroup.class, message = "调用目标字符串不能为空")
	private String invokeTarget;

	/**
	 * 耗时
	 *
	 * @since 2024/11/14
	 */

	private Long timeConsuming;


	/**
	 * 执行状态（0正常 1失败）
	 *
	 * @since 2024/11/07
	 */
	private String status;


	/**
	 * 异常信息
	 *
	 * @since 2024/11/07
	 */
	private String exceptionInfo;


	/**
	 * 创建时间
	 *
	 * @since 2024/11/07
	 */
	@JsonFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
	private Date createTime;

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

	/**
	 * 任务id
	 *
	 * @since 2024/11/14
	 */

	private Long jobId;

	/**
	 * 参数
	 *
	 * @since 2024/11/14
	 */

	private String params;

}
