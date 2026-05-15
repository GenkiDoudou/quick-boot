package io.github.genkidoudou.common.file;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传钩子接口
 * 支持上传前置、后置及异常处理，业务方可实现并注册为 Bean，支持 @Order 控制执行顺序
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
public interface FileUploadHook {

    /**
     * 上传前置（MultipartFile）
     *
     * @param file     待上传文件
     * @param classify 分类
     * @return 是否继续上传，false 则中断
     */
    default boolean beforeUpload(MultipartFile file, String classify) {
        return true;
    }

    /**
     * 上传前置（字节数组）
     *
     * @param bytes    文件内容
     * @param filename 文件名
     * @param classify 分类
     * @return 是否继续上传
     */
    default boolean beforeUpload(byte[] bytes, String filename, String classify) {
        return true;
    }

    /**
     * 上传后置
     *
     * @param relativePath 上传后的相对路径
     * @param file         原文件（字节数组上传时可为 null）
     */
    default void afterUpload(String relativePath, MultipartFile file) {
    }

    /**
     * 上传异常（MultipartFile）
     *
     * @param file     原文件
     * @param classify 分类
     * @param e        异常
     */
    default void onError(MultipartFile file, String classify, Throwable e) {
    }

    /**
     * 上传异常（字节数组）
     *
     * @param bytes    文件内容
     * @param filename 文件名
     * @param classify 分类
     * @param e        异常
     */
    default void onError(byte[] bytes, String filename, String classify, Throwable e) {
    }
}
