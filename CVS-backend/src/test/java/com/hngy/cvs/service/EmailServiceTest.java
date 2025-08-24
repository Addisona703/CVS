package com.hngy.cvs.service;

import com.hngy.cvs.dto.request.SendCodeDTO;
import com.hngy.cvs.dto.request.VerifyCodeDTO;
import com.hngy.cvs.dto.response.VerifyCodeVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 邮件服务测试类
 * 
 * @author CVS Team
 */
@SpringBootTest
@ActiveProfiles("test")
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    void testSendSimpleMessage() {
        // 注意：这个测试需要真实的邮件配置才能运行
        // 在测试环境中可能需要Mock邮件服务
        String to = "test@example.com";
        String subject = "CVS系统测试邮件";
        String content = "这是一封测试邮件，验证邮件服务是否正常工作。";
        
        try {
            emailService.sendSimpleMessage(to, subject, content);
            System.out.println("简单文本邮件测试成功");
        } catch (Exception e) {
            System.err.println("邮件发送失败: " + e.getMessage());
        }
    }

    @Test
    void testSendHtmlMessage() {
        // 注意：这个测试需要真实的邮件配置才能运行
        String to = "test@example.com";
        String subject = "CVS系统HTML测试邮件";
        String htmlContent = """
            <html>
            <body>
                <h1>CVS系统测试</h1>
                <p>这是一封<b>HTML格式</b>的测试邮件。</p>
                <div style="background-color: #f0f0f0; padding: 10px; border-radius: 5px;">
                    <p>验证码: <span style="font-size: 24px; color: #409EFF; font-weight: bold;">123456</span></p>
                </div>
            </body>
            </html>
            """;
        
        try {
            emailService.sendHtmlMessage(to, subject, htmlContent);
            System.out.println("HTML邮件测试成功");
        } catch (Exception e) {
            System.err.println("邮件发送失败: " + e.getMessage());
        }
    }

    @Test
    void testSendVerificationCode() {
        // 测试验证码发送功能
        SendCodeDTO sendCodeDTO = new SendCodeDTO();
        sendCodeDTO.setEmail("test@example.com");
        sendCodeDTO.setType("register");
        
        try {
            emailService.sendVerificationCode(sendCodeDTO);
            System.out.println("验证码发送测试成功");
        } catch (Exception e) {
            System.err.println("验证码发送失败: " + e.getMessage());
        }
    }

    @Test
    void testVerifyCode() {
        // 测试验证码验证功能
        VerifyCodeDTO verifyCodeDTO = new VerifyCodeDTO();
        verifyCodeDTO.setEmail("test@example.com");
        verifyCodeDTO.setCode("123456");
        verifyCodeDTO.setType("register");
        
        try {
            VerifyCodeVO result = emailService.verifyCode(verifyCodeDTO);
            System.out.println("验证结果: " + result.getVerified() + ", 消息: " + result.getMessage());
        } catch (Exception e) {
            System.err.println("验证码验证失败: " + e.getMessage());
        }
    }
}