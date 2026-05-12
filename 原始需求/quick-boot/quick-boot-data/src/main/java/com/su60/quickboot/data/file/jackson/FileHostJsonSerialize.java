package com.su60.quickboot.data.file.jackson;

import com.su60.quickboot.data.file.FileTemplate;
import com.su60.quickboot.data.spring.SpringContextHolder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * 文件host序列化
 *
 * @author luyanan
 * @since 2024/06/16
 **/
public class FileHostJsonSerialize extends JsonSerializer<String> implements ContextualSerializer {
	private FileHostFormat fileHostFormat;

	@Override
	public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		FileTemplate fileTemplate = SpringContextHolder.getBean(FileTemplate.class);

		if (null != fileTemplate) {

			s = fileTemplate.addHost(s);
		}
		jsonGenerator.writeString(s);
	}


	@Override
	public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
		FileHostFormat fileHostFormat = beanProperty.getAnnotation(FileHostFormat.class);
		this.fileHostFormat = fileHostFormat;
		return this;
	}
}