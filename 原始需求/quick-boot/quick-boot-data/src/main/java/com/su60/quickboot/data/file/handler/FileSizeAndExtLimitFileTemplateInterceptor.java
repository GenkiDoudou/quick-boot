package com.su60.quickboot.data.file.handler;

import cn.hutool.core.util.ArrayUtil;
import com.su60.quickboot.data.file.FileInfoVo;
import com.su60.quickboot.data.file.FileProperties;
import com.su60.quickboot.data.file.exception.FileExtLimitException;
import com.su60.quickboot.data.file.exception.FileSizeLimitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 文件大小和后缀过滤
 *
 * @author luyanan
 * @since 2025/1/8
 */

@RequiredArgsConstructor
@Slf4j
@Order(-1)
@Component("fileSizeAndExtLimitFileTemplateInterceptor")
public class FileSizeAndExtLimitFileTemplateInterceptor implements FileTemplateInterceptor {


	private final FileProperties fileProperties;

	@Override
	public void preUploadHandler(byte[] bytes, FileInfoVo fileInfoVo) {
		String path = fileInfoVo.getPath();
		// 分类
		FileProperties.FileClassify fileClassify = fileProperties.getFileClassifies()
				.stream()
				.filter(a -> a.getClassify().equals(path)).findFirst().orElse(fileProperties.getDefaultClassify());
		// 后缀限制
		if (!ArrayUtil.contains(fileClassify.getLimitExt().split(","), fileInfoVo.getExt())) {
			throw new FileExtLimitException("当前文件后缀为:" + fileInfoVo.getExt() + ",不符合规格");
		}
		// 文件大小限制
//		fileInfoVo.getSize() 转kb
		if (fileClassify.getLimitSize() * 1024 < fileInfoVo.getSize()) {
			throw new FileSizeLimitException("当前文件大小为:" + (fileInfoVo.getSize() / 1024) + "KB,不符合");
		}

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
