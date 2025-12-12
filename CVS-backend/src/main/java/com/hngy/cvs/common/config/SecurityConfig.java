package com.hngy.cvs.common.config;

import com.hngy.cvs.common.security.JwtAccessDeniedHandler;
import com.hngy.cvs.common.security.JwtAuthenticationEntryPoint;
import com.hngy.cvs.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 配置类
 * 
 * @author CVS Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).formLogin().disable().logout().disable()
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 公开接口 - 无需认证即可访问
                .requestMatchers("/api/auth/**", "/", "/health", "/swagger-ui/**", "/v3/api-docs/**", "/error", "/actuator/health", "/actuator/info").permitAll()
                // 静态资源 - 上传的文件和图片允许匿名访问
                .requestMatchers("/uploads/**", "/images/**").permitAll()
                // 管理员接口
                .requestMatchers("/api/admin/**",  "/api/statistics/admin-dashboard").hasRole("ADMIN")
                // 教师接口
                .requestMatchers("/api/teacher/**", "/api/statistics/teacher-dashboard").hasAnyRole("TEACHER", "ADMIN")
                // 学生接口
                .requestMatchers("/api/student/**", "/api/statistics/student-dashboard").hasAnyRole("STUDENT", "ADMIN")
                // 其他接口需要认证
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
