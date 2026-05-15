package io.github.genkidoudou.common.logger;

import io.github.genkidoudou.common.logger.annotation.IgnoreLogger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LoggingAspect 日志拦截切面集成测试类
 *
 * @author genkidoudou
 */
@SpringBootTest(
    classes = {LoggingAspectTest.TestApplication.class},
    properties = {
        "qc.logger.print=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
    }
)
@AutoConfigureMockMvc
class LoggingAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLoggingForGetRequest() throws Exception {
        mockMvc.perform(get("/api/test/get"))
                .andExpect(status().isOk())
                .andExpect(content().string("GET success"));
    }

    @Test
    void testLoggingForPostRequest() throws Exception {
        mockMvc.perform(post("/api/test/post")
                .contentType("application/json")
                .content("{\"name\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("POST success"));
    }

    @Test
    void testLoggingForPutRequest() throws Exception {
        mockMvc.perform(put("/api/test/put")
                .contentType("application/json")
                .content("{\"id\":1,\"name\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("PUT success"));
    }

    @Test
    void testLoggingForDeleteRequest() throws Exception {
        mockMvc.perform(delete("/api/test/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("DELETE success"));
    }

    @Test
    void testIgnoreLoggerAll() throws Exception {
        mockMvc.perform(get("/api/test/ignore-all"))
                .andExpect(status().isOk())
                .andExpect(content().string("Ignored all"));
    }

    @Test
    void testIgnoreLoggerParams() throws Exception {
        mockMvc.perform(get("/api/test/ignore-params")
                .param("secret", "password123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Ignored params"));
    }

    @Test
    void testIgnoreLoggerResult() throws Exception {
        mockMvc.perform(get("/api/test/ignore-result"))
                .andExpect(status().isOk())
                .andExpect(content().string("Ignored result"));
    }

    @Test
    void testLoggingWithException() throws Exception {
        mockMvc.perform(get("/api/test/error"))
                .andExpect(status().isInternalServerError());
    }

    /**
     * 测试应用配置
     */
    @org.springframework.boot.autoconfigure.SpringBootApplication
    static class TestApplication {
        
        @RestController
        @RequestMapping("/api/test")
        static class TestController {
            
            @GetMapping("/get")
            public String getTest() {
                return "GET success";
            }
            
            @PostMapping("/post")
            public String postTest(@RequestBody TestDto dto) {
                return "POST success";
            }
            
            @PutMapping("/put")
            public String putTest(@RequestBody TestDto dto) {
                return "PUT success";
            }
            
            @DeleteMapping("/delete/{id}")
            public String deleteTest(@PathVariable Long id) {
                return "DELETE success";
            }
            
            @GetMapping("/ignore-all")
            @IgnoreLogger(type = IgnoreLogger.Type.ALL)
            public String ignoreAll() {
                return "Ignored all";
            }
            
            @GetMapping("/ignore-params")
            @IgnoreLogger(type = IgnoreLogger.Type.PARAMS)
            public String ignoreParams(@RequestParam String secret) {
                return "Ignored params";
            }
            
            @GetMapping("/ignore-result")
            @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
            public String ignoreResult() {
                return "Ignored result";
            }
            
            @GetMapping("/error")
            public String error() {
                throw new RuntimeException("Test error");
            }
        }
        
        static class TestDto {
            private Long id;
            private String name;
            
            public Long getId() {
                return id;
            }
            
            public void setId(Long id) {
                this.id = id;
            }
            
            public String getName() {
                return name;
            }
            
            public void setName(String name) {
                this.name = name;
            }
        }
    }
}
