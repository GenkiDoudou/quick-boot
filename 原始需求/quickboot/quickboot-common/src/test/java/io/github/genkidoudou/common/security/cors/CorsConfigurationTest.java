package io.github.genkidoudou.common.security.cors;

import io.github.genkidoudou.common.firewall.cors.CorsConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CorsConfiguration 跨域配置集成测试类
 *
 * @author genkidoudou
 */
@SpringBootTest(
    classes = {CorsConfigurationTest.TestApplication.class, CorsConfiguration.class},
    properties = {
        "qc.security.cors.enabled=true",
        "qc.security.cors.allowed-origins=http://localhost:3000,http://localhost:8080",
        "qc.security.cors.allowed-methods=GET,POST,PUT,DELETE",
        "qc.security.cors.allowed-headers=*",
        "qc.security.cors.allow-credentials=true",
        "qc.security.cors.max-age=3600",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
    }
)
@AutoConfigureMockMvc
class CorsConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCorsPreflightRequest() throws Exception {
        mockMvc.perform(options("/api/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Max-Age"));
    }

    @Test
    void testCorsActualRequestWithAllowedOrigin() throws Exception {
        mockMvc.perform(get("/api/test")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void testCorsActualRequestWithAnotherAllowedOrigin() throws Exception {
        mockMvc.perform(get("/api/test")
                .header("Origin", "http://localhost:8080"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8080"));
    }

    @Test
    void testCorsPostRequest() throws Exception {
        mockMvc.perform(post("/api/test")
                .header("Origin", "http://localhost:3000")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void testCorsPutRequest() throws Exception {
        mockMvc.perform(put("/api/test")
                .header("Origin", "http://localhost:3000")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void testCorsDeleteRequest() throws Exception {
        mockMvc.perform(delete("/api/test")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void testCorsWithCredentials() throws Exception {
        mockMvc.perform(get("/api/test")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Credentials"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    /**
     * 测试应用配置
     */
    @org.springframework.boot.autoconfigure.SpringBootApplication
    static class TestApplication {
        
        @RestController
        static class TestController {
            
            @GetMapping("/api/test")
            public String getTest() {
                return "GET success";
            }
            
            @PostMapping("/api/test")
            public String postTest() {
                return "POST success";
            }
            
            @org.springframework.web.bind.annotation.PutMapping("/api/test")
            public String putTest() {
                return "PUT success";
            }
            
            @org.springframework.web.bind.annotation.DeleteMapping("/api/test")
            public String deleteTest() {
                return "DELETE success";
            }
        }
    }
}
