package io.github.genkidoudou.web.knowledge.ingest;

import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import io.github.genkidoudou.web.knowledge.constants.KbChunkDelimiter;
import io.github.genkidoudou.web.knowledge.constants.KbSegmentMode;
import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证重建索引时优先使用文档快照字段（任务 3.7）。
 */
class IngestSegmentConfigTest {

    @Test
    void usesDocumentSnapshotOverKnowledgeBaseDefaults() {
        KbDocument doc = new KbDocument();
        doc.setSegmentMode(KbSegmentMode.CUSTOM);
        doc.setChunkSize(600);
        doc.setChunkOverlap(80);
        doc.setChunkDelimiter(KbChunkDelimiter.SINGLE_NEWLINE);
        doc.setPreprocessNormalizeWs(0);
        doc.setPreprocessRemoveUrl(1);
        doc.setPreprocessRemoveEmail(1);

        KbKnowledgeBase kb = new KbKnowledgeBase();
        kb.setChunkSize(800);
        kb.setChunkOverlap(120);

        KnowledgeProperties props = new KnowledgeProperties();
        IngestSegmentConfig config = IngestSegmentConfig.fromDocumentSnapshot(doc, kb, props);

        assertEquals(KbSegmentMode.CUSTOM, config.getSegmentMode());
        assertEquals(600, config.getChunkSize());
        assertEquals(80, config.getChunkOverlap());
        assertEquals(KbChunkDelimiter.SINGLE_NEWLINE, config.getChunkDelimiter());
        assertFalse(config.isPreprocessNormalizeWs());
        assertTrue(config.isPreprocessRemoveUrl());
        assertTrue(config.isPreprocessRemoveEmail());
    }

    @Test
    void fallsBackWhenSnapshotFieldsMissing() {
        KbDocument doc = new KbDocument();
        KbKnowledgeBase kb = new KbKnowledgeBase();
        kb.setChunkSize(900);
        kb.setChunkOverlap(100);

        KnowledgeProperties props = new KnowledgeProperties();
        props.getIngest().setDefaultChunkSize(800);
        props.getIngest().setDefaultChunkOverlap(120);

        IngestSegmentConfig config = IngestSegmentConfig.fromDocumentSnapshot(doc, kb, props);

        assertEquals(KbSegmentMode.AUTO, config.getSegmentMode());
        assertEquals(900, config.getChunkSize());
        assertEquals(100, config.getChunkOverlap());
        assertEquals(KbChunkDelimiter.DOUBLE_NEWLINE, config.getChunkDelimiter());
        assertTrue(config.isPreprocessNormalizeWs());
    }
}
