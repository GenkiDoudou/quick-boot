package io.github.genkidoudou.web.knowledge.service;

import io.github.genkidoudou.web.knowledge.dto.KbDocLibraryFolderBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocLibraryFolderTreeVo;

import java.util.List;

/**
 * 知识文档库目录服务。
 */
public interface KbDocLibraryFolderService {

    /**
     * 返回完整目录树（未删除记录）。
     */
    List<KbDocLibraryFolderTreeVo> tree();

    /**
     * 新建目录。
     */
    Long add(KbDocLibraryFolderBo req);

    /**
     * 重命名或移动目录。
     */
    void update(KbDocLibraryFolderBo req);

    /**
     * 删除空目录（无子目录、无文件）。
     */
    void remove(Long folderId);
}
