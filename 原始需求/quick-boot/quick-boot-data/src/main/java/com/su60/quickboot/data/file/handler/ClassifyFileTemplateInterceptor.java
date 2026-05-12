package com.su60.quickboot.data.file.handler;

import cn.hutool.crypto.digest.MD5;
import com.su60.quickboot.data.file.FileInfoVo;
import com.su60.quickboot.data.file.FileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 文件分类
 *
 * @author luyanan
 * @since 2025/1/7
 */
@RequiredArgsConstructor
@Order(1)
@Component("classifyFileTemplateInterceptor")
public class ClassifyFileTemplateInterceptor implements FileTemplateInterceptor {


	private final FileProperties fileProperties;

	@Override
	public void preUploadHandler(byte[] bytes, FileInfoVo fileInfoVo) {
		// 对文件名进行处理
		fileInfoVo.setFileName(MD5.create().digestHex(bytes) + "." + fileInfoVo.getExt());

	}

	@Override
	public void postUploadHandler(FileInfoVo fileInfoVo, byte[] bytes) {

	}

	@Override
	public void preDownloadHandler(String fileName) {

	}

	@Override
	public void postDownloadHandler(String fileName, byte[] bytes) {

	}
}
