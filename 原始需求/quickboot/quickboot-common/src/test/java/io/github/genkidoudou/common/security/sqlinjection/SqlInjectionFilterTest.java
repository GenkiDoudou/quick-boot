package io.github.genkidoudou.common.security.sqlinjection;

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
 * SqlInjectionFilter SQL注入过滤器集成测试类
 *
 * @author genkidoudou
 */
@SpringBootTest(
    classes = {SqlInjectionFilterTest.TestApplication.class},
    properties = {
        "qc.security.firewall.sql-injection.enabled=true",
        "qc.security.firewall.sql-injection.ignore-urls=/api/public/**",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
    }
)
@AutoConfigureMockMvc
class SqlInjectionFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testNormalRequest() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("name", "张三"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }

    @Test
    void testSqlInjectionInGetParam() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("name", "admin' or '1'='1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("请求参数包含非法字符"));
    }

    @Test
    void testSqlInjectionWithSelectKeyword() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("query", "select * from users"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSqlInjectionWithUnionKeyword() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("id", "1 union select password from admin"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSqlInjectionInJsonBody() throws Exception {
        mockMvc.perform(post("/api/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"admin' or '1'='1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNormalJsonRequest() throws Exception {
        mockMvc.perform(post("/api/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"张三\",\"age\":25}"))
                .andExpect(status().isOk());
    }

    @Test
    void testSqlInjectionWithDropKeyword() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("sql", "drop table users"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testIgnoredUrl() throws Exception {
        mockMvc.perform(get("/api/public/test")
                .param("name", "select * from users"))
                .andExpect(status().isOk())
                .andExpect(content().string("public success"));
    }

    @Test
    void testSqlInjectionWithComment() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("name", "admin'--"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSqlInjectionWithSemicolon() throws Exception {
        mockMvc.perform(get("/api/test")
                .param("cmd", "delete from users; drop table admin;"))
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
            public String getTest(@RequestParam(required = false) String name,
                                 @RequestParam(required = false) String query,
                                 @RequestParam(required = false) String id,
                                 @RequestParam(required = false) String sql,
                                 @RequestParam(required = false) String cmd) {
                return "success";
            }
            
            @PostMapping("/api/test")
            public String postTest(@RequestBody TestDto dto) {
                return "success";
            }
            
            @GetMapping("/api/public/test")
            public String publicTest(@RequestParam(required = false) String name) {
                return "public success";
            }
        }
        
        static class TestDto {
            private String name;
            private Integer age;
            
            public String getName() {
                return name;
            }
            
            public void setName(String name) {
                this.name = name;
            }
            
            public Integer getAge() {
                return age;
            }
            
            public void setAge(Integer age) {
                this.age = age;
            }
        }
    }
}
