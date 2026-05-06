package io.github.genkidoudou.common.file;

import java.io.InputStream;

/**
 * 实际读写存储介质（本地磁盘或 MinIO），由 {@link DefaultFileTemplate} 编排路径校验与钩子。
 */
public interface FileStorageOperations {

    void put(String relativePath, InputStream inputStream, long size, String contentType) throws Exception;

    /**
     * 打开只读流；调用方负责关闭。
     */
    InputStream openStream(String relativePath) throws Exception;

    void remove(String relativePath) throws Exception;

    boolean objectExists(String relativePath) throws Exception;

    /**
     * @return 签名下载 URL，仅 MinIO 实现；本地可不被调用
     */
    String presignedGetUrl(String relativePath, int expireSeconds) throws Exception;
}
