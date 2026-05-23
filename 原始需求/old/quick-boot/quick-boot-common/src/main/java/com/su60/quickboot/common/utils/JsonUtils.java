package com.su60.quickboot.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.io.IOException;

@UtilityClass
public class JsonUtils {
	/**
	 * 全局唯一 ObjectMapper
	 *
	 * ObjectMapper 是线程安全的（配置完成后）
	 * 推荐在整个项目中复用
	 */
	private static final ObjectMapper MAPPER = new ObjectMapper();

	static {
		/**
		 * 当 JSON 中出现 DTO 不存在的字段时：
		 * - 不抛异常
		 * - 允许向后兼容
		 */
		MAPPER.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false
		);
	}



	/**
	 * JSON 字符串 → JsonNode
	 *
	 * 使用场景：
	 * - Request Body 解析
	 * - 需要递归遍历 JSON 结构
	 *
	 * @param json 原始 JSON 字符串
	 * @return JsonNode 根节点
	 */
	public static JsonNode parse(String json) {
		try {
			return MAPPER.readTree(json);
		} catch (IOException e) {
			// 统一抛出运行时异常，避免在 Filter 层污染方法签名
			throw new IllegalArgumentException("JSON 解析失败", e);
		}
	}

	/**
	 * JsonNode → JSON 字符串
	 *
	 * 使用场景：
	 * - 处理完成后，重新写回 Request Body
	 *
	 * @param node 处理后的 JsonNode
	 * @return JSON 字符串
	 */
	public static String toJson(JsonNode node) {
		try {
			return MAPPER.writeValueAsString(node);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("JSON 序列化失败", e);
		}
	}

	/**
	 * 暴露 ObjectMapper（可选）
	 *
	 * 适用于：
	 * - 需要和 Spring MVC 的 ObjectMapper 保持一致
	 * - 扩展模块复用同一配置
	 */
	public static ObjectMapper mapper() {
		return MAPPER;
	}
}
