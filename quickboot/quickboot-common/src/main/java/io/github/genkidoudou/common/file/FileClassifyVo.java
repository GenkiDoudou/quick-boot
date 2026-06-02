package io.github.genkidoudou.common.file;

import lombok.Data;

import org.springframework.util.unit.DataSize;

/**
 * 上传分类配置（对外展示，不含密钥等敏感项）。
 */
@Data
public class FileClassifyVo {

    /** 分类名（上传时 classify 参数）。 */
    private String classify;

    /** 允许扩展名，逗号分隔（如 png,jpg,pdf）；空表示使用内置默认白名单。 */
    private String limitExt;

    /** 大小上限（如 {@code 10MB}、{@code 512KB}）。 */
    private DataSize limitSize;

    /** 大小上限字节数（前端校验用）。 */
    private long limitSizeBytes;

    /** 单次上传允许的最大文件个数。 */
    private int limitCount;

    /** 该分类上传是否允许匿名（未登录）。 */
    private boolean anonymous;
}
