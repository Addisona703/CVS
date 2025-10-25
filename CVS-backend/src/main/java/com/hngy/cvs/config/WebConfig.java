package com.hngy.cvs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 *
 * @author CVS Team
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问路径
        String locationPath = uploadPath;
        if (!locationPath.endsWith("/")) {
            locationPath += "/";
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + locationPath)
                .setCachePeriod(3600) // 缓存1小时
                .resourceChain(true);
        
        System.out.println("静态资源配置: /uploads/** -> file:" + locationPath);
        System.out.println("上传路径绝对路径: " + new java.io.File(uploadPath).getAbsolutePath());
    }
}