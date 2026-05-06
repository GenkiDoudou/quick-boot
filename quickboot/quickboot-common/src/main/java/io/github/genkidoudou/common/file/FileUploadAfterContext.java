package io.github.genkidoudou.common.file;

/**
 * {@link FileUploadHook#afterUpload} 入参。
 */
public class FileUploadAfterContext {

    private final String relativePath;
    private final FileUploadBeforeContext before;

    public FileUploadAfterContext(String relativePath, FileUploadBeforeContext before) {
        this.relativePath = relativePath;
        this.before = before;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public FileUploadBeforeContext getBefore() {
        return before;
    }
}
