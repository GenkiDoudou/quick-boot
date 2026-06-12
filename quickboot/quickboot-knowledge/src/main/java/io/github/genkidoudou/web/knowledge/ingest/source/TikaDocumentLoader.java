package io.github.genkidoudou.web.knowledge.ingest.source;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.file.FileTemplate;
import io.github.genkidoudou.web.system.file.domain.SysFile;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 通过 {@link TikaDocumentReader} 从 {@code sys_file} 相对路径加载文档。
 */
@Component
public class TikaDocumentLoader {

    private final FileTemplate fileTemplate;

    public TikaDocumentLoader(FileTemplate fileTemplate) {
        this.fileTemplate = fileTemplate;
    }

    /**
     * 下载并 Tika 解析文件。
     *
     * @param file 文件元数据，须含有效 {@code relativePath}
     * @return 解析结果
     * @throws IllegalStateException 文件无效或解析为空
     */
    public List<Document> load(SysFile file) {
        if (file == null || StrUtil.isBlank(file.getRelativePath())) {
            throw new IllegalStateException("关联文件不存在或路径为空");
        }
        Resource resource = fileTemplate.download(file.getRelativePath());
        return readResource(resource);
    }

    /**
     * 从内存字节解析文档（预览场景，不落库）。
     *
     * @param bytes    文件字节
     * @param filename 原始文件名，供 Tika 推断类型
     * @return 解析结果
     */
    public List<Document> loadFromBytes(byte[] bytes, String filename) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalStateException("文件内容为空");
        }
        Resource resource = new org.springframework.core.io.ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        return readResource(resource);
    }

    /**
     * 从纯文本构建单文档（手动录入预览）。
     *
     * @param text 正文
     * @return 单元素列表
     */
    public List<Document> loadFromText(String text) {
        if (StrUtil.isBlank(text)) {
            throw new IllegalStateException("正文为空");
        }
        return List.of(new Document(text.trim()));
    }

    private List<Document> readResource(Resource resource) {
        List<Document> parsed = new TikaDocumentReader(resource).read();
        if (parsed.isEmpty()) {
            throw new IllegalStateException("文档解析结果为空");
        }
        return parsed;
    }
}
