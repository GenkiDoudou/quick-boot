package io.github.genkidoudou.common.file;

import java.io.InputStream;

/**
 * 实际读写存储介质（本期仅本地磁盘），由 {@link DefaultFileTemplate} 编排路径校验与钩子。
 */
public interface FileStorageOperations {

  /**
   * 写入对象到存储。
   *
   * @param relativePath 相对路径键
   * @param inputStream  内容流
   * @param size         字节数（部分实现可忽略）
   * @param contentType  MIME，可 null
   */
  void put(String relativePath, InputStream inputStream, long size, String contentType) throws Exception;

  /**
   * 打开只读流；调用方负责关闭。
   */
  InputStream openStream(String relativePath) throws Exception;

  void remove(String relativePath) throws Exception;

  boolean objectExists(String relativePath) throws Exception;

  /**
   * @return 签名下载 URL；本地实现抛 {@link UnsupportedOperationException}
   */
  String presignedGetUrl(String relativePath, int expireSeconds) throws Exception;
}
