package io.github.genkidoudou.common.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 统一文件门面：上传、下载、访问 URL、预签名、删除等；不暴露 Web Controller。
 */
public interface FileTemplate {

  /**
   * 上传 multipart 文件。
   *
   * @param file     上传文件
   * @param classify 分类键
   * @return 存储相对路径
   */
  String upload(MultipartFile file, String classify);

  /**
   * 上传字节内容。
   *
   * @param content  文件字节
   * @param filename 原始文件名（用于扩展名校验）
   * @param classify 分类键
   * @return 存储相对路径
   */
  String upload(byte[] content, String filename, String classify);

  /**
   * 按相对路径下载为 {@link Resource}。
   *
   * @param relativePath 存储键
   * @return 可读资源流
   */
  Resource download(String relativePath);

  /**
   * 解析对外预览 URL（相对路径 + domain 拼接）。
   *
   * @param relativePath 存储键
   * @return 绝对 URL 或相对路径（未配置 domain 时）
   */
  String view(String relativePath);

  /**
   * 短链 URL；本地实现与 {@link #view} 相同。
   *
   * @param relativePath 存储键
   * @return 对外 URL
   */
  String getShortUrl(String relativePath);

  /**
   * 生成限时访问 URL；本地存储不支持时回退 {@link #view}。
   *
   * @param relativePath  存储键
   * @param expireSeconds 有效秒数
   * @return 预签名或普通 URL
   */
  String getPresignedUrl(String relativePath, int expireSeconds);

  /**
   * 删除存储对象。
   *
   * @param relativePath 存储键
   */
  void delete(String relativePath);

  /**
   * 判断对象是否存在。
   *
   * @param relativePath 存储键
   * @return 存在且为普通文件时 {@code true}
   */
  boolean exists(String relativePath);
}
