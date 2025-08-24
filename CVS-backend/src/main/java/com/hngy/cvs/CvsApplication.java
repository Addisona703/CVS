package com.hngy.cvs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CVS 高校志愿服务数字化系统启动类
 * 
 * @author CVS Team
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.hngy.cvs.mapper")
public class CvsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CvsApplication.class, args);
        System.out.println("CVS 高校志愿服务数字化系统启动成功！");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui/index.html");
    }
}
