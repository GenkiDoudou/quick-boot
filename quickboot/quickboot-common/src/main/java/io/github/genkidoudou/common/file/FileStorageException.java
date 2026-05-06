package io.github.genkidoudou.common.file;

/**
 * 文件存储模块业务异常（后缀/大小非法、路径不安全、存储失败等），供全局异常处理转换为统一响应。
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
