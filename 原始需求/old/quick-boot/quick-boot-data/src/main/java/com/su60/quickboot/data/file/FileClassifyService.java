package com.su60.quickboot.data.file;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.su60.quickboot.data.file.exception.FileExtLimitException;
import com.su60.quickboot.data.file.exception.FileSizeLimitException;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件分类配置类
 *
 * @author luyanan
 * @since 2024/06/05
 **/
@RequiredArgsConstructor
public class FileClassifyService {

	/**
	 * 文件配置类
	 *
	 * @since 2024/11/12
	 */

	private final FileProperties fileProperties;


	/**
	 * 文件限制
	 *
	 * @param ext      后缀
	 * @param size     文件大小
	 * @param classify 分类
	 * @return
	 * @since 2024/06/05
	 */
	public void limit(String ext, Long size, String classify) {
		FileProperties.FileClassify defaultClassify = fileProperties.getDefaultClassify();
		List<FileProperties.FileClassify> fileClassifies = fileProperties.getFileClassifies();
		FileProperties.FileClassify fileClassify = fileClassifies.stream().filter(a -> a.getClassify().equals(classify)).findFirst().orElse(defaultClassify);
		boolean contains = Arrays.stream(fileClassify.getLimitExt().toLowerCase().split(",")).collect(Collectors.toList()).contains(ext.toLowerCase());
		// 当不包含的时候,
		if (!contains) {
			throw new FileExtLimitException(ext);
		}
		// 文件大小限制

		if (fileClassify.getLimitSize() * 1024 < size) {
			throw new FileSizeLimitException(size + "");
		}
	}

	/**
	 * 生成文件名
	 *
	 * @param classify 分类
	 * @param md5      md5
	 * @param ext      后缀
	 * @return 文件名
	 * @since 2024/06/05
	 */
	public String fileName(String classify, String md5, String ext) {
		return "/" + DateUtil.date().toString(DatePattern.PURE_DATE_PATTERN) + "/" + md5 + "." + ext;
	}

}
