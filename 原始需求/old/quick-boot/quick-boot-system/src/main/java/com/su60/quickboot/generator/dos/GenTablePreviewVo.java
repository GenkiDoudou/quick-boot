package com.su60.quickboot.generator.dos;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 代码表预览vo类
 *
 * @since 2024/10/30
 */
@Accessors(chain = true)
@Data
public class GenTablePreviewVo {

	/**
	 * 文件名
	 *
	 * @since 2024/10/30
	 */

	private String name;


	/**
	 * 内容
	 *
	 * @since 2024/10/30
	 */

	private String content;
}
