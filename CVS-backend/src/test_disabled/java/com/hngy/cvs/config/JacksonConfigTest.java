package com.hngy.cvs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.result.ResultCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jackson 配置测试
 * 
 * @author CVS Team
 */
@SpringBootTest
@ActiveProfiles("test")
public class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testLocalDateTimeSerializationInResult() throws Exception {
        // 创建包含 LocalDateTime 的 Result 对象
        Result<String> result = Result.success("测试数据");
        
        // 序列化为 JSON
        String json = objectMapper.writeValueAsString(result);
        
        // 验证 JSON 包含格式化的时间戳
        assertNotNull(json);
        assertTrue(json.contains("timestamp"));
        assertTrue(json.contains("-")); // 日期分隔符
        assertTrue(json.contains(":")); // 时间分隔符
        
        System.out.println("序列化结果: " + json);
    }

    @Test
    public void testLocalDateTimeDeserialization() throws Exception {
        // 创建包含格式化时间的 JSON
        String json = """
            {
                "code": 200,
                "message": "成功",
                "data": "测试数据",
                "timestamp": "2024-01-01 10:00:00"
            }
            """;
        
        // 反序列化为 Result 对象
        Result<?> result = objectMapper.readValue(json, Result.class);
        
        // 验证反序列化成功
        assertNotNull(result);
        assertNotNull(result.getTimestamp());
        assertEquals(200, result.getCode());
        assertEquals("成功", result.getMessage());
        
        System.out.println("反序列化结果: " + result);
    }

    @Test
    public void testErrorResultSerialization() throws Exception {
        // 创建错误结果
        Result<Void> errorResult = Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        
        // 序列化为 JSON
        String json = objectMapper.writeValueAsString(errorResult);
        
        // 验证序列化成功
        assertNotNull(json);
        assertTrue(json.contains("timestamp"));
        assertTrue(json.contains("500")); // 错误码
        
        System.out.println("错误结果序列化: " + json);
    }

    @Test
    public void testStandaloneLocalDateTimeSerialization() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        
        // 序列化 LocalDateTime
        String json = objectMapper.writeValueAsString(now);
        
        // 验证格式
        assertNotNull(json);
        assertTrue(json.contains("-"));
        assertTrue(json.contains(":"));
        assertTrue(json.startsWith("\""));
        assertTrue(json.endsWith("\""));
        
        System.out.println("LocalDateTime 序列化: " + json);
        
        // 反序列化
        LocalDateTime deserialized = objectMapper.readValue(json, LocalDateTime.class);
        assertEquals(now.withNano(0), deserialized.withNano(0)); // 忽略纳秒差异
    }
}
