package io.github.genkidoudou.common.security.idempotent;

import io.github.genkidoudou.common.firewall.idempotent.Idempotent;
import io.github.genkidoudou.common.firewall.idempotent.KeyGenerateStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * IdempotentAspect 幂等切面集成测试类
 *
 * @author genkidoudou
 */
@SpringBootTest(
    classes = {IdempotentAspectTest.TestApplication.class},
    properties = {
        "qc.security.idempotent.enabled=true",
        "qc.security.idempotent.expire-time=5",
        "qc.security.idempotent.key-prefix=test:idempotent",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
    }
)
@AutoConfigureMockMvc
class IdempotentAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testIdempotentWithDefaultStrategy() throws Exception {
        // 第一次请求应该成功
        mockMvc.perform(post("/api/test/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("created"));

        // 立即重复请求应该被拦截
        mockMvc.perform(post("/api/test/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("重复请求,请稍后再试"));
    }

    @Test
    void testIdempotentWithDifferentParams() throws Exception {
        // 不同参数的请求应该都能成功
        mockMvc.perform(post("/api/test/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test1\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/test/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test2\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testIdempotentWithDeleteAfterExecution() throws Exception {
        // 第一次请求
        mockMvc.perform(post("/api/test/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"name\":\"test\"}"))
                .andExpect(status().isOk());

        // 由于设置了 deleteAfterExecution=true，第二次请求应该也能成功
        mockMvc.perform(post("/api/test/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"name\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testIdempotentWithCustomExpireTime() throws Exception {
        // 第一次请求
        mockMvc.perform(post("/api/test/short-expire")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\":\"test\"}"))
                .andExpect(status().isOk());

        // 立即重复请求应该被拦截
        mockMvc.perform(post("/api/test/short-expire")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\":\"test\"}"))
                .andExpect(status().isBadRequest());

        // 等待过期时间后再次请求应该成功
        Thread.sleep(2000);
        mockMvc.perform(post("/api/test/short-expire")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testIdempotentWithCustomPrefix() throws Exception {
        mockMvc.perform(post("/api/test/custom-prefix")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\":\"test\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/test/custom-prefix")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\":\"test\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNonIdempotentEndpoint() throws Exception {
        // 没有幂等注解的接口可以重复调用
        mockMvc.perform(get("/api/test/query"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/test/query"))
                .andExpect(status().isOk());
    }

    /**
     * 测试应用配置
     */
    @org.springframework.boot.autoconfigure.SpringBootApplication
    static class TestApplication {
        
        @RestController
        @RequestMapping("/api/test")
        static class TestController {
            
            @PostMapping("/create")
            @Idempotent(strategy = KeyGenerateStrategy.DEFAULT, expireTime = 5)
            public String create(@RequestBody TestDto dto) {
                return "created";
            }
            
            @PostMapping("/update")
            @Idempotent(strategy = KeyGenerateStrategy.DEFAULT, deleteAfterExecution = true)
            public String update(@RequestBody TestDto dto) {
                return "updated";
            }
            
            @PostMapping("/short-expire")
            @Idempotent(strategy = KeyGenerateStrategy.DEFAULT, expireTime = 1, timeUnit = TimeUnit.SECONDS)
            public String shortExpire(@RequestBody TestDto dto) {
                return "success";
            }
            
            @PostMapping("/custom-prefix")
            @Idempotent(prefix = "custom", strategy = KeyGenerateStrategy.DEFAULT)
            public String customPrefix(@RequestBody TestDto dto) {
                return "success";
            }
            
            @GetMapping("/query")
            public String query() {
                return "query result";
            }
        }
        
        static class TestDto {
            private Long id;
            private String name;
            private String data;
            private String value;
            
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
            
            public String getData() {
                return data;
            }
            
            public void setData(String data) {
                this.data = data;
            }
            
            public String getValue() {
                return value;
            }
            
            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}
