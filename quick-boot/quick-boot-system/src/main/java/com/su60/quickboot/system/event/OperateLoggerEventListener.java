package com.su60.quickboot.system.event;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.exception.BaseException;
import com.su60.quickboot.common.utils.IpUtils;
import com.su60.quickboot.core.security.LoginUser;
import com.su60.quickboot.core.security.LoginUserUtils;
import com.su60.quickboot.data.logger.AbstractLoggerParserHandler;
import com.su60.quickboot.data.logger.LoggerEvent;
import com.su60.quickboot.data.logger.LoggerEventDto;
import com.su60.quickboot.data.logger.LoggerInfo;
import com.su60.quickboot.data.utils.MessageUtils;
import com.su60.quickboot.system.dos.SysOperLogDo;
import com.su60.quickboot.system.service.ISysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class OperateLoggerEventListener extends AbstractLoggerParserHandler implements ApplicationListener<LoggerEvent> {

	private final ISysOperLogService sysOperLogService;

	@Override
	public void onApplicationEvent(LoggerEvent loggerEvent) {
		if (!(loggerEvent.getSource() instanceof LoggerEventDto loggerEventDto)) {
			return;
		}
		LoggerInfo loggerInfo = parserToLoggerInfo(loggerEventDto);

		SysOperLogDo sysOperLogDo = new SysOperLogDo();
		LoginUser user = LoginUserUtils.getUser();
		if (null != user) {
			sysOperLogDo.setOperName(user.getUsername());
			sysOperLogDo.setClientId(user.getClientId());
			sysOperLogDo.setUserId(user.getId());
		}
		sysOperLogDo.setOperUrl(loggerInfo.getUri());
		sysOperLogDo.setOperIp(loggerInfo.getSourceIp());
		String region = IpUtils.getRegion(loggerInfo.getSourceIp());
		sysOperLogDo.setOperLocation(region);
		sysOperLogDo.setOperParam(StrUtil.sub(loggerInfo.getRequestParams(), 0, 500));
		Throwable throwable = loggerEventDto.getThrowable();
		String errorMsg = null;
		if (null != throwable) {
			if (throwable instanceof BaseException) {
				BaseException baseException = (BaseException) throwable;

				errorMsg = MessageUtils.getMessage("{" + baseException.getCode() + "}", baseException.getArgs());
			} else {
				errorMsg = throwable.getLocalizedMessage();
			}
		}

		sysOperLogDo.setJsonResult(StrUtil.isNotBlank(errorMsg) ? errorMsg : StrUtil.sub(loggerInfo.getResult(), 0, 1000));
		sysOperLogDo.setStatus(loggerInfo.getCode());
		sysOperLogDo.setOperTime(DateUtil.date(loggerInfo.getStartTime()));
		sysOperLogDo.setCostTime(loggerInfo.getTimeConsuming());
		String methodName = loggerInfo.getMethodName();
		String[] split = methodName.split("\\.");
		methodName = split[split.length - 2] + "." + split[split.length - 1];
		sysOperLogDo.setMethod(methodName);
		sysOperLogDo.setRequestMethod(loggerInfo.getMethod());
		sysOperLogService.saveLog(sysOperLogDo);
	}
}
