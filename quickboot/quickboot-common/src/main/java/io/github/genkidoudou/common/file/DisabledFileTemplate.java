package io.github.genkidoudou.common.file;

import org.springframework.core.io.Resource;

/**
 * {@code qc.file.enabled=false} 时注入，防止误用存储能力。
 */
public class DisabledFileTemplate implements FileTemplate {

  private static final String MSG = "文件存储已禁用（qc.file.enabled=false）";

  @Override
  public String upload(org.springframework.web.multipart.MultipartFile file, String classify) {
    throw new UnsupportedOperationException(MSG);
  }

  @Override
  public String upload(byte[] content, String filename, String classify) {
    throw new UnsupportedOperationException(MSG);
  }

  @Override
  public Resource download(String relativePath) {
    throw new UnsupportedOperationException(MSG);
  }

  @Override
  public String view(String relativePath) {
    throw new UnsupportedOperationException(MSG);
  }

  @Override
  public String getShortUrl(String relativePath) {
    throw new UnsupportedOperationException(MSG);
  }

  @Override
  public String getPresignedUrl(String relativePath, int expireSeconds) {
    throw new UnsupportedOperationException(MSG);
  }

  @Override
  public void delete(String relativePath) {
    throw new UnsupportedOperationException(MSG);
  }

  @Override
  public boolean exists(String relativePath) {
    return false;
  }
}
