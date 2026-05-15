package io.github.genkidoudou.common.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件操作统一接口
 * 支持上传、下载、查看、删除等操作，可适配本地文件存储和 MinIO 对象存储
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
public interface FileTemplate {

    /**
     * 上传文件（MultipartFile）
     *
     * @param file    文件
     * @param classify 文件分类，必传。根据分类校验后缀、大小，并保存到对应分类目录
     * @return 相对路径，如 "image/2026/03/xxx.jpg"
     */
    String upload(MultipartFile file, String classify);

    /**
     * 根据字节数组上传文件
     *
     * @param bytes    文件内容
     * @param filename 原始文件名（用于提取扩展名），如 "photo.jpg"
     * @param classify 文件分类，必传
     * @return 相对路径
     */
    String upload(byte[] bytes, String filename, String classify);

    /**
     * 上传文件（使用默认分类）
     *
     * @param file 文件
     * @return 相对路径
     */
    String upload(MultipartFile file);

    /**
     * 下载文件
     *
     * @param relativePath 相对路径
     * @return 文件流或 Resource
     */
    Resource download(String relativePath);

    /**
     * 查看/预览（返回完整可访问 URL）
     *
     * @param relativePath 相对路径
     * @return 完整 URL，如 https://xxx.com/image/2026/03/xxx.jpg
     */
    String view(String relativePath);

    /**
     * 获取短链 URL（用于分享、二维码等场景）
     *
     * @param relativePath 相对路径
     * @return 短链 URL，如 https://xxx.com/s/abc123
     */
    String getShortUrl(String relativePath);

    /**
     * 获取有时效性的 URL（如 1 小时内有效，适用于私有文件临时访问）
     *
     * @param relativePath  相对路径
     * @param expireSeconds 过期时间（秒）
     * @return 带签名的临时 URL
     */
    String getPresignedUrl(String relativePath, long expireSeconds);

    /**
     * 删除文件
     *
     * @param relativePath 相对路径
     * @return 是否成功
     */
    boolean delete(String relativePath);

    /**
     * 判断文件是否存在
     *
     * @param relativePath 相对路径
     * @return 是否存在
     */
    boolean exists(String relativePath);
}
