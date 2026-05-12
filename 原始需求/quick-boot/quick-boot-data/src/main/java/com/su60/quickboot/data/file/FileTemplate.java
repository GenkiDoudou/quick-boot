package com.su60.quickboot.data.file;

import java.util.List;
import java.util.Map;

/**
 * 文件操作模板
 *
 * @author luyanan
 * @since 2024/06/15
 **/

public interface FileTemplate {


	/**
	 * 给文件添加host(支持多个文件,多个文件路径用,隔开)
	 *
	 * @param filePaths 文件路径
	 * @return 文件路径
	 * @since 2024/06/15
	 */
	Map<String, String> addHost(List<String> filePaths);


	/**
	 * 给文件添加host
	 *
	 * @param filePath 文件路径
	 * @return 文件路径
	 * @since 2024/06/15
	 */
	String addHost(String filePath);


	/**
	 * 移除文件的host为相对路径(支持多个文件,多个文件路径用,隔开)
	 *
	 * @param filePath 文件路径
	 * @return 文件路径
	 * @since 2024/06/15
	 */
	String removeHost(String filePath);


	/**
	 * 移除文件的host为相对路径(支持多个文件,多个文件路径用,隔开)
	 *
	 * @param paths 文件路径
	 * @return 文件路径
	 * @since 2024/06/15
	 */
	Map<String, String> removeHost(List<String> paths);


	/*******************文件上传**************/


	/**
	 * 文件上传
	 *
	 * @param bytes    byte数组
	 * @param fileName 文件名
	 * @param path     路径
	 * @return 文件信息
	 * @since 2024/06/15
	 */
	FileInfoVo upload(byte[] bytes, String fileName, String path);




	/**
	 * 下载文件
	 *
	 * @param fileName 文件名
	 * @return 文件字节
	 * @since 2025/1/1
	 */
	byte[] download(String fileName);

}
