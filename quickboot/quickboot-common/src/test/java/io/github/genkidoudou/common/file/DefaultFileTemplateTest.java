package io.github.genkidoudou.common.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

/**
 * {@link DefaultFileTemplate} 本地存储与分类校验集成测试（不启动 Spring 容器）。
 */
class DefaultFileTemplateTest {

    @TempDir
    Path tempDir;

    @Test
    void upload_and_view_and_shortUrl_same() throws Exception {
        QcFileProperties props = new QcFileProperties();
        props.getLocal().setPath(tempDir.toString());
        props.setDomain("https://cdn.example.com");
        LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
        DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, List.of());

        MockMultipartFile mf = new MockMultipartFile("f", "a.PNG", "image/png", new byte[]{1, 2, 3});
        String path = tpl.upload(mf, "img");
        assertThat(path).startsWith("img/");
        assertThat(path).endsWith(".png");
        assertThat(tpl.exists(path)).isTrue();

        assertThat(tpl.view(path)).isEqualTo("https://cdn.example.com/" + path);
        assertThat(tpl.getShortUrl(path)).isEqualTo(tpl.view(path));

        Resource res = tpl.download(path);
        assertThat(res.getInputStream().readAllBytes()).hasSize(3);
        tpl.delete(path);
        assertThat(tpl.exists(path)).isFalse();
    }

    @Test
    void reject_bad_extension() {
        QcFileProperties props = new QcFileProperties();
        props.getLocal().setPath(tempDir.toString());
        LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
        DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, List.of());

        MockMultipartFile mf = new MockMultipartFile("f", "a.exe", "application/octet-stream", new byte[]{1});
        assertThatThrownBy(() -> tpl.upload(mf, "img")).isInstanceOf(FileStorageException.class);
    }

    @Test
    void hook_before_abort_triggers_onError() {
        QcFileProperties props = new QcFileProperties();
        props.getLocal().setPath(tempDir.toString());
        LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
        boolean[] onError = {false};
        FileUploadHook hook = new FileUploadHook() {
            @Override
            public void beforeUpload(FileUploadBeforeContext ctx) {
                throw new IllegalStateException("deny");
            }

            @Override
            public void onError(FileUploadErrorContext ctx) {
                onError[0] = true;
            }
        };
        DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, List.of(hook));
        MockMultipartFile mf = new MockMultipartFile("f", "a.png", "image/png", new byte[]{1});
        assertThatThrownBy(() -> tpl.upload(mf, "img")).isInstanceOf(IllegalStateException.class);
        assertThat(onError[0]).isTrue();
    }

    @Test
    void hook_after_abort_rollbacks_object_and_triggers_onError() {
        QcFileProperties props = new QcFileProperties();
        props.getLocal().setPath(tempDir.toString());
        LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
        boolean[] onError = {false};
        String[] relativePath = {null};
        FileUploadHook hook = new FileUploadHook() {
            @Override
            public void afterUpload(FileUploadAfterContext ctx) {
                relativePath[0] = ctx.getRelativePath();
                throw new IllegalStateException("after");
            }

            @Override
            public void onError(FileUploadErrorContext ctx) {
                onError[0] = true;
            }
        };
        DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, List.of(hook));
        MockMultipartFile mf = new MockMultipartFile("f", "a.png", "image/png", new byte[]{1});
        assertThatThrownBy(() -> tpl.upload(mf, "img")).isInstanceOf(IllegalStateException.class);
        assertThat(onError[0]).isTrue();
        assertThat(relativePath[0]).isNotBlank();
        assertThat(tpl.exists(relativePath[0])).isFalse();
    }
}
