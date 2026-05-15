package io.github.genkidoudou.common.security.xss;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * XssFilter XSS过滤器集成测试类
 *
 * @author genkidoudou
 */
@SpringBootTest(
    classes = {XssFilterTest.TestApplication.class},
    properties = {
        "qc.security.firewall.xss.enabled=true",
        "qc.security.firewall.xss.ignore-urls=/api/public/**",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
    }
)
@AutoConfigureMockMvc
class XssFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testNormalRequest() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("content", "这是正常内容"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }

    @Test
    void testXssWithScriptTag() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("content", "<script>alert('xss')</script>"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("请求参数包含非法字符"));
    }

    @Test
    void testXssWithJavascriptProtocol() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("url", "javascript:alert(1)"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testXssWithEventHandler() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("html", "<img src=x onerror=alert(1)>"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testXssInJsonBody() throws Exception {
        mockMvc.perform(post("/api/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"<script>alert('xss')</script>\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNormalJsonRequest() throws Exception {
        mockMvc.perform(post("/api/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"正常内容\",\"title\":\"标题\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testXssWithIframe() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("html", "<iframe src='http://evil.com'></iframe>"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testIgnoredUrl() throws Exception {
        mockMvc.perform(get("/api/public/test")
                .param("content", "<script>alert('xss')</script>"))
                .andExpect(status().isOk())
                .andExpect(content().string("public success"));
    }

    @Test
    void testXssWithOnload() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("html", "<body onload=alert(1)>"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testXssWithEval() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("code", "eval('alert(1)')"))
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试应用配置
     */
    @org.springframework.boot.autoconfigure.SpringBootApplication
    static class TestApplication {
        
        @RestController
        static class TestController {
            
            @GetMapping("/api/test")
            public String getTest(@RequestParam(required = false) String content,
                                 @RequestParam(required = false) String url,
                                 @RequestParam(required = false) String html,
                                 @RequestParam(required = false) String code) {
                return "success";
            }
            
            @PostMapping("/api/test")
            public String postTest(@RequestBody TestDto dto) {
                return "success";
            }
            
            @GetMapping("/api/public/test")
            public String publicTest(@RequestParam(required = false) String content) {
                return "public success";
            }
        }
        
        static class TestDto {
            private String content;
            private String title;
            
            public String getContent() {
                return content;
            }
            
            public void setContent(String content) {
                this.content = content;
            }
            
            public String getTitle() {
                return title;
            }
            
            public void setTitle(String title) {
                this.title = title;
            }
        }
    }
}
