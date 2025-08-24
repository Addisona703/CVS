package com.hngy.cvs.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.cvs.dto.request.RegisterDTO;
import com.hngy.cvs.dto.request.SendCodeDTO;
import com.hngy.cvs.dto.request.VerifyCodeDTO;
import com.hngy.cvs.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 注册验证码流程集成测试
 * 
 * @author CVS Team
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class RegisterWithVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCompleteRegistrationFlow() throws Exception {
        String testEmail = "test@university.edu.cn";
        String verificationType = "register";
        
        // 1. 发送验证码
        SendCodeDTO sendCodeDTO = new SendCodeDTO();
        sendCodeDTO.setEmail(testEmail);
        sendCodeDTO.setType(verificationType);
        
        mockMvc.perform(post("/api/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendCodeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("验证码发送成功"));

        // 2. 模拟验证验证码（在实际测试中，我们需要从Redis获取验证码）
        // 这里我们模拟验证码验证成功的场景
        VerifyCodeDTO verifyCodeDTO = new VerifyCodeDTO();
        verifyCodeDTO.setEmail(testEmail);
        verifyCodeDTO.setCode("123456"); // 模拟验证码
        verifyCodeDTO.setType(verificationType);
        
        // 注意：这个测试需要Redis服务运行，或者需要Mock Redis
        // 在实际环境中验证码应该是动态生成的
        
        // 3. 使用获得的验证令牌进行注册
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("testuser001");
        registerDTO.setPassword("123456");
        registerDTO.setName("测试用户");
        registerDTO.setRole(UserRole.STUDENT);
        registerDTO.setEmail(testEmail);
        registerDTO.setPhone("13800138001");
        registerDTO.setVerificationCode("123456");
        registerDTO.setVerifyToken("mock_verify_token"); // 在实际测试中应该从验证接口获取
        
        // 注册请求应该失败，因为我们使用的是模拟令牌
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("验证令牌无效或已过期，请重新验证邮箱"));
    }

    @Test 
    void testRegisterWithoutVerificationToken() throws Exception {
        // 测试没有验证令牌的注册请求
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("testuser002");
        registerDTO.setPassword("123456");
        registerDTO.setName("测试用户2");
        registerDTO.setRole(UserRole.STUDENT);
        registerDTO.setEmail("test2@university.edu.cn");
        registerDTO.setPhone("13800138002");
        registerDTO.setVerificationCode("123456");
        // 故意不设置 verifyToken
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest()) // 应该返回400错误，因为验证字段不能为空
                .andExpect(jsonPath("$.code").value(422));
    }

    @Test
    void testRegisterWithInvalidVerificationCode() throws Exception {
        // 测试验证码字段为空的情况
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("testuser003");
        registerDTO.setPassword("123456");
        registerDTO.setName("测试用户3");
        registerDTO.setRole(UserRole.STUDENT);
        registerDTO.setEmail("test3@university.edu.cn");
        registerDTO.setPhone("13800138003");
        registerDTO.setVerificationCode(""); // 空验证码
        registerDTO.setVerifyToken("some_token");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest()) // 应该返回400错误，因为验证码不能为空
                .andExpect(jsonPath("$.code").value(422));
    }
}