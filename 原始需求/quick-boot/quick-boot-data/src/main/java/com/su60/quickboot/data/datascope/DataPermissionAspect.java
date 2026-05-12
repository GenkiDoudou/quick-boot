package com.su60.quickboot.data.datascope;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DataPermissionAspect {

	@Around("@annotation(dataPermission)")
	public Object around(
			ProceedingJoinPoint pjp,
			DataPermission dataPermission) throws Throwable {

		try {
			// 放入 ThreadLocal
			DataPermissionContext.set(dataPermission);
			return pjp.proceed();
		} finally {
			DataPermissionContext.clear();
		}
	}
}
