package com.su60.quickboot.data.utils;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.data.spring.SpringContextHolder;
import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 消息工具类
 *
 * @author luyanan
 * @since 2024/07/06
 **/
@UtilityClass
public class MessageUtils {

	/**
	 * 获取消息
	 *
	 * @param code 错误编码
	 * @param args 参数
	 * @return 异常消息
	 * @since 2024/07/06
	 */
	public String getMessage(String code, Object... args) {
		String reg = "\\{(.*?)\\}";
		if (StrUtil.isNotBlank(code) && code.matches(reg)) {

			String s = ReUtil.get(reg, code, 0);
			s = s.substring(1, s.length() - 1);
			MessageSource messageSource = SpringContextHolder.getBean(MessageSource.class);
			return messageSource.getMessage(s, args, LocaleContextHolder.getLocale());
		} else {
			return code;
		}
	}

	/**
	 * 获取异常信息
	 *
	 * @param code        异常编码
	 * @param defaultCode 默认的编码
	 * @param msg         异常信息
	 * @param e           异常
	 * @return 异常消息
	 * @since 2024/07/06
	 */
	public String getMessage(String code, String defaultCode, String msg, Exception e) {
		// 优先级  code defaultCode  msg e.msg
		String resultMsg = null;
		if (StrUtil.isNotBlank(code)) {
			resultMsg = getMessage(code);
		}
		if (StrUtil.isNotBlank(defaultCode) && StrUtil.isBlank(resultMsg)) {
			resultMsg = getMessage(defaultCode);
		}
		if (StrUtil.isNotBlank(msg) && StrUtil.isBlank(resultMsg)) {
			resultMsg = msg;
		}
		if (StrUtil.isBlank(resultMsg)) {
			resultMsg = e.getLocalizedMessage();
		}
		return resultMsg;
	}

	/**
	 * 获取提示消息
	 *
	 * @param code 编码
	 * @param e    异常信息
	 * @return 异常消息
	 * @since 2024/07/07
	 */
	public String getMessage(String code, Exception e) {
		return getMessage(code, null, null, e);
	}
}
