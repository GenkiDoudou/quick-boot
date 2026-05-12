package com.su60.quickboot.quartz.exception;

import com.su60.quickboot.common.exception.WarningException;
import lombok.Getter;

/**
 * 定时任务异常
 *
 * @author luyanan
 * @since 2024/11/12
 */
public class TaskException extends WarningException {
	public TaskException(Code code, Object... args) {
		super(code.code, args);
	}

	@Getter
	public enum Code {
		TASK_EXISTS(300001),

		NO_TASK_EXISTS(300002), TASK_ALREADY_STARTED(300003), UNKNOWN(300004), CONFIG_ERROR(300005),

		TASK_NODE_NOT_AVAILABLE(300006);


		private Integer code;

		Code(Integer code) {
			this.code = code;
		}

		;

	}
}
