package com.su60.quickboot.data.file.jackson;

import com.su60.quickboot.data.file.FileTemplate;
import com.su60.quickboot.data.spring.SpringContextHolder;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * 文件host 反序列化
 *
 * @author luyanan
 * @since 2024/06/16
 **/
public class FileHostJsonDeserialize extends JsonDeserializer<String> {
	@Override
	public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
		String text = jsonParser.getText();
		FileTemplate fileTemplate = SpringContextHolder.getBean(FileTemplate.class);
		if (null != fileTemplate) {
			text = fileTemplate.removeHost(text);
		}
		return text;
	}
}
