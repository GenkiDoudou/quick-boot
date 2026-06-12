package io.github.genkidoudou.web.knowledge.ingest.chunk;

import io.github.genkidoudou.web.knowledge.constants.KbChunkDelimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DelimiterTokenChunkSplitter} 行为用例。
 */
class DelimiterTokenChunkSplitterTest {

    private DelimiterTokenChunkSplitter splitter;

    @BeforeEach
    void setUp() {
        splitter = new DelimiterTokenChunkSplitter();
    }

    @Test
    void singleNewlineSplitsParagraphs() {
        String text = "para one\npara two\npara three";
        List<Document> chunks = splitter.split(
            List.of(new Document(text)),
            KbChunkDelimiter.SINGLE_NEWLINE,
            3,
            0);
        List<String> texts = texts(chunks);
        assertEquals(3, texts.size());
        assertEquals("para one", texts.get(0));
        assertEquals("para two", texts.get(1));
        assertEquals("para three", texts.get(2));
    }

    @Test
    void doubleNewlineSplitsOnBlankLines() {
        String text = "block a\n\nblock b\n\n\nblock c";
        List<Document> chunks = splitter.split(
            List.of(new Document(text)),
            KbChunkDelimiter.DOUBLE_NEWLINE,
            1,
            0);
        assertEquals(3, chunks.size());
    }

    @Test
    void mergesSmallParagraphsUpToTokenLimit() {
        String text = "short\n\nalso short\n\nthird";
        List<Document> chunks = splitter.split(
            List.of(new Document(text)),
            KbChunkDelimiter.DOUBLE_NEWLINE,
            50,
            0);
        assertEquals(1, chunks.size());
        assertTrue(chunks.get(0).getText().contains("short"));
        assertTrue(chunks.get(0).getText().contains("third"));
    }

    @Test
    void overlapPrependsPreviousTail() {
        String text = "alpha block\n\nbeta block";
        List<Document> chunks = splitter.split(
            List.of(new Document(text)),
            KbChunkDelimiter.DOUBLE_NEWLINE,
            3,
            4);
        assertEquals(2, chunks.size());
        String second = chunks.get(1).getText();
        assertTrue(second.length() > "beta block".length());
        assertTrue(second.contains("beta"));
        assertTrue(second.contains("block"));
    }

    @Test
    void estimateTokensUsesCharsDividedByFour() {
        assertEquals(0, DelimiterTokenChunkSplitter.estimateTokens(""));
        assertEquals(3, DelimiterTokenChunkSplitter.estimateTokens("abcdefghijkl"));
    }

    private static List<String> texts(List<Document> chunks) {
        return chunks.stream().map(Document::getText).collect(Collectors.toList());
    }
}
