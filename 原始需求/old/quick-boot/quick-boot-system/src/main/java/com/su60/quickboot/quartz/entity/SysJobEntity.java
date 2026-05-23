package com.su60.quickboot.quartz.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;

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
@TableName("sys_job")
public class SysJobEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 任务ID
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

	private String jobName;


	/**
	 * 任务组名
	 *
	 * @since 2024/11/07
	 */

	private String jobGroup;


	/**
	 * 调用目标字符串
	 *
	 * @since 2024/11/07
	 */
	@TableField("invoke_target")
	private String invokeTarget;


	/**
	 * cron执行表达式
	 *
	 * @since 2024/11/07
	 */
	@TableField("cron_expression")
	private String cronExpression;


	/**
	 * 计划执行错误策略（1立即执行 2执行一次 3放弃执行）
	 *
	 * @since 2024/11/07
	 */
	@TableField("misfire_policy")
	private String misfirePolicy;


	/**
	 * 是否并发执行（0允许 1禁止）
	 *
	 * @since 2024/11/07
	 */
	@TableField("concurrent")
	private String concurrent;


	/**
	 * 状态（0正常 1暂停）
	 *
	 * @since 2024/11/07
	 */
	@TableField("status")
	private String status;


	/**
	 * 创建者
	 *
	 * @since 2024/11/07
	 */
	@TableField(value = "create_by", fill = FieldFill.INSERT)
	private String createBy;


	/**
	 * 创建时间
	 *
	 * @since 2024/11/07
	 */
	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private Date createTime;


	/**
	 * 更新者
	 *
	 * @since 2024/11/07
	 */
	@TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
	private String updateBy;


	/**
	 * 更新时间
	 *
	 * @since 2024/11/07
	 */
	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;


	/**
	 * 备注信息
	 *
	 * @since 2024/11/07
	 */
	@TableField("remark")
	private String remark;
	/**
	 * 参数
	 *
	 * @since 2024/11/11
	 */

	private String params;

}
