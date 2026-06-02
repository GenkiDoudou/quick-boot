package io.github.genkidoudou.common.file;

import lombok.Data;

/**
 * 文件上传结果：相对路径入库，绝对路径供前端直接访问。
 */
@Data
public class FileUploadResult {

    /** 原始文件名。 */
    private String fileName;

    /** 存储相对路径（持久化键）。 */
    private String relativePath;

    /** 对外可访问的绝对 URL（由 domain / viewUrlBase 拼接）。 */
    private String absolutePath;

    /** 实际使用的分类名。 */
    private String classify;

    public static FileUploadResult of(String fileName, String relativePath, String absolutePath, String classify) {
        FileUploadResult r = new FileUploadResult();
        r.setFileName(fileName);
        r.setRelativePath(relativePath);
        r.setAbsolutePath(absolutePath);
        r.setClassify(classify);
        return r;
    }
}
