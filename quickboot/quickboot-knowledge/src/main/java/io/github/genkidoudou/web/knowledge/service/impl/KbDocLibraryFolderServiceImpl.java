package io.github.genkidoudou.web.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.knowledge.constants.KnowledgeConstants;
import io.github.genkidoudou.web.knowledge.domain.KbDocLibraryFile;
import io.github.genkidoudou.web.knowledge.domain.KbDocLibraryFolder;
import io.github.genkidoudou.web.knowledge.dto.KbDocLibraryFolderBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocLibraryFolderTreeVo;
import io.github.genkidoudou.web.knowledge.mapper.KbDocLibraryFileMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbDocLibraryFolderMapper;
import io.github.genkidoudou.web.knowledge.service.KbDocLibraryFolderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识文档库目录服务实现。
 */
@Service
public class KbDocLibraryFolderServiceImpl implements KbDocLibraryFolderService {

    private static final long ROOT_PARENT_ID = 0L;

    private final KbDocLibraryFolderMapper folderMapper;
    private final KbDocLibraryFileMapper fileMapper;

    public KbDocLibraryFolderServiceImpl(KbDocLibraryFolderMapper folderMapper,
                                         KbDocLibraryFileMapper fileMapper) {
        this.folderMapper = folderMapper;
        this.fileMapper = fileMapper;
    }

    @Override
    public List<KbDocLibraryFolderTreeVo> tree() {
        List<KbDocLibraryFolder> all = folderMapper.selectList(
            Wrappers.<KbDocLibraryFolder>lambdaQuery()
                .eq(KbDocLibraryFolder::getDeleted, KnowledgeConstants.NOT_DELETED)
                .orderByAsc(KbDocLibraryFolder::getOrderNum)
                .orderByAsc(KbDocLibraryFolder::getFolderId));
        return buildTree(all);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(KbDocLibraryFolderBo req) {
        Long parentId = req.getParentId() != null ? req.getParentId() : ROOT_PARENT_ID;
        validateParentExists(parentId);
        assertNameUnique(parentId, req.getName(), null);

        KbDocLibraryFolder entity = new KbDocLibraryFolder();
        entity.setParentId(parentId);
        entity.setName(req.getName().trim());
        entity.setOrderNum(req.getOrderNum() != null ? req.getOrderNum() : 0);
        entity.setDeleted(KnowledgeConstants.NOT_DELETED);
        folderMapper.insert(entity);
        return entity.getFolderId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(KbDocLibraryFolderBo req) {
        KbDocLibraryFolder existing = requireFolder(req.getFolderId());
        Long parentId = req.getParentId() != null ? req.getParentId() : existing.getParentId();
        validateParentExists(parentId);
        if (parentId.equals(req.getFolderId())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "父目录不能为自身");
        }
        assertNoCycle(req.getFolderId(), parentId);
        assertNameUnique(parentId, req.getName(), req.getFolderId());

        KbDocLibraryFolder upd = new KbDocLibraryFolder();
        upd.setFolderId(req.getFolderId());
        upd.setParentId(parentId);
        upd.setName(req.getName().trim());
        if (req.getOrderNum() != null) {
            upd.setOrderNum(req.getOrderNum());
        }
        folderMapper.updateById(upd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long folderId) {
        requireFolder(folderId);

        long childFolders = folderMapper.selectCount(
            Wrappers.<KbDocLibraryFolder>lambdaQuery()
                .eq(KbDocLibraryFolder::getParentId, folderId)
                .eq(KbDocLibraryFolder::getDeleted, KnowledgeConstants.NOT_DELETED));
        if (childFolders > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "目录下存在子目录，无法删除");
        }

        long files = fileMapper.selectCount(
            Wrappers.<KbDocLibraryFile>lambdaQuery()
                .eq(KbDocLibraryFile::getFolderId, folderId)
                .eq(KbDocLibraryFile::getDeleted, KnowledgeConstants.NOT_DELETED));
        if (files > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "目录下存在文件，无法删除");
        }

        KbDocLibraryFolder upd = new KbDocLibraryFolder();
        upd.setFolderId(folderId);
        upd.setDeleted(KnowledgeConstants.DELETED);
        folderMapper.updateById(upd);
    }

    private KbDocLibraryFolder requireFolder(Long folderId) {
        if (folderId == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "目录ID不能为空");
        }
        KbDocLibraryFolder row = folderMapper.selectById(folderId);
        if (row == null || KnowledgeConstants.DELETED == row.getDeleted()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "目录不存在或已删除");
        }
        return row;
    }

    private void validateParentExists(Long parentId) {
        if (parentId == null || ROOT_PARENT_ID == parentId) {
            return;
        }
        requireFolder(parentId);
    }

    private void assertNameUnique(Long parentId, String name, Long excludeFolderId) {
        if (StrUtil.isBlank(name)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "目录名称不能为空");
        }
        LambdaQueryWrapper<KbDocLibraryFolder> wrapper = Wrappers.<KbDocLibraryFolder>lambdaQuery()
            .eq(KbDocLibraryFolder::getParentId, parentId)
            .eq(KbDocLibraryFolder::getName, name.trim())
            .eq(KbDocLibraryFolder::getDeleted, KnowledgeConstants.NOT_DELETED);
        if (excludeFolderId != null) {
            wrapper.ne(KbDocLibraryFolder::getFolderId, excludeFolderId);
        }
        if (folderMapper.selectCount(wrapper) > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "同级目录下名称已存在");
        }
    }

    private void assertNoCycle(Long folderId, Long newParentId) {
        if (newParentId == null || ROOT_PARENT_ID == newParentId) {
            return;
        }
        ArrayDeque<Long> stack = new ArrayDeque<>();
        stack.push(newParentId);
        while (!stack.isEmpty()) {
            Long current = stack.pop();
            if (folderId.equals(current)) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不能将目录移动到其子目录下");
            }
            List<KbDocLibraryFolder> children = folderMapper.selectList(
                Wrappers.<KbDocLibraryFolder>lambdaQuery()
                    .eq(KbDocLibraryFolder::getParentId, current)
                    .eq(KbDocLibraryFolder::getDeleted, KnowledgeConstants.NOT_DELETED));
            for (KbDocLibraryFolder child : children) {
                stack.push(child.getFolderId());
            }
        }
    }

    private List<KbDocLibraryFolderTreeVo> buildTree(List<KbDocLibraryFolder> rows) {
        Map<Long, List<KbDocLibraryFolder>> childrenMap = new HashMap<>();
        for (KbDocLibraryFolder row : rows) {
            childrenMap.computeIfAbsent(row.getParentId(), k -> new ArrayList<>()).add(row);
        }
        for (List<KbDocLibraryFolder> list : childrenMap.values()) {
            list.sort(Comparator.comparing(KbDocLibraryFolder::getOrderNum)
                .thenComparing(KbDocLibraryFolder::getFolderId));
        }
        List<KbDocLibraryFolder> roots = childrenMap.getOrDefault(ROOT_PARENT_ID, List.of());
        List<KbDocLibraryFolderTreeVo> out = new ArrayList<>();
        for (KbDocLibraryFolder root : roots) {
            out.add(toTreeVo(root, childrenMap));
        }
        return out;
    }

    private KbDocLibraryFolderTreeVo toTreeVo(KbDocLibraryFolder folder,
                                              Map<Long, List<KbDocLibraryFolder>> childrenMap) {
        KbDocLibraryFolderTreeVo vo = BeanUtil.copyProperties(folder, KbDocLibraryFolderTreeVo.class);
        List<KbDocLibraryFolder> children = childrenMap.getOrDefault(folder.getFolderId(), List.of());
        for (KbDocLibraryFolder child : children) {
            vo.getChildren().add(toTreeVo(child, childrenMap));
        }
        return vo;
    }
}
