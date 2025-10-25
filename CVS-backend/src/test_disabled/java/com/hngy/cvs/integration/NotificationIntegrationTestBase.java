package com.hngy.cvs.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.cvs.entity.User;
import com.hngy.cvs.service.UserService;
import com.hngy.cvs.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 通知系统集成测试基类
 * 提供通用的测试环境配置和工具方法
 */
@SpringBootTest
@AutoConfigureTestMockMvc
@ActiveProfiles("integration")
@Sql(scripts = {"/schema-h2.sql", "/data-h2.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
public abstract class NotificationIntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserService userService;

    @Autowired
    protected JwtUtil jwtUtil;

    // 测试用户信息
    protected static final Long TEACHER1_ID = 1L;
    protected static final Long TEACHER2_ID = 2L;
    protected static final Long STUDENT1_ID = 3L;
    protected static final Long STUDENT2_ID = 4L;
    protected static final Long STUDENT3_ID = 5L;

    protected static final String TEACHER1_USERNAME = "teacher1";
    protected static final String TEACHER2_USERNAME = "teacher2";
    protected static final String STUDENT1_USERNAME = "student1";
    protected static final String STUDENT2_USERNAME = "student2";
    protected static final String STUDENT3_USERNAME = "student3";

    // JWT Token缓存
    protected String teacher1Token;
    protected String teacher2Token;
    protected String student1Token;
    protected String student2Token;
    protected String student3Token;

    @BeforeEach
    void setUp() {
        // 生成测试用的JWT Token
        teacher1Token = generateTokenForUser(TEACHER1_ID, TEACHER1_USERNAME);
        teacher2Token = generateTokenForUser(TEACHER2_ID, TEACHER2_USERNAME);
        student1Token = generateTokenForUser(STUDENT1_ID, STUDENT1_USERNAME);
        student2Token = generateTokenForUser(STUDENT2_ID, STUDENT2_USERNAME);
        student3Token = generateTokenForUser(STUDENT3_ID, STUDENT3_USERNAME);
    }

    /**
     * 为指定用户生成JWT Token
     */
    protected String generateTokenForUser(Long userId, String username) {
        return jwtUtil.generateToken(userId, username);
    }

    /**
     * 创建带认证头的GET请求
     */
    protected MockHttpServletRequestBuilder authenticatedGet(String url, String token) {
        return get(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 创建带认证头的POST请求
     */
    protected MockHttpServletRequestBuilder authenticatedPost(String url, String token) {
        return post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 创建带认证头的PUT请求
     */
    protected MockHttpServletRequestBuilder authenticatedPut(String url, String token) {
        return put(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 创建带认证头的DELETE请求
     */
    protected MockHttpServletRequestBuilder authenticatedDelete(String url, String token) {
        return delete(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 将对象转换为JSON字符串
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * 等待指定毫秒数（用于测试定时任务等异步操作）
     */
    protected void waitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}