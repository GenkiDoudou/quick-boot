package com.su60.quickboot.quartz.dos;

import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 定时任务调度表
 * </p>
 *
 * @author luyanan
 * @since 2024/11/07
 */
@Data
@Accessors(chain = true)
public class SysJobDo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 任务ID
	 *
	 * @since 2024/11/07
	 */
	@NotNull(groups = UpdateGroup.class, message = "任务ID 不能为空")
	private Long id;


	/**
	 * 任务名称
	 *
	 * @since 2024/11/07
	 */
	@NotNull(groups = UpdateGroup.class, message = "任务名称 不能为空")
	private String jobName;


	/**
	 * 任务组名
	 *
	 * @since 2024/11/07
	 */
	@NotNull(groups = UpdateGroup.class, message = "任务组名 不能为空")
	private String jobGroup;


	/**
	 * 调用目标字符串
	 *
	 * @since 2024/11/07
	 */
	@NotBlank(groups = AddGroup.class, message = "调用目标字符串不能为空")
	private String invokeTarget;


	/**
	 * cron执行表达式
	 *
	 * @since 2024/11/07
	 */
	@NotBlank(groups = AddGroup.class, message = "cron执行表达式不能为空")
	private String cronExpression;


	/**
	 * 计划执行错误策略（1立即执行 2执行一次 3放弃执行）
	 *
	 * @since 2024/11/07
	 */
	@NotBlank(groups = AddGroup.class, message = "计划执行错误策略（1立即执行 2执行一次 3放弃执行）不能为空")
	private String misfirePolicy;


	/**
	 * 是否并发执行（0允许 1禁止）
	 *
	 * @since 2024/11/07
	 */
	@NotBlank(groups = AddGroup.class, message = "是否并发执行（0允许 1禁止）不能为空")
	private String concurrent;


	/**
	 * 状态（0正常 1暂停）
	 *
	 * @since 2024/11/07
	 */
	@NotBlank(groups = AddGroup.class, message = "状态（0正常 1暂停）不能为空")
	private String status;


	/**
	 * 创建者
	 *
	 * @since 2024/11/07
	 */
	@JsonIgnore
	private String createBy;


	/**
	 * 创建时间
	 *
	 * @since 2024/11/07
	 */
	private Date createTime;


	/**
	 * 更新者
	 *
	 * @since 2024/11/07
	 */
	@JsonIgnore
	private String updateBy;


	/**
	 * 更新时间
	 *
	 * @since 2024/11/07
	 */
	@JsonIgnore
	private Date updateTime;


	/**
	 * 备注信息
	 *
	 * @since 2024/11/07
	 */
	private String remark;


	/**
	 * 参数
	 *
	 * @since 2024/11/11
	 */

	private String params;

	/**
	 * 下次执行时间
	 *
	 * @since 2024/11/14
	 */

	private String nextTime;
}
