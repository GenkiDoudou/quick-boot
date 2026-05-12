//package com.su60.quickboot.system.event;
//
//import com.su60.quickboot.core.security.LoginUser;
//import com.su60.quickboot.core.security.LoginUserUtils;
//import com.su60.quickboot.security.event.SecurityEvent;
//import com.su60.quickboot.security.event.SecurityEventDo;
//import com.su60.quickboot.system.dos.SysLogininforDo;
//import com.su60.quickboot.system.service.ISysLogininforService;
//import eu.bitwalker.useragentutils.UserAgent;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.ApplicationListener;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//
///**
// * 登录用户事件监听
// *
// * @author luyanan
// * @since 2024/11/15
// */
//@RequiredArgsConstructor
//@Component
//@Slf4j
//public class LoginUserEventListener implements ApplicationListener<SecurityEvent> {
//	private final ISysLogininforService sysLogininforService;
//
//	@Override
//	public void onApplicationEvent(SecurityEvent event) {
//		log.debug("LoginUserEventListener监听:{}", event);
//		Object source = event.getSource();
//		if (source instanceof SecurityEventDo) {
//			SecurityEventDo securityEventDo = (SecurityEventDo) source;
//			SysLogininforDo sysLogininforDo = new SysLogininforDo();
//			sysLogininforDo.setUserName(securityEventDo.getUsername());
//			LoginUser user = LoginUserUtils.getUser();
//			sysLogininforDo.setUserId(user.getId());
//			sysLogininforDo.setClientId(user.getClientId());
//			sysLogininforDo.setIpaddr(securityEventDo.getIpaddr());
//			UserAgent userAgent = UserAgent.parseUserAgentString(securityEventDo.getUserAgent());
//			sysLogininforDo.setOs(userAgent.getOperatingSystem().getName());
//			sysLogininforDo.setBrowser(userAgent.getBrowser().getName());
//			sysLogininforDo.setLoginTime(new Date());
//			sysLogininforService.saveVo(sysLogininforDo);
//
//		}
//	}
//}
