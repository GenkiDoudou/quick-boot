package io.github.genkidoudou.common.file;

/**
 * {@code qc.file.type} 取值。本期仅支持 {@link #local}；{@link #minio} 选中时启动失败。
 */
public enum FileStorageType {
  local,
  minio
}
