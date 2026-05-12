package com.su60.quickboot.data.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 文件配置
 *
 * @author luyanan
 * @since 2024/05/29
 **/

@Data
@ConfigurationProperties("file")
public class FileProperties {

	/**
	 * 文件前缀
	 *
	 * @since 2024/06/15
	 */

	private String domain;


	/**
	 * 基础路径
	 *
	 * @since 2025/1/7
	 */

	private String basePath;

	/**
	 * 默认的分类
	 *
	 * @since 2024/06/04
	 */

	private FileClassify defaultClassify;

	/**
	 * 文件分类配置
	 *
	 * @since 2024/06/04
	 */

	private List<FileClassify> fileClassifies;


	/**
	 * S3文件存储配置文件
	 *
	 * @since 2025/1/6
	 */

	private S3FileProperties s3;

	/**
	 * 本地存放的配置文件
	 *
	 * @since 2025/1/6
	 */

	private LocalFileProperties local;


	/**
	 * 文件上传的类型(local/minio)
	 *
	 * @since 2025/1/6
	 */

	private String type;

	/**
	 * 文件上传校验配置
	 *
	 * @since 2025/01/07
	 */
	private FileValidationProperties validation = new FileValidationProperties();

	/**
	 * 文件上传校验配置
	 *
	 * @author luyanan
	 * @since 2025/01/07
	 */
	@Data
	public static class FileValidationProperties {

		/**
		 * 是否启用文件校验
		 */
		private Boolean enabled = true;

		/**
		 * 最大文件大小（字节），默认 10MB
		 */
		private Long maxSize = 10 * 1024 * 1024L;

		/**
		 * 允许的文件后缀列表（小写），如：jpg,jpeg,png,gif,pdf,doc,docx
		 */
		private List<String> allowedExtensions;

		/**
		 * 允许的 MIME 类型列表，如：image/jpeg,image/png,application/pdf
		 */
		private List<String> allowedMimeTypes;

		/**
		 * 是否检查文件头魔数（更安全的文件类型检测）
		 */
		private Boolean checkFileMagic = true;

		/**
		 * 是否允许空文件
		 */
		private Boolean allowEmptyFile = false;
	}

	/**
	 * 文件分类配置
	 *
	 * @since 2024/06/04
	 */

	@Data
	public static class FileClassify {


		/**
		 * 分类
		 *
		 * @since 2024/06/04
		 */

		private String classify;


		/**
		 * 限制文件后缀
		 *
		 * @since 2024/06/04
		 */

		private String limitExt;


		/**
		 * 限制文件大小(kb)
		 *
		 * @since 2024/06/04
		 */

		private Long limitSize;

	}


	/**
	 * 本地文件配置
	 *
	 * @author luyanan
	 * @since 2025/1/6
	 */
	@Data
	public static class LocalFileProperties {


		/**
		 * 本地文件存放路径
		 *
		 * @author luyanan
		 * @since 2025/1/6
		 */
		private String path;
	}

	/**
	 * S3 文件存储配置文件
	 *
	 * @author luyanan
	 * @since 2025/1/6
	 */
	@Data
	public static class S3FileProperties {

		/**
		 * MinIO/S3 服务地址，例如：http://127.0.0.1:9000
		 */
		private String endpoint;

		/**
		 * 访问键
		 */
		private String accessKey;

		/**
		 * 密钥
		 */
		private String secretKey;

		/**
		 * 存储桶名称
		 */
		private String bucket;

		/**
		 * 区域（可选）
		 */
		private String region;

		/**
		 * 路径风格访问（MinIO 通常为 true）
		 */
		private Boolean pathStyleAccess = true;
	}
}
