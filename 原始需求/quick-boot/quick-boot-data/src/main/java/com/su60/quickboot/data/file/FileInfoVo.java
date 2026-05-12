package com.su60.quickboot.data.file;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 文件详情vo类
 *
 * @author luyanan
 * @since 2024/05/30
 **/
@Data
@Accessors(chain = true)
public class FileInfoVo {

	/**
	 * 文件访问的url
	 *
	 * @since 2024/05/30
	 */

	private String url;


	/**
	 * 文件大小
	 *
	 * @since 2024/05/30
	 */

	private Long size;
	/**
	 * 基础存储路径
	 */
	private String basePath;

	/**
	 * 存储路径
	 */
	private String path;

	/**
	 * 文件扩展名
	 */
	private String ext;

	/**
	 * MIME 类型
	 */
	private String contentType;


	/**
	 * 文件名
	 *
	 * @since 2024/11/20
	 */

	private String fileName;
}
