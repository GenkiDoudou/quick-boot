package io.github.genkidoudou.common.file;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 本地磁盘存储。
 */
public class LocalFileStorageBackend implements FileStorageOperations {

  private final Path root;

  /**
   * @param root 本地存储根目录（绝对路径）
   */
  public LocalFileStorageBackend(Path root) {
    this.root = root;
  }

  @Override
  public void put(String relativePath, InputStream inputStream, long size, String contentType) throws Exception {
    Path target = FilePathSupport.resolveUnderRoot(root, relativePath);
    Files.createDirectories(target.getParent());
    try (OutputStream out = Files.newOutputStream(target)) {
      inputStream.transferTo(out);
    }
  }

  @Override
  public InputStream openStream(String relativePath) throws Exception {
    Path target = FilePathSupport.resolveUnderRoot(root, relativePath);
    if (!Files.exists(target) || !Files.isRegularFile(target)) {
      throw new FileStorageException("文件不存在: " + relativePath);
    }
    return Files.newInputStream(target);
  }

  @Override
  public void remove(String relativePath) throws Exception {
    Path target = FilePathSupport.resolveUnderRoot(root, relativePath);
    Files.deleteIfExists(target);
  }

  @Override
  public boolean objectExists(String relativePath) throws Exception {
    Path target = FilePathSupport.resolveUnderRoot(root, relativePath);
    return Files.exists(target) && Files.isRegularFile(target);
  }

  @Override
  public String presignedGetUrl(String relativePath, int expireSeconds) {
    throw new UnsupportedOperationException("本地存储不支持 presigned URL");
  }
}
