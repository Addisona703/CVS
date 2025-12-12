package com.hngy.cvs.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态资源控制器
 * 用于处理图片等静态资源的访问
 */
@Slf4j
@RestController
public class StaticResourceController {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @GetMapping("/images/**")
    public ResponseEntity<Resource> getImage(HttpServletRequest request) {
        try {
            // 获取请求的完整路径
            String requestPath = request.getRequestURI();
            // 移除 /images/ 前缀
            String filePath = requestPath.substring("/images/".length());
            
            // 构建完整文件路径
            File uploadDir = new File(uploadPath);
            Path fullPath = Paths.get(uploadDir.getCanonicalPath(), "images", filePath);
            File file = fullPath.toFile();
            
            log.info("请求图片: {} -> {}", requestPath, file.getAbsolutePath());
            
            if (!file.exists() || !file.isFile()) {
                log.warn("文件不存在: {}", file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            // 检测文件类型
            String contentType = Files.probeContentType(fullPath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("获取图片失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
