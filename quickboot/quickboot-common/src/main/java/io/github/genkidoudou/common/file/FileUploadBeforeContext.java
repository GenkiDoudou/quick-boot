package io.github.genkidoudou.common.file;

import org.springframework.web.multipart.MultipartFile;

/**
 * {@link FileUploadHook#beforeUpload} 入参。
 */
public class FileUploadBeforeContext {

    private final MultipartFile multipart;
    private final byte[] bytes;
    private final String filename;
    private final String classify;
    private final String contentType;
    private final long size;

    public FileUploadBeforeContext(MultipartFile multipart, String classify) {
        this.multipart = multipart;
        this.bytes = null;
        this.filename = multipart != null ? multipart.getOriginalFilename() : null;
        this.classify = classify;
        this.contentType = multipart != null ? multipart.getContentType() : null;
        this.size = multipart != null ? multipart.getSize() : 0;
    }

    public FileUploadBeforeContext(byte[] bytes, String filename, String classify, String contentType) {
        this.multipart = null;
        this.bytes = bytes;
        this.filename = filename;
        this.classify = classify;
        this.contentType = contentType;
        this.size = bytes != null ? bytes.length : 0;
    }

    public MultipartFile getMultipart() {
        return multipart;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getFilename() {
        return filename;
    }

    public String getClassify() {
        return classify;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }
}
