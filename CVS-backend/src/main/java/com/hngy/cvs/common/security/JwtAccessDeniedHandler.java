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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 访问拒绝处理器
 * 处理权限不足的请求
 * 
 * @author CVS Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.warn("权限不足的请求: {} {}", request.getMethod(), request.getRequestURI());
        
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        Result<Void> result = Result.error(ResultCode.FORBIDDEN);
        String jsonResponse = objectMapper.writeValueAsString(result);
        
        response.getWriter().write(jsonResponse);
    }
}
