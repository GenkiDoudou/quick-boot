package com.su60.quickboot.data.spring.restful;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.su60.quickboot.common.core.R;
import com.su60.quickboot.data.spring.restful.annotation.NoRestFul;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 返回结果封装
 *
 * @author luyanan
 * @since 2023/09/09
 **/
@RequiredArgsConstructor
@Slf4j
@ControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RestFulResultHandler implements ResponseBodyAdvice {
	private final RestFulProperties restFulProperties;

	@Override
	public boolean supports(MethodParameter returnType, Class converterType) {
		Boolean enable = restFulProperties.getEnable();
		if (!enable) {
			return false;
		}
		boolean flag = returnType.hasMethodAnnotation(NoRestFul.class);
		if (flag) {
			return false;
		}
		String typeName = returnType.getContainingClass().getTypeName();
		if (ArrayUtil.isNotEmpty(restFulProperties.getExcludePackages())) {
			for (String includePackage : restFulProperties.getIncludePackages()) {
				if (typeName.contains(includePackage)) {
					flag = true;
				} else {
					flag = false;
				}
				break;
			}
		}
		if (ArrayUtil.isNotEmpty(restFulProperties.getExcludePackages())) {
			for (String excludePackages : restFulProperties.getExcludePackages()
			) {
				if (typeName.contains(excludePackages)) {
					flag = false;
				} else {
					flag = true;
				}
				break;
			}
		}
		Map<String, Object> controllerBeans = SpringUtil.getApplicationContext().getBeansWithAnnotation(RestController.class);
		controllerBeans.remove("openApiResource");
		controllerBeans.remove("swaggerConfigResource");
		List<String> packNames = controllerBeans.values().stream().map(a -> a.getClass().getPackage().getName()).distinct().collect(Collectors.toList());

//		for (Object bean : controllerBeans.values()) {
//			String packName = bean.getClass().getPackage().getName();
//			if (typeName.contains(packName)) {
//				flag = true;
//			} else {
//				flag = false;
//			}
//			break;
//		}
		return packNames.stream().filter(a -> typeName.contains(a)).findFirst().isPresent();

	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

		if (body instanceof R) {
			return body;
		} else if (body == null) {
			return R.ok();
		} else if (body instanceof String) {
			return R.ok(body);
		} else {
			return R.ok(body);
		}
	}


}
