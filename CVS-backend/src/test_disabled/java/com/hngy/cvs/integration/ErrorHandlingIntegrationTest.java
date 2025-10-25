package com.hngy.cvs.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 错误处理集成测试
 * 验证错误端点能正确处理LocalDateTime序列化
 * 
 * @author CVS Team
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public class ErrorHandlingIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Test
    public void testErrorEndpointWithoutAuthentication() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // 测试 GET /error 不需要认证且能正确返回JSON
        mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    public void testErrorEndpointPostWithoutAuthentication() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // 测试 POST /error 不需要认证且能正确返回JSON
        mockMvc.perform(post("/error"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    public void testUnauthorizedEndpointReturnsValidJson() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // 测试受保护的端点返回正确的JSON错误响应（包含timestamp）
        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    public void testNotFoundEndpointReturnsValidJson() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // 测试不存在的端点返回正确的JSON错误响应
        mockMvc.perform(get("/api/nonexistent"))
                .andExpect(status().isUnauthorized()) // 因为需要认证
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    public void testPublicEndpointAccessible() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // 测试公开端点可以正常访问
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
