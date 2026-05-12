package com.su60.quickboot.common.utils;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * json解析
 *
 * @author luyanan
 * @since 2026/1/15
 */
@UtilityClass
public class JsonTraverseUtil {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public String traverse(String json, BiFunction<JsonNode, Object, String> biFunction) throws Exception {
		JsonNode root = MAPPER.readTree(json);
		modify(root, null, null, biFunction);
		return root.toPrettyString();
	}

	/**
	 * @param node   当前节点
	 * @param parent 父节点（ObjectNode 或 ArrayNode）
	 * @param key    当前节点在父节点中的 key / index
	 */
	private static void modify(JsonNode node, JsonNode parent, Object key, BiFunction<JsonNode, Object, String> biFunction) {
		// 1. 对象
		if (node.isObject()) {
			ObjectNode obj = (ObjectNode) node;
			Iterator<Map.Entry<String, JsonNode>> it = obj.fields();
			while (it.hasNext()) {
				Map.Entry<String, JsonNode> entry = it.next();
				modify(entry.getValue(), obj, entry.getKey(), biFunction);
			}
			return;
		}

		// 2. 数组
		if (node.isArray()) {
			ArrayNode array = (ArrayNode) node;
			for (int i = 0; i < array.size(); i++) {
				modify(array.get(i), array, i, biFunction);
			}
			return;
		}

		// 3. 基本类型（⭐这里才是真正修改的地方）
		if (node.isValueNode() && parent != null) {

			String oldValue = node.asText();

			String newValue = biFunction.apply(node, key);

			// 示例：敏感词替换
//			String newValue = oldValue.replace("张三", "**");

			// 如果值没变，直接跳过
			if (oldValue.equals(newValue)) {
				return;
			}

			// ⭐关键：在父节点上替换
			JsonNode newNode = TextNode.valueOf(newValue);

			if (parent instanceof ObjectNode objectNode) {
				objectNode.set((String) key, newNode);
			} else if (parent instanceof ArrayNode arrayNode) {
				arrayNode.set((Integer) key, newNode);
			}
		}
	}


	public static void main(String[] args) throws Exception {
		// 给我造一个Map的对象  内容是嵌套的多级菜单的哪种
		Map<String, Object> nestedMenu =
				createNestedMenu();
		String json1 = JsonTraverseUtil.traverse(JSONUtil.toJsonStr(nestedMenu), (node, path) -> {
			if (node.isTextual()) {
				String text = node.asText();
				if (text.contains("用户管理")) {
//					System.out.println("path=" + path + "------------, value=" + node.asText());
					return "用户管理+11111";
				} else {
					return text;
				}
			}
			return node.asText();
		});
		System.out.println(json1);

		System.out.println("------------------");
		List<Map<String, Object>> menus = new ArrayList<>();
		menus.add(nestedMenu);
		String json2 = JsonTraverseUtil.traverse(JSONUtil.toJsonStr(menus), (node, path) -> {
			if (node.isTextual()) {
				String text = node.asText();
//				System.out.println("path=" + path + "------------, value=" + node.asText());
				if (text.contains("用户管理")) {
					return "用户管理+11111";
				} else {
					return text;
				}
			}
			return node.asText();
		});
		System.out.println(json2);
	}


	public static Map<String, Object> createNestedMenu() {
		Map<String, Object> rootMenu = new HashMap<>();

		// 一级菜单：首页
		Map<String, Object> homeMenu = new HashMap<>();
		homeMenu.put("id", 1);
		homeMenu.put("name", "首页");
		homeMenu.put("url", "/home");
		homeMenu.put("children", new ArrayList<>());

		// 一级菜单：产品管理
		Map<String, Object> productMenu = new HashMap<>();
		productMenu.put("id", 2);
		productMenu.put("name", "产品管理");
		productMenu.put("url", "/products");

		// 二级菜单：产品列表
		List<Map<String, Object>> productChildren = new ArrayList<>();
		Map<String, Object> productList = new HashMap<>();
		productList.put("id", 3);
		productList.put("name", "产品列表");
		productList.put("url", "/products/list");

		// 三级菜单：添加产品
		List<Map<String, Object>> listChildren = new ArrayList<>();
		Map<String, Object> addProduct = new HashMap<>();
		addProduct.put("id", 4);
		addProduct.put("name", "添加产品");
		addProduct.put("url", "/products/add");
		listChildren.add(addProduct);

		Map<String, Object> editProduct = new HashMap<>();
		editProduct.put("id", 5);
		editProduct.put("name", "编辑产品");
		editProduct.put("url", "/products/edit");
		listChildren.add(editProduct);

		productList.put("children", listChildren);
		productChildren.add(productList);

		// 二级菜单：分类管理
		Map<String, Object> categoryMenu = new HashMap<>();
		categoryMenu.put("id", 6);
		categoryMenu.put("name", "分类管理");
		categoryMenu.put("url", "/categories");
		categoryMenu.put("children", new ArrayList<>());
		productChildren.add(categoryMenu);

		productMenu.put("children", productChildren);

		// 一级菜单：用户管理
		Map<String, Object> userMenu = new HashMap<>();
		userMenu.put("id", 7);
		userMenu.put("name", "用户管理");
		userMenu.put("url", "/users");

		List<Map<String, Object>> userChildren = new ArrayList<>();
		Map<String, Object> userList = new HashMap<>();
		userList.put("id", 8);
		userList.put("name", "用户列表");
		userList.put("url", "/users/list");
		userList.put("children", new ArrayList<>());
		userChildren.add(userList);

		userMenu.put("children", userChildren);

		// 添加到根菜单
		List<Map<String, Object>> menus = new ArrayList<>();
		menus.add(homeMenu);
		menus.add(productMenu);
		menus.add(userMenu);

		rootMenu.put("menus", menus);
		rootMenu.put("title", "系统导航菜单");

		return rootMenu;
	}
}
