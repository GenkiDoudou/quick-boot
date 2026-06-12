package io.github.genkidoudou.web.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import io.github.genkidoudou.web.knowledge.constants.KbChunkDelimiter;
import io.github.genkidoudou.web.knowledge.constants.KbSegmentMode;
import io.github.genkidoudou.web.knowledge.constants.KnowledgeConstants;
import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.domain.KbDocumentChunk;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBase;
import io.github.genkidoudou.web.knowledge.dto.KbKnowledgeBaseBo;
import io.github.genkidoudou.web.knowledge.dto.KbKnowledgeBaseQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbKnowledgeBaseVo;
import io.github.genkidoudou.web.knowledge.mapper.KbDocumentChunkMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbDocumentMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbKnowledgeBaseMapper;
import io.github.genkidoudou.web.knowledge.service.KbKnowledgeBaseMcpService;
import io.github.genkidoudou.web.knowledge.service.KbKnowledgeBaseService;
import io.github.genkidoudou.web.knowledge.support.KnowledgeVectorSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库管理服务实现。
 */
@Service
public class KbKnowledgeBaseServiceImpl implements KbKnowledgeBaseService {

    private final KbKnowledgeBaseMapper knowledgeBaseMapper;
    private final KbDocumentMapper documentMapper;
    private final KbDocumentChunkMapper chunkMapper;
    private final KnowledgeVectorSupport vectorSupport;
    private final KnowledgeProperties properties;
    private final KbKnowledgeBaseMcpService mcpBindingService;

    public KbKnowledgeBaseServiceImpl(KbKnowledgeBaseMapper knowledgeBaseMapper,
                                        KbDocumentMapper documentMapper,
                                        KbDocumentChunkMapper chunkMapper,
                                        KnowledgeVectorSupport vectorSupport,
                                        KnowledgeProperties properties,
                                        KbKnowledgeBaseMcpService mcpBindingService) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.vectorSupport = vectorSupport;
        this.properties = properties;
        this.mcpBindingService = mcpBindingService;
    }

    @Override
    public PageInfo<KbKnowledgeBaseVo> page(KbKnowledgeBaseQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        LambdaQueryWrapper<KbKnowledgeBase> wrapper = Wrappers.<KbKnowledgeBase>lambdaQuery()
            .eq(KbKnowledgeBase::getDeleted, KnowledgeConstants.NOT_DELETED)
            .like(StrUtil.isNotBlank(query.getName()), KbKnowledgeBase::getName, query.getName())
            .eq(query.getStatus() != null, KbKnowledgeBase::getStatus, query.getStatus())
            .orderByDesc(KbKnowledgeBase::getCreateTime);

        Page<KbKnowledgeBase> mp = knowledgeBaseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<KbKnowledgeBaseVo> rows = new ArrayList<>(mp.getRecords().size());
        for (KbKnowledgeBase row : mp.getRecords()) {
            rows.add(BeanUtil.copyProperties(row, KbKnowledgeBaseVo.class));
        }
        Page<KbKnowledgeBaseVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public KbKnowledgeBase getById(Long kbId) {
        if (kbId == null) {
            return null;
        }
        KbKnowledgeBase row = knowledgeBaseMapper.selectById(kbId);
        if (row == null || KnowledgeConstants.DELETED == row.getDeleted()) {
            return null;
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(KbKnowledgeBaseBo req) {
        KbKnowledgeBase entity = BeanUtil.copyProperties(req, KbKnowledgeBase.class);
        entity.setDeleted(KnowledgeConstants.NOT_DELETED);
        if (entity.getStatus() == null) {
            entity.setStatus(KnowledgeConstants.KB_STATUS_NORMAL);
        }
        if (entity.getChunkSize() == null) {
            entity.setChunkSize(properties.getIngest().getDefaultChunkSize());
        }
        if (entity.getChunkOverlap() == null) {
            entity.setChunkOverlap(properties.getIngest().getDefaultChunkOverlap());
        }
        applySegmentDefaults(entity, req);
        knowledgeBaseMapper.insert(entity);
        mcpBindingService.saveBindings(entity.getKbId(), req.getMcpIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(KbKnowledgeBaseBo req) {
        KbKnowledgeBase old = getById(req.getKbId());
        if (old == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "知识库不存在或已删除");
        }
        KbKnowledgeBase entity = BeanUtil.copyProperties(req, KbKnowledgeBase.class);
        applySegmentDefaults(entity, req);
        knowledgeBaseMapper.updateById(entity);
        if (req.getMcpIds() != null) {
            mcpBindingService.saveBindings(req.getKbId(), req.getMcpIds());
        }
    }

    private void applySegmentDefaults(KbKnowledgeBase entity, KbKnowledgeBaseBo req) {
        if (StrUtil.isBlank(entity.getSegmentMode())) {
            entity.setSegmentMode(KbSegmentMode.AUTO);
        }
        if (StrUtil.isBlank(entity.getChunkDelimiter())) {
            entity.setChunkDelimiter(KbChunkDelimiter.DOUBLE_NEWLINE);
        }
        if (req.getPreprocessNormalizeWs() != null) {
            entity.setPreprocessNormalizeWs(req.getPreprocessNormalizeWs() ? 1 : 0);
        } else if (entity.getPreprocessNormalizeWs() == null) {
            entity.setPreprocessNormalizeWs(1);
        }
        if (req.getPreprocessRemoveUrl() != null) {
            entity.setPreprocessRemoveUrl(req.getPreprocessRemoveUrl() ? 1 : 0);
        } else if (entity.getPreprocessRemoveUrl() == null) {
            entity.setPreprocessRemoveUrl(0);
        }
        if (req.getPreprocessRemoveEmail() != null) {
            entity.setPreprocessRemoveEmail(req.getPreprocessRemoveEmail() ? 1 : 0);
        } else if (entity.getPreprocessRemoveEmail() == null) {
            entity.setPreprocessRemoveEmail(0);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> kbIds) {
        if (kbIds == null || kbIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "删除知识库ID不能为空");
        }
        for (Long kbId : kbIds) {
            KbKnowledgeBase kb = getById(kbId);
            if (kb == null) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "存在无效的知识库ID: " + kbId);
            }
        }

        for (Long kbId : kbIds) {
            vectorSupport.deleteByKbId(kbId);

            List<KbDocument> docs = documentMapper.selectList(
                Wrappers.<KbDocument>lambdaQuery()
                    .eq(KbDocument::getKbId, kbId)
                    .eq(KbDocument::getDeleted, KnowledgeConstants.NOT_DELETED)
            );
            for (KbDocument doc : docs) {
                vectorSupport.deleteByDocId(doc.getDocId());
                chunkMapper.delete(Wrappers.<KbDocumentChunk>lambdaQuery()
                    .eq(KbDocumentChunk::getDocId, doc.getDocId()));

                KbDocument docUpd = new KbDocument();
                docUpd.setDocId(doc.getDocId());
                docUpd.setDeleted(KnowledgeConstants.DELETED);
                documentMapper.updateById(docUpd);
            }

            mcpBindingService.removeByKbId(kbId);

            KbKnowledgeBase kbUpd = new KbKnowledgeBase();
            kbUpd.setKbId(kbId);
            kbUpd.setDeleted(KnowledgeConstants.DELETED);
            knowledgeBaseMapper.updateById(kbUpd);
        }
    }
}
