package com.su60.quickboot.data.file.handler;

import com.su60.quickboot.data.file.FileInfoVo;

/**
 * 文件上传拦截器
 *
 * @author luyanan
 * @since 2025/1/5
 */
public interface FileTemplateInterceptor {


	/**
	 * 上传前的拦截
	 *
	 * @param bytes      字节数组
	 * @param fileInfoVo 文件信息
	 * @since 2025/1/5
	 */
	void preUploadHandler(byte[] bytes, FileInfoVo fileInfoVo);


	/**
	 * 上传后的拦截请i去
	 *
	 * @param fileInfoVo 文件信息
	 * @param bytes      字节数组
	 * @since 2025/1/5
	 */
	void postUploadHandler(FileInfoVo fileInfoVo, byte[] bytes);


	/**
	 * 下载前的拦截
	 *
	 * @param fileName 文件名
	 * @since 2025/1/5
	 */
	void preDownloadHandler(String fileName);


	/**
	 * 下载后的拦截
	 *
	 * @param fileName 文件名
	 * @param bytes    字节数组
	 * @since 2025/1/5
	 */
	void postDownloadHandler(String fileName, byte[] bytes);
}
