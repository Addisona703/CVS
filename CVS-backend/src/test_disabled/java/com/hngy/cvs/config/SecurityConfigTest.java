package com.hngy.cvs.config;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring Security 配置测试
 * 
 * @author CVS Team
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Test
    public void testErrorEndpointAccessibleWithoutAuthentication() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // 测试 GET /error 不需要认证
        mockMvc.perform(get("/error"))
                .andExpect(status().isOk());
        
        // 测试 POST /error 不需要认证
        mockMvc.perform(post("/error"))
                .andExpect(status().isOk());
    }

    @Test
    public void testHealthEndpointAccessibleWithoutAuthentication() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // 测试健康检查端点不需要认证
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    public void testProtectedEndpointRequiresAuthentication() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // 测试受保护的端点需要认证
        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAuthEndpointsAccessibleWithoutAuthentication() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // 测试认证端点不需要认证
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andExpect(status().isOk());
    }
}
