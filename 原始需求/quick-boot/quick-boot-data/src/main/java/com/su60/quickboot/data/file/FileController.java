package com.su60.quickboot.data.file;

import com.su60.quickboot.common.core.R;
import com.su60.quickboot.data.file.utils.FileTypeDetector;
import com.su60.quickboot.data.file.validation.FileValidationException;
import com.su60.quickboot.data.file.validation.FileValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件控制层
 *
 * @author luyanan
 * @since 2024/05/29
 **/
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("file")
public class FileController {

	private final FileTemplate fileTemplate;
	private final FileValidationService fileValidationService;

	/**
	 * 文件上传
	 *
	 * @param file     文件
	 * @param classify 分类
	 * @return 文件信息
	 * @since 2024/05/30
	 */
	@ResponseBody
	@PostMapping("upload/{classify}")
	public R<FileInfoVo> upload(@RequestParam("file") MultipartFile file, @PathVariable("classify") String classify) {
		try {
			// 1. 文件校验
			fileValidationService.validateFile(file, classify);
			
			// 2. 上传文件
			FileInfoVo upload = fileTemplate.upload(file.getBytes(), file.getOriginalFilename(), classify);
			
			log.info("文件上传成功: {}, 分类: {}, 大小: {} bytes", 
				file.getOriginalFilename(), classify, file.getSize());
			
			return R.ok(upload);
			
		} catch (FileValidationException e) {
			log.warn("文件校验失败: {}, 原因: {}", file.getOriginalFilename(), e.getMessage());
			return (R<FileInfoVo>) R.failed(e.getMessage());
		} catch (IOException e) {
			log.error("文件上传失败: {}, 原因: {}", file.getOriginalFilename(), e.getMessage(), e);
			return (R<FileInfoVo>) R.failed("文件上传失败: " + e.getMessage());
		} catch (Exception e) {
			log.error("文件上传异常: {}, 原因: {}", file.getOriginalFilename(), e.getMessage(), e);
			return (R<FileInfoVo>) R.failed("文件上传异常: " + e.getMessage());
		}
	}


	/**
	 * 图片查看
	 *
	 * @param request  请求类
	 * @param response 响应类
	 * @param classify 分类
	 * @return
	 * @since 2024/06/15
	 */
	@GetMapping("view/{classify}/**")
	public void view(HttpServletRequest request, HttpServletResponse response, @PathVariable("classify") String classify) {
		try {
			String requestURI = request.getRequestURI();
			
			// 路径安全检查
			if (requestURI.contains("..") || requestURI.contains("//")) {
				log.warn("检测到可疑路径访问: {}", requestURI);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			
			byte[] download = fileTemplate.download(requestURI);
			String contentType = FileTypeDetector.detectFileType(requestURI, download);
			response.setContentType(contentType);
			response.getOutputStream().write(download);
			
		} catch (Exception e) {
			log.error("文件查看异常: {}, 原因: {}", request.getRequestURI(), e.getMessage(), e);
			try {
				if (e instanceof IOException) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				} else {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			} catch (Exception ex) {
				log.error("设置响应状态失败", ex);
			}
		}
	}


}
