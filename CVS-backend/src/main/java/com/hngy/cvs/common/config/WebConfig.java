package com.hngy.cvs.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 *
 * @author CVS Team
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取绝对路径并规范化
        java.io.File uploadDir = new java.io.File(uploadPath);
        String absolutePath = uploadDir.getAbsolutePath();
        
        // 规范化路径，移除 .\ 或 ./
        try {
            absolutePath = uploadDir.getCanonicalPath();
        } catch (Exception e) {
            log.warn("无法获取规范路径，使用绝对路径: {}", e.getMessage());
        }
        
        // 统一使用正斜杠，并确保以斜杠结尾
        absolutePath = absolutePath.replace("\\", "/");
        if (!absolutePath.endsWith("/")) {
            absolutePath += "/";
        }
        
        String fileUrl = "file:///" + absolutePath;
        
        // 禁用默认的资源处理器，使用我们自定义的
        registry.setOrder(1);
        
        // 配置 /uploads/** 路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileUrl)
                .resourceChain(false);
        
        // 配置 /images/** 路径，映射到 uploads 目录下
        registry.addResourceHandler("/images/**")
                .addResourceLocations(fileUrl)
                .resourceChain(false);
        
        log.info("=== 静态资源配置 ===");
        log.info("/uploads/** -> {}", fileUrl);
        log.info("/images/** -> {}", fileUrl);
        log.info("目录是否存在: {}", uploadDir.exists());
        log.info("是否为目录: {}", uploadDir.isDirectory());
        
        // 测试图片文件是否存在
        java.io.File testFile = new java.io.File(uploadDir, "images/products/calculator.jpg");
        log.info("测试文件 images/products/calculator.jpg 是否存在: {}", testFile.exists());
        if (testFile.exists()) {
            log.info("测试文件完整路径: {}", testFile.getAbsolutePath());
        }
    }
}