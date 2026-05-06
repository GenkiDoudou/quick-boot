package io.github.genkidoudou.common.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * {@link FilePathSupport} 单元测试。
 */
class FilePathSupportTest {

    @Test
    void normalizeClassify_rejects_dotdot() {
        assertThatThrownBy(() -> FilePathSupport.normalizeClassifyKey("a/../b", "def"))
                .isInstanceOf(FileStorageException.class);
    }

    @Test
    void normalizeExtension_lowercase() {
        assertThat(FilePathSupport.normalizeExtension("FILE.PDF")).isEqualTo("pdf");
        assertThat(FilePathSupport.normalizeExtension("noext")).isEmpty();
    }

    @Test
    void validateRelativePath_rejects_leading_slash() {
        assertThatThrownBy(() -> FilePathSupport.validateRelativePath("/a/b"))
                .isInstanceOf(FileStorageException.class);
    }

    @Test
    void validateAgainstRule_default_whitelist_allows_png() {
        FilePathSupport.validateAgainstRule("png", 100, null);
    }
}
