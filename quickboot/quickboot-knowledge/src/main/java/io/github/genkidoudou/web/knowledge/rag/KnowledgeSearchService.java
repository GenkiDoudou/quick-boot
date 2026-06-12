package io.github.genkidoudou.web.knowledge.rag;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import io.github.genkidoudou.web.knowledge.constants.KbSearchMode;
import io.github.genkidoudou.web.knowledge.constants.KnowledgeConstants;
import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.domain.KbDocumentChunk;
import io.github.genkidoudou.web.knowledge.domain.KbRetrievalLog;
import io.github.genkidoudou.web.knowledge.dto.ChunkHitVo;
import io.github.genkidoudou.web.knowledge.dto.KbRetrievalLogQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbRetrievalLogVo;
import io.github.genkidoudou.web.knowledge.dto.KnowledgeSearchBo;
import io.github.genkidoudou.web.knowledge.mapper.KbDocumentChunkMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbDocumentMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbRetrievalLogMapper;
import io.github.genkidoudou.web.knowledge.support.KnowledgeAiGuard;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 知识库检索服务：支持纯向量与 Hybrid（向量 + 关键词）融合，并记录命中测试历史。
 */
@Service
public class KnowledgeSearchService {

    private static final Pattern TERM_SPLIT = Pattern.compile("[\\s\\p{Punct}]+");

    private final VectorStore vectorStore;
    private final KnowledgeProperties properties;
    private final KnowledgeAiGuard aiGuard;
    private final KbDocumentMapper documentMapper;
    private final KbDocumentChunkMapper chunkMapper;
    private final KbRetrievalLogMapper retrievalLogMapper;

    public KnowledgeSearchService(VectorStore vectorStore,
                                  KnowledgeProperties properties,
                                  KnowledgeAiGuard aiGuard,
                                  KbDocumentMapper documentMapper,
                                  KbDocumentChunkMapper chunkMapper,
                                  KbRetrievalLogMapper retrievalLogMapper) {
        this.vectorStore = vectorStore;
        this.properties = properties;
        this.aiGuard = aiGuard;
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.retrievalLogMapper = retrievalLogMapper;
    }

    /**
     * 在指定知识库内检索，默认 Hybrid 模式。
     */
    public List<ChunkHitVo> search(KnowledgeSearchBo req) {
        aiGuard.requireEmbeddingModel(req.getKbId());

        int topK = req.getTopK() != null ? req.getTopK() : properties.getRag().getTopK();
        double threshold = req.getSimilarityThreshold() != null
            ? req.getSimilarityThreshold()
            : properties.getRag().getSimilarityThreshold();
        String searchMode = resolveSearchMode(req.getSearchMode());

        List<ChunkHitVo> hits;
        if (KbSearchMode.VECTOR.equals(searchMode)) {
            hits = vectorSearch(req.getKbId(), req.getQuery(), topK, threshold, searchMode);
        } else {
            hits = hybridSearch(req.getKbId(), req.getQuery(), topK, threshold, searchMode);
        }

        if (shouldSaveHistory(req.getSaveHistory())) {
            saveRetrievalLog(req, searchMode, topK, threshold, hits.size());
        }
        return hits;
    }

    /**
     * 检索测试历史分页。
     */
    public PageInfo<KbRetrievalLogVo> pageRetrievalHistory(KbRetrievalLogQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : query.getPageSize();

        var mp = retrievalLogMapper.selectPage(
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize),
            Wrappers.<KbRetrievalLog>lambdaQuery()
                .eq(KbRetrievalLog::getKbId, query.getKbId())
                .orderByDesc(KbRetrievalLog::getCreateTime)
        );

        List<KbRetrievalLogVo> rows = new ArrayList<>(mp.getRecords().size());
        for (KbRetrievalLog row : mp.getRecords()) {
            KbRetrievalLogVo vo = new KbRetrievalLogVo();
            vo.setLogId(row.getLogId());
            vo.setKbId(row.getKbId());
            vo.setQuery(row.getQuery());
            vo.setSearchMode(row.getSearchMode());
            vo.setTopK(row.getTopK());
            vo.setSimilarityThreshold(row.getSimilarityThreshold());
            vo.setHitCount(row.getHitCount());
            vo.setCreateBy(row.getCreateBy());
            vo.setCreateTime(row.getCreateTime());
            rows.add(vo);
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<KbRetrievalLogVo> voPage =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    private List<ChunkHitVo> vectorSearch(Long kbId, String query, int topK, double threshold, String searchMode) {
        SearchRequest searchRequest = SearchRequest.builder()
            .query(query)
            .topK(topK)
            .similarityThreshold(threshold)
            .filterExpression("kbId == '" + kbId + "'")
            .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        Set<Long> disabledIds = loadDisabledChunkIds(kbId);
        List<ChunkHitVo> hits = new ArrayList<>();
        for (Document doc : documents) {
            ChunkHitVo vo = toHit(doc, searchMode);
            if (vo.getChunkId() != null && disabledIds.contains(vo.getChunkId())) {
                continue;
            }
            vo.setVectorScore(vo.getScore());
            vo.setKeywordScore(0.0);
            hits.add(vo);
        }
        return hits;
    }

    private List<ChunkHitVo> hybridSearch(Long kbId, String query, int topK, double threshold, String searchMode) {
        int recallK = Math.max(topK * 3, topK);
        double vectorRecallThreshold = Math.min(threshold, 0.3);

        SearchRequest searchRequest = SearchRequest.builder()
            .query(query)
            .topK(recallK)
            .similarityThreshold(vectorRecallThreshold)
            .filterExpression("kbId == '" + kbId + "'")
            .build();

        List<Document> vectorDocs = vectorStore.similaritySearch(searchRequest);
        Set<Long> disabledIds = loadDisabledChunkIds(kbId);

        Map<Long, ChunkHitVo> merged = new LinkedHashMap<>();
        Map<Long, Integer> vectorRank = new HashMap<>();
        int rank = 1;
        for (Document doc : vectorDocs) {
            ChunkHitVo hit = toHit(doc, searchMode);
            if (hit.getChunkId() == null || disabledIds.contains(hit.getChunkId())) {
                continue;
            }
            hit.setVectorScore(hit.getScore());
            merged.put(hit.getChunkId(), hit);
            vectorRank.put(hit.getChunkId(), rank++);
        }

        List<ChunkHitVo> keywordHits = keywordSearch(kbId, query, recallK, disabledIds);
        Map<Long, Integer> keywordRank = new HashMap<>();
        rank = 1;
        for (ChunkHitVo hit : keywordHits) {
            keywordRank.put(hit.getChunkId(), rank++);
            ChunkHitVo existing = merged.get(hit.getChunkId());
            if (existing == null) {
                hit.setSearchMode(searchMode);
                hit.setVectorScore(0.0);
                merged.put(hit.getChunkId(), hit);
            } else {
                existing.setKeywordScore(hit.getKeywordScore());
                if (StrUtil.isBlank(existing.getContent())) {
                    existing.setContent(hit.getContent());
                }
            }
        }

        KnowledgeProperties.Rag rag = properties.getRag();
        int rrfK = rag.getHybridRrfK();
        double vectorWeight = rag.getHybridVectorWeight();
        double keywordWeight = rag.getHybridKeywordWeight();

        List<ChunkHitVo> fused = new ArrayList<>(merged.values());
        for (ChunkHitVo hit : fused) {
            Long chunkId = hit.getChunkId();
            double vectorPart = vectorRank.containsKey(chunkId)
                ? vectorWeight / (rrfK + vectorRank.get(chunkId))
                : 0.0;
            double keywordPart = keywordRank.containsKey(chunkId)
                ? keywordWeight / (rrfK + keywordRank.get(chunkId))
                : 0.0;
            double fusedScore = vectorPart + keywordPart;
            hit.setScore(fusedScore);
            if (hit.getVectorScore() == null) {
                hit.setVectorScore(0.0);
            }
            if (hit.getKeywordScore() == null) {
                hit.setKeywordScore(keywordRank.containsKey(chunkId) ? keywordRank.get(chunkId) * 1.0 / recallK : 0.0);
            }
        }

        fused.sort(Comparator.comparing(ChunkHitVo::getScore, Comparator.nullsLast(Comparator.reverseOrder())));

        return fused.stream().limit(topK).collect(Collectors.toList());
    }

    private List<ChunkHitVo> keywordSearch(Long kbId, String query, int limit, Set<Long> disabledIds) {
        List<String> terms = extractTerms(query);
        if (terms.isEmpty()) {
            return List.of();
        }

        List<Long> docIds = documentMapper.selectList(
            Wrappers.<KbDocument>lambdaQuery()
                .eq(KbDocument::getKbId, kbId)
                .eq(KbDocument::getDeleted, KnowledgeConstants.NOT_DELETED)
                .select(KbDocument::getDocId)
        ).stream().map(KbDocument::getDocId).toList();
        if (docIds.isEmpty()) {
            return List.of();
        }

        List<KbDocumentChunk> chunks = chunkMapper.selectList(
            Wrappers.<KbDocumentChunk>lambdaQuery()
                .in(KbDocumentChunk::getDocId, docIds)
                .eq(KbDocumentChunk::getEnabled, 1)
        );

        Map<Long, String> docTitleCache = new HashMap<>();
        List<ScoredChunk> scored = new ArrayList<>();
        for (KbDocumentChunk chunk : chunks) {
            if (chunk.getChunkId() != null && disabledIds.contains(chunk.getChunkId())) {
                continue;
            }
            String text = resolveChunkText(chunk);
            if (StrUtil.isBlank(text)) {
                continue;
            }
            String lower = text.toLowerCase(Locale.ROOT);
            int matched = 0;
            for (String term : terms) {
                if (lower.contains(term)) {
                    matched++;
                }
            }
            if (matched == 0) {
                continue;
            }
            double score = (double) matched / terms.size();
            scored.add(new ScoredChunk(chunk, score, text));
        }

        scored.sort(Comparator.comparing(ScoredChunk::score).reversed());
        List<ChunkHitVo> hits = new ArrayList<>(Math.min(limit, scored.size()));
        for (int i = 0; i < Math.min(limit, scored.size()); i++) {
            ScoredChunk sc = scored.get(i);
            ChunkHitVo vo = new ChunkHitVo();
            vo.setChunkId(sc.chunk().getChunkId());
            vo.setDocId(sc.chunk().getDocId());
            vo.setContent(sc.text());
            vo.setKeywordScore(sc.score());
            vo.setScore(sc.score());
            vo.setPageNumber(sc.chunk().getPageNumber());
            vo.setFileName(resolveFileName(sc.chunk().getDocId(), docTitleCache));
            hits.add(vo);
        }
        return hits;
    }

    private String resolveFileName(Long docId, Map<Long, String> cache) {
        return cache.computeIfAbsent(docId, id -> {
            KbDocument doc = documentMapper.selectById(id);
            return doc != null ? StrUtil.blankToDefault(doc.getTitle(), "document") : "document";
        });
    }

    private static String resolveChunkText(KbDocumentChunk chunk) {
        if (StrUtil.isNotBlank(chunk.getContentFull())) {
            return chunk.getContentFull();
        }
        return chunk.getContentPreview();
    }

    private Set<Long> loadDisabledChunkIds(Long kbId) {
        List<Long> docIds = documentMapper.selectList(
            Wrappers.<KbDocument>lambdaQuery()
                .eq(KbDocument::getKbId, kbId)
                .eq(KbDocument::getDeleted, KnowledgeConstants.NOT_DELETED)
                .select(KbDocument::getDocId)
        ).stream().map(KbDocument::getDocId).toList();
        if (docIds.isEmpty()) {
            return Set.of();
        }
        return chunkMapper.selectList(
            Wrappers.<KbDocumentChunk>lambdaQuery()
                .in(KbDocumentChunk::getDocId, docIds)
                .eq(KbDocumentChunk::getEnabled, 0)
                .select(KbDocumentChunk::getChunkId)
        ).stream().map(KbDocumentChunk::getChunkId).collect(Collectors.toSet());
    }

    private void saveRetrievalLog(KnowledgeSearchBo req, String searchMode, int topK, double threshold, int hitCount) {
        KbRetrievalLog log = new KbRetrievalLog();
        log.setKbId(req.getKbId());
        log.setQuery(StrUtil.sub(req.getQuery(), 0, 2000));
        log.setSearchMode(searchMode);
        log.setTopK(topK);
        log.setSimilarityThreshold(threshold);
        log.setHitCount(hitCount);
        log.setCreateBy(currentUserLabel());
        retrievalLogMapper.insert(log);
    }

    private static String currentUserLabel() {
        try {
            if (StpUtil.isLogin()) {
                return String.valueOf(StpUtil.getLoginId());
            }
        } catch (Exception ignored) {
            // 非 Web 上下文
        }
        return "";
    }

    private static boolean shouldSaveHistory(Boolean saveHistory) {
        return saveHistory == null || Boolean.TRUE.equals(saveHistory);
    }

    private String resolveSearchMode(String mode) {
        if (StrUtil.isBlank(mode)) {
            return StrUtil.blankToDefault(properties.getRag().getDefaultSearchMode(), KbSearchMode.HYBRID);
        }
        if (KbSearchMode.VECTOR.equals(mode) || KbSearchMode.HYBRID.equals(mode)) {
            return mode;
        }
        return KbSearchMode.HYBRID;
    }

    private static List<String> extractTerms(String query) {
        if (StrUtil.isBlank(query)) {
            return List.of();
        }
        String[] parts = TERM_SPLIT.split(query.trim().toLowerCase(Locale.ROOT));
        Set<String> terms = new HashSet<>();
        for (String part : parts) {
            if (part.length() >= 2) {
                terms.add(part);
            }
        }
        if (terms.isEmpty() && query.trim().length() >= 1) {
            terms.add(query.trim().toLowerCase(Locale.ROOT));
        }
        return new ArrayList<>(terms);
    }

    private ChunkHitVo toHit(Document doc, String searchMode) {
        Map<String, Object> metadata = doc.getMetadata();
        ChunkHitVo vo = new ChunkHitVo();
        vo.setContent(doc.getText());
        vo.setScore(extractScore(metadata));
        vo.setDocId(parseLong(metadata.get("docId")));
        vo.setChunkId(parseLong(metadata.get("chunkId")));
        vo.setFileName(asString(metadata.get("fileName")));
        vo.setPageNumber(parseInteger(metadata.get("pageNumber")));
        vo.setSearchMode(searchMode);
        return vo;
    }

    private static Double extractScore(Map<String, Object> metadata) {
        if (metadata == null) {
            return null;
        }
        Object distance = metadata.get("distance");
        if (distance instanceof Number number) {
            return 1.0 - number.doubleValue();
        }
        Object score = metadata.get("score");
        if (score instanceof Number number) {
            return number.doubleValue();
        }
        return null;
    }

    private static Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str && StrUtil.isNotBlank(str)) {
            try {
                return Long.parseLong(str.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str && StrUtil.isNotBlank(str)) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private record ScoredChunk(KbDocumentChunk chunk, double score, String text) {
    }
}
