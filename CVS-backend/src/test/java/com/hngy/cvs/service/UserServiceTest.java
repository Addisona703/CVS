package com.hngy.cvs.service;

import com.hngy.cvs.dto.request.RegisterDTO;
import com.hngy.cvs.dto.response.UserVO;
import com.hngy.cvs.entity.enums.UserRole;
import com.hngy.cvs.mapper.UserMapper;
import com.hngy.cvs.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试类
 * 
 * @author CVS Team
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterDTO registerDTO;

    @BeforeEach
    void setUp() {
        registerDTO = new RegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setPassword("123456");
        registerDTO.setName("测试用户");
        registerDTO.setRole(UserRole.STUDENT);
        registerDTO.setEmail("test@example.com");
        registerDTO.setPhone("13800138000");
    }

    @Test
    void testRegister_Success() {
        // Given
//        when(userMapper.selectByUsername(anyString())).thenReturn(null);
//        when(userMapper.selectByEmail(anyString())).thenReturn(null);
//        when(userMapper.selectByPhone(anyString())).thenReturn(null);
//        when(userMapper.insert(any())).thenReturn(1);

        // When
        UserVO result = userService.register(registerDTO);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getName());
        assertEquals(UserRole.STUDENT, result.getRole());
        
        verify(userMapper, times(1)).insert(any());
    }

    @Test
    void testExistsByUsername() {
        // Given
//        when(userMapper.selectByUsername("testuser")).thenReturn(null);

        // When
        boolean exists = userService.existsByUsername("testuser");

        // Then
        assertFalse(exists);
//        verify(userMapper, times(1)).selectByUsername("testuser");
    }
}
