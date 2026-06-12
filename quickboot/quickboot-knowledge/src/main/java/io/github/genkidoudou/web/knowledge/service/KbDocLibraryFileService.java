package io.github.genkidoudou.web.knowledge.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.knowledge.domain.KbDocLibraryFile;
import io.github.genkidoudou.web.knowledge.dto.KbDocLibraryFileQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocLibraryFileVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识文档库文件服务。
 */
public interface KbDocLibraryFileService {

    /**
     * 分页列出目录下文件。
     */
    PageInfo<KbDocLibraryFileVo> page(KbDocLibraryFileQueryBo query);

    /**
     * 按 ID 查询文档库文件。
     */
    KbDocLibraryFile getById(Long libFileId);

    /**
     * 上传文件到文档库目录。
     */
    KbDocLibraryFileVo upload(Long folderId, MultipartFile file, String remark);

    /**
     * 批量删除文档库文件（逻辑删）。
     */
    void removeBatch(List<Long> libFileIds);
}
