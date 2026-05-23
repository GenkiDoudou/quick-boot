package com.su60.quickboot.data.file;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.su60.quickboot.data.file.handler.FileTemplateInterceptor;
import com.su60.quickboot.data.file.utils.FileTypeDetector;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件操作模板类抽象类
 *
 * @author luyanan
 * @since 2024/06/15
 **/
public abstract class AbstractFileTemplate implements FileTemplate {


	/**
	 * 拦截
	 *
	 * @author luyanan
	 * @since 2025/1/5
	 */
	@Autowired
	private ObjectProvider<FileTemplateInterceptor> fileTemplateInterceptors;


	@Autowired
	private FileProperties fileProperties;
	/**
	 * http
	 *
	 * @since 2022/2/6
	 */

	private static final String HTTP = "http";

	/**
	 * https
	 *
	 * @since 2022/2/6
	 */

	private static final String HTTPS = "https";

	/**
	 * 逗号
	 *
	 * @since 2022/2/6
	 */

	private final static String COMMA = ",";

	@Override
	public Map<String, String> addHost(List<String> filePaths) {
		if (CollectionUtil.isEmpty(filePaths)) {
			return new HashMap<String, String>(0);
		}
		Map<String, String> res = new HashMap<>(filePaths.size());
		for (String p : filePaths) {
			if (StrUtil.isBlank(p)) {
				continue;
			}
			String withHost = addHost(p);
			res.put(p, withHost);
		}
		return res;
	}

	@Override
	public String addHost(String filePath) {
		String domain = fileProperties.getDomain();
		Assert.notBlank(domain, "customDomain配置不能为空");
		if (StrUtil.isBlank(filePath)) {
			return filePath;
		}
		// 兼容老系统
		return Arrays.stream(filePath.split(COMMA)).filter(StrUtil::isNotBlank)
				.map(p -> {
					if (p.startsWith(HTTP) || p.startsWith(HTTPS)) {
						return p;
					}
					return URLUtil.normalize((domain + "/" + p).replace("/+", "/"), true, true);
				}).collect(Collectors.joining(COMMA));

	}

	@Override
	public String removeHost(String filePath) {
		String domain = fileProperties.getDomain();

		if (StrUtil.isBlank(filePath)) {
			return filePath;
		}
		return Arrays
				.stream(filePath.split(COMMA))
				.map(a -> a.replace(domain, ""))
				.collect(Collectors.joining(COMMA));
	}

	@Override
	public Map<String, String> removeHost(List<String> paths) {
		String domain = fileProperties.getDomain();

		return Optional
				.ofNullable(paths)
				.filter(a -> CollectionUtil.isNotEmpty(a)).map(ps -> {
					if (CollectionUtil.isEmpty(ps)) {
						return new HashMap<>(0);
					}
					Map<String, String> pathMap = new HashMap<>(paths.size());
					ps.stream().filter(a -> StrUtil.isNotBlank(a)).forEach(p -> {
						String relativePath = p.replace(domain, "");
						pathMap.put(p, relativePath);
					});
					return pathMap;
				}).orElse(new HashMap(0));
	}


	@Override
	public FileInfoVo upload(byte[] bytes, String fileName, String path) {
		String contentType = FileTypeDetector.detectFileType(fileName, bytes);
		FileInfoVo fileInfoVo = new FileInfoVo()
				.setFileName(fileName)
				.setPath(path)
				.setBasePath(fileProperties.getBasePath())
				.setExt(FileUtil.extName(fileName))
				.setContentType(contentType)
				.setSize((long) bytes.length/1024);

		// 上传前的拦截
		fileTemplateInterceptors.orderedStream().forEachOrdered(handler -> {

			handler.preUploadHandler(bytes, fileInfoVo);
		});
		doUpload(bytes, fileInfoVo);
		// 上传后的拦截
		fileTemplateInterceptors.orderedStream().forEachOrdered(handler -> {
			handler.postUploadHandler(fileInfoVo, bytes);
		});


		// 设置url
		fileInfoVo.setUrl(fileProperties.getDomain() + fileProperties.getBasePath() + fileInfoVo.getPath() + "/" + fileInfoVo.getFileName());
		return fileInfoVo;
	}


	/**
	 * 上传的方法
	 *
	 * @param bytes      字节数字
	 * @param fileInfoVo 文件信息
	 * @since 2025/1/1
	 */
	protected abstract void doUpload(byte[] bytes, FileInfoVo fileInfoVo);


	@Override
	public byte[] download(String fileName) {
		String newFileName = fileName.replaceFirst(fileProperties.getBasePath(), "");
		// 上传前的拦截
		fileTemplateInterceptors.orderedStream().forEachOrdered(handler -> {
			handler.preDownloadHandler(newFileName);
		});
		byte[] bytes = doDownload(newFileName);
		// 上传后的拦截
		fileTemplateInterceptors.orderedStream().forEachOrdered(handler -> {
			handler.postDownloadHandler(newFileName, bytes);
		});
		return bytes;
	}

	/**
	 * 文件下载
	 *
	 * @param fileName 文件名
	 * @return 文件字节数组
	 * @since 2025/1/1
	 */
	protected abstract byte[] doDownload(String fileName);
}
