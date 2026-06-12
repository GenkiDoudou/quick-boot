package io.github.genkidoudou.web.knowledge.ingest.source;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.constants.KbDocSourceType;
import io.github.genkidoudou.web.knowledge.constants.KnowledgeConstants;
import io.github.genkidoudou.web.knowledge.domain.KbDocLibraryFile;
import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.mapper.KbDocLibraryFileMapper;
import io.github.genkidoudou.web.system.file.domain.SysFile;
import io.github.genkidoudou.web.system.file.mapper.SysFileMapper;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档库来源：通过 {@code library_file_id} 定位库文件，再 Tika 解析其关联 {@code sys_file}。
 */
@Component
public class LibraryDocumentSourceAdapter implements DocumentSourceAdapter {

    private final KbDocLibraryFileMapper libraryFileMapper;
    private final SysFileMapper sysFileMapper;
    private final TikaDocumentLoader tikaDocumentLoader;

    public LibraryDocumentSourceAdapter(KbDocLibraryFileMapper libraryFileMapper,
                                        SysFileMapper sysFileMapper,
                                        TikaDocumentLoader tikaDocumentLoader) {
        this.libraryFileMapper = libraryFileMapper;
        this.sysFileMapper = sysFileMapper;
        this.tikaDocumentLoader = tikaDocumentLoader;
    }

    @Override
    public String sourceType() {
        return KbDocSourceType.LIBRARY;
    }

    @Override
    public List<Document> load(DocumentSourceContext context) {
        KbDocument doc = context.getDocument();
        Long libraryFileId = doc.getLibraryFileId();
        if (libraryFileId == null) {
            throw new IllegalStateException("文档库来源缺少 libraryFileId");
        }
        KbDocLibraryFile libraryFile = libraryFileMapper.selectById(libraryFileId);
        if (libraryFile == null || KnowledgeConstants.DELETED == libraryFile.getDeleted()) {
            throw new IllegalStateException("文档库文件不存在或已删除: " + libraryFileId);
        }
        SysFile file = sysFileMapper.selectById(libraryFile.getFileId());
        if (file == null || StrUtil.isBlank(file.getRelativePath())) {
            throw new IllegalStateException("文档库关联物理文件不存在");
        }
        return tikaDocumentLoader.load(file);
    }
}
