package io.github.genkidoudou.common.security.firewall.sensitiveword;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveWordListLoaderTest {

    @Test
    void skips_comment_and_blank() throws Exception {
        DefaultResourceLoader rl = new DefaultResourceLoader();
        List<String> lines = SensitiveWordListLoader.loadAll(rl, List.of("classpath:sensitive-test-black.txt"));
        assertThat(lines).containsExactly("badword", "evil");
    }
}
