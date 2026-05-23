package com.su60.quickboot.data.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 读取yml配置文件
 *
 * @author luyanan
 * @since 2024/06/24
 **/
@Slf4j
public class YmlUtils {


	/**
	 * 存放结果
	 *
	 * @since 2024/06/24
	 */

	private static Map<String, String> result = new HashMap<>();

	/**
	 * 根据文件名获取yml的文件内容
	 *
	 * @param filePath getYmlByFileName(bootstrap_file,"spring", "name");
	 * @return 解析结果
	 */
	public static Map<String, String> getYmlByFileName(String filePath) {
		result = new HashMap<>();
		InputStream in = null;
		try {
			File file = ResourceUtils.getFile(filePath);
			in = new BufferedInputStream(new FileInputStream(file));
			Yaml props = new Yaml();
			Object obj = props.loadAs(in, Map.class);
			Map<String, Object> param = (Map<String, Object>) obj;

			for (Map.Entry<String, Object> entry : param.entrySet()) {
				String key = entry.getKey();
				Object val = entry.getValue();
//				if (keys.length != 0 && keys[0].equals(key)) {
////				if (keys.length == 0) {
//					continue;
//				}
				if (val instanceof Map) {
					forEachYaml(key, (Map<String, Object>) val, 1);
				} else {
					result.put(key, val.toString());
				}
			}
			return result;
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return null;
	}

	/**
	 * 根据key获取值
	 *
	 * @param key key
	 * @return 解析结果
	 */
	public static String getValue(String key) throws FileNotFoundException {
		Map<String, String> map = getYmlByFileName(null);
		if (map == null) {
			return null;
		}
		return map.get(key);
	}

	/**
	 * 遍历yml文件，获取map集合
	 *
	 * @param keyStr keyStr
	 * @param obj    文件
	 * @param i      数组
	 * @return 解析结果
	 */
	public static Map<String, String> forEachYaml(String keyStr, Map<String, Object> obj, int i) {
		for (Map.Entry<String, Object> entry : obj.entrySet()) {
			String key = entry.getKey();
			Object val = entry.getValue();
//			if (keys.length > i && !keys[i].equals(key)) {
//				continue;
//			}
			String strNew = "";
			if (StringUtils.isNotEmpty(keyStr)) {
				strNew = keyStr + "." + key;
			} else {
				strNew = key;
			}
			if (val instanceof Map) {
				forEachYaml(strNew, (Map<String, Object>) val, ++i);
				i--;
			} else {

				result.put(strNew, val.toString());
			}
		}

		return result;
	}


}
