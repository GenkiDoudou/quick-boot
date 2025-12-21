package com.su60.quickboot.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

	private final ObjectMapper objectMapper;

	public JacksonConfig(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@PostConstruct
	public void configureObjectMapper() {
		// 将 Long、long、BigInteger 序列化为字符串
		SimpleModule module = new SimpleModule();
		module.addSerializer(Long.class, ToStringSerializer.instance);
		module.addSerializer(Long.TYPE, ToStringSerializer.instance);
		// 如果用到 BigInteger
		// module.addSerializer(BigInteger.class, ToStringSerializer.instance);

		objectMapper.registerModule(module);
	}
}