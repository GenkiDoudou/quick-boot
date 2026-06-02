package io.github.genkidoudou.common.security.firewall.sqlinjection;

import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CachedBodyHttpServletRequestWrapper} 在防火墙预读 body 后仍须能解析 multipart。
 */
class CachedBodyHttpServletRequestWrapperTest {

    @Test
    void getParts_parsesFileFieldFromCachedBody() throws Exception {
        byte[] body = (
                "------Bb\r\n"
                        + "Content-Disposition: form-data; name=\"file\"; filename=\"a.txt\"\r\n"
                        + "Content-Type: text/plain\r\n"
                        + "\r\n"
                        + "hello\r\n"
                        + "------Bb--\r\n"
        ).getBytes(StandardCharsets.UTF_8);

        MockHttpServletRequest raw = new MockHttpServletRequest("POST", "/system/file/upload");
        raw.setContentType("multipart/form-data; boundary=----Bb");
        byte[] consumed = StreamUtils.copyToByteArray(raw.getInputStream());
        raw.setContent(consumed);

        CachedBodyHttpServletRequestWrapper wrapped = new CachedBodyHttpServletRequestWrapper(raw, body);
        Collection<Part> parts = wrapped.getParts();

        assertThat(parts).hasSize(1);
        Part file = parts.iterator().next();
        assertThat(file.getName()).isEqualTo("file");
        assertThat(file.getSubmittedFileName()).isEqualTo("a.txt");
        assertThat(new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("hello");
    }
}
