package com.hngy.cvs.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.result.ResultCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 认证入口点
 * 处理未认证的请求
 * 
 * @author CVS Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        log.warn("未认证的请求: {} {}", request.getMethod(), request.getRequestURI());
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED);
        String jsonResponse = objectMapper.writeValueAsString(result);
        
        response.getWriter().write(jsonResponse);
    }
}
