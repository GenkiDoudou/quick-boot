package io.github.genkidoudou.web.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import io.github.genkidoudou.web.knowledge.constants.KnowledgeConstants;
import io.github.genkidoudou.web.knowledge.domain.KbDocLibraryFile;
import io.github.genkidoudou.web.knowledge.domain.KbDocLibraryFolder;
import io.github.genkidoudou.web.knowledge.dto.KbDocLibraryFileQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocLibraryFileVo;
import io.github.genkidoudou.web.knowledge.mapper.KbDocLibraryFileMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbDocLibraryFolderMapper;
import io.github.genkidoudou.web.knowledge.service.KbDocLibraryFileService;
import io.github.genkidoudou.web.system.file.dto.SysFileUploadVo;
import io.github.genkidoudou.web.system.file.service.SysFileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 知识文档库文件服务实现。
 */
@Service
public class KbDocLibraryFileServiceImpl implements KbDocLibraryFileService {

    private final KbDocLibraryFileMapper fileMapper;
    private final KbDocLibraryFolderMapper folderMapper;
    private final SysFileService sysFileService;
    private final KnowledgeProperties properties;

    public KbDocLibraryFileServiceImpl(KbDocLibraryFileMapper fileMapper,
                                       KbDocLibraryFolderMapper folderMapper,
                                       SysFileService sysFileService,
                                       KnowledgeProperties properties) {
        this.fileMapper = fileMapper;
        this.folderMapper = folderMapper;
        this.sysFileService = sysFileService;
        this.properties = properties;
    }

    @Override
    public PageInfo<KbDocLibraryFileVo> page(KbDocLibraryFileQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        Page<KbDocLibraryFile> mp = fileMapper.selectPage(
            new Page<>(pageNum, pageSize),
            Wrappers.<KbDocLibraryFile>lambdaQuery()
                .eq(KbDocLibraryFile::getDeleted, KnowledgeConstants.NOT_DELETED)
                .eq(KbDocLibraryFile::getFolderId, query.getFolderId())
                .like(StrUtil.isNotBlank(query.getTitle()), KbDocLibraryFile::getTitle, query.getTitle())
                .orderByDesc(KbDocLibraryFile::getCreateTime));

        List<KbDocLibraryFileVo> rows = new ArrayList<>(mp.getRecords().size());
        for (KbDocLibraryFile row : mp.getRecords()) {
            rows.add(BeanUtil.copyProperties(row, KbDocLibraryFileVo.class));
        }
        Page<KbDocLibraryFileVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public KbDocLibraryFile getById(Long libFileId) {
        if (libFileId == null) {
            return null;
        }
        KbDocLibraryFile row = fileMapper.selectById(libFileId);
        if (row == null || KnowledgeConstants.DELETED == row.getDeleted()) {
            return null;
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KbDocLibraryFileVo upload(Long folderId, MultipartFile file, String remark) {
        requireFolder(folderId);
        if (file == null || file.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "上传文件不能为空");
        }

        String ext = normalizeExt(FileUtil.extName(file.getOriginalFilename()));
        validateExtension(ext);

        long maxBytes = (long) properties.getLibrary().getMaxFileSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "文件大小超过限制（最大 " + properties.getLibrary().getMaxFileSizeMb() + "MB）");
        }

        SysFileUploadVo fileVo = sysFileService.upload(file, KnowledgeConstants.LIBRARY_FILE_CLASSIFY);
        String title = StrUtil.blankToDefault(file.getOriginalFilename(), fileVo.getFileName());

        KbDocLibraryFile entity = new KbDocLibraryFile();
        entity.setFolderId(folderId);
        entity.setFileId(fileVo.getFileId());
        entity.setTitle(title);
        entity.setFileExt(ext);
        entity.setFileSize(file.getSize());
        entity.setRemark(StrUtil.nullToEmpty(remark));
        entity.setDeleted(KnowledgeConstants.NOT_DELETED);
        fileMapper.insert(entity);

        return BeanUtil.copyProperties(entity, KbDocLibraryFileVo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> libFileIds) {
        if (libFileIds == null || libFileIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "删除文件ID不能为空");
        }
        for (Long libFileId : libFileIds) {
            if (getById(libFileId) == null) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "存在无效的文件ID: " + libFileId);
            }
        }
        for (Long libFileId : libFileIds) {
            KbDocLibraryFile upd = new KbDocLibraryFile();
            upd.setLibFileId(libFileId);
            upd.setDeleted(KnowledgeConstants.DELETED);
            fileMapper.updateById(upd);
        }
    }

    private void requireFolder(Long folderId) {
        if (folderId == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "目录ID不能为空");
        }
        KbDocLibraryFolder folder = folderMapper.selectById(folderId);
        if (folder == null || KnowledgeConstants.DELETED == folder.getDeleted()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "目录不存在或已删除");
        }
    }

    private void validateExtension(String ext) {
        if (StrUtil.isBlank(ext)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "无法识别文件扩展名");
        }
        List<String> allowed = properties.getLibrary().getAllowedExtensions();
        if (allowed == null || allowed.isEmpty()) {
            return;
        }
        boolean ok = allowed.stream()
            .map(e -> e.toLowerCase(Locale.ROOT))
            .anyMatch(e -> e.equals(ext));
        if (!ok) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "不支持的文件类型，允许: " + String.join(",", allowed));
        }
    }

    private static String normalizeExt(String ext) {
        if (StrUtil.isBlank(ext)) {
            return "";
        }
        return ext.toLowerCase(Locale.ROOT);
    }
}
