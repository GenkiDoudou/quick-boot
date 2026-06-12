package io.github.genkidoudou.web.knowledge.ingest.preprocess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TextPreprocessor} 行为用例。
 */
class TextPreprocessorTest {

    private TextPreprocessor preprocessor;

    @BeforeEach
    void setUp() {
        preprocessor = new TextPreprocessor();
    }

    @Test
    void normalizeWhitespaceCollapsesRuns() {
        String raw = "hello   world\n\nfoo\tbar";
        String result = preprocessor.preprocessText(raw, true, false, false);
        assertEquals("hello world foo bar", result);
    }

    @Test
    void removeUrlStripsHttpAndWww() {
        String raw = "visit https://example.com/path and www.demo.org now";
        String result = preprocessor.preprocessText(raw, false, true, false);
        assertFalse(result.contains("https://"));
        assertFalse(result.contains("www.demo"));
        assertTrue(result.contains("visit"));
        assertTrue(result.contains("now"));
    }

    @Test
    void removeEmailStripsAddresses() {
        String raw = "contact me at user@example.com thanks";
        String result = preprocessor.preprocessText(raw, false, false, true);
        assertFalse(result.contains("@"));
        assertTrue(result.contains("contact"));
        assertTrue(result.contains("thanks"));
    }

    @Test
    void preprocessDocumentsSkipsEmptyAfterCleaning() {
        String onlyUrl = "https://only-url.test";
        List<Document> result = preprocessor.preprocess(
            List.of(new Document(onlyUrl)),
            false,
            true,
            false);
        assertTrue(result.isEmpty());
    }

    @Test
    void allFlagsAppliedInOrder() {
        String raw = "  hi   https://x.com  user@test.com  ";
        String result = preprocessor.preprocessText(raw, true, true, true);
        assertEquals("hi", result);
    }
}
