package com.hngy.cvs.service.impl;

import com.hngy.cvs.common.exception.BusinessException;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.dto.request.SendCodeDTO;
import com.hngy.cvs.dto.request.VerifyCodeDTO;
import com.hngy.cvs.dto.response.VerifyCodeVO;
import com.hngy.cvs.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.Duration;

/**
 * 邮件服务实现类
 * 
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate stringRedisTemplate;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    // 验证码过期时间（5分钟）
    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(5);
    
    // Redis key前缀
    private static final String VERIFICATION_CODE_PREFIX = "email_code:";
    
    // 验证码发送间隔（60秒）
    private static final Duration SEND_INTERVAL = Duration.ofSeconds(60);
    
    // 验证码发送记录前缀
    private static final String SEND_RECORD_PREFIX = "email_send:";

    @Override
    public void sendSimpleMessage(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            javaMailSender.send(message);
            log.info("简单邮件发送成功 - 收件人: {}, 主题: {}", to, subject);
        } catch (Exception e) {
            log.error("简单邮件发送失败 - 收件人: {}, 主题: {}, 错误: {}", to, subject, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendHtmlMessage(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true表示发送HTML格式邮件
            
            javaMailSender.send(mimeMessage);
            log.info("HTML邮件发送成功 - 收件人: {}, 主题: {}", to, subject);
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败 - 收件人: {}, 主题: {}, 错误: {}", to, subject, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendVerificationCode(SendCodeDTO sendCodeDTO) {
        String email = sendCodeDTO.getEmail();
        String type = sendCodeDTO.getType();
        
        // 检查发送频率限制
        String sendRecordKey = SEND_RECORD_PREFIX + email + ":" + type;
        if (stringRedisTemplate.hasKey(sendRecordKey)) {
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "验证码发送过于频繁，请稍后再试");
        }
        
        // 生成6位数字验证码
        String code = generateVerificationCode();
        
        // 存储验证码到Redis，设置5分钟过期时间
        String codeKey = VERIFICATION_CODE_PREFIX + email + ":" + type;
        stringRedisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRATION);
        
        // 记录发送时间，防止频繁发送
        stringRedisTemplate.opsForValue().set(sendRecordKey, String.valueOf(System.currentTimeMillis()), SEND_INTERVAL);
        
        // 发送验证码邮件
        sendVerificationEmail(email, code, type);
        
        log.info("验证码已发送到邮箱: {}, 类型: {}, 验证码: {}", email, type, code);
    }

    @Override
    public VerifyCodeVO verifyCode(VerifyCodeDTO verifyCodeDTO) {
        String email = verifyCodeDTO.getEmail();
        String code = verifyCodeDTO.getCode();
        String type = verifyCodeDTO.getType();
        
        // 从Redis获取验证码
        String codeKey = VERIFICATION_CODE_PREFIX + email + ":" + type;
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        
        if (storedCode == null) {
            return VerifyCodeVO.builder()
                    .verified(false)
                    .message("验证码已过期或不存在")
                    .build();
        }
        
        if (!storedCode.equals(code)) {
            return VerifyCodeVO.builder()
                    .verified(false)
                    .message("验证码不正确")
                    .build();
        }
        
        // 验证成功，删除验证码
        stringRedisTemplate.delete(codeKey);
        
        log.info("验证码验证成功，邮箱: {}, 类型: {}", email, type);
        
        return VerifyCodeVO.builder()
                .verified(true)
                .message("验证码验证成功")
                .build();
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000; // 生成100000-999999之间的数字
        return String.valueOf(code);
    }

    /**
     * 发送验证码邮件
     * 遵循HTML邮件模板规范
     */
    private void sendVerificationEmail(String email, String code, String type) {
        // 根据不同类型生成不同的邮件内容
        String subject;
        String content = switch (type) {
            case "register" -> {
                subject = "CVS系统 - 注册验证码";
                yield buildEmailContent("注册验证", code, "完成账户注册");
            }
            case "reset_password" -> {
                subject = "CVS系统 - 密码重置验证码";
                yield buildEmailContent("密码重置", code, "重置您的密码");
            }
            case "login" -> {
                subject = "CVS系统 - 登录验证码";
                yield buildEmailContent("安全登录", code, "安全登录验证");
            }
            default -> {
                subject = "CVS系统 - 验证码";
                yield buildEmailContent("身份验证", code, "完成身份验证");
            }
        };

        // 调用HTML邮件发送方法
        sendHtmlMessage(email, subject, content);
        log.info("验证码邮件已发送到: {}, 主题: {}", email, subject);
    }

    /**
     * 构建HTML格式的邮件内容
     * 遵循Element Plus配色方案和响应式设计
     */
    private String buildEmailContent(String title, String code, String purpose) {
        return String.format(
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <meta charset='UTF-8'>" +
            "    <title>CVS系统验证码</title>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }" +
            "        .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
            "        .header { background-color: #409EFF; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
            "        .content { padding: 30px; }" +
            "        .code-box { background-color: #f8f9fa; border: 2px dashed #409EFF; border-radius: 6px; padding: 20px; text-align: center; margin: 20px 0; }" +
            "        .code { font-size: 32px; font-weight: bold; color: #409EFF; letter-spacing: 4px; }" +
            "        .footer { background-color: #f8f9fa; padding: 15px; text-align: center; border-radius: 0 0 8px 8px; color: #666; font-size: 12px; }" +
            "        .warning { color: #E6A23C; font-size: 14px; margin-top: 15px; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class='container'>" +
            "        <div class='header'>" +
            "            <h1>CVS 高校志愿服务数字化系统</h1>" +
            "            <p>%s</p>" +
            "        </div>" +
            "        <div class='content'>" +
            "            <p>您好！</p>" +
            "            <p>您正在进行%s操作，请使用以下验证码：</p>" +
            "            <div class='code-box'>" +
            "                <div class='code'>%s</div>" +
            "            </div>" +
            "            <p class='warning'>⚠️ 验证码有效期为5分钟，请及时使用。为了您的账户安全，请勿将验证码泄露给他人。</p>" +
            "            <p>如果这不是您本人的操作，请忽略此邮件。</p>" +
            "        </div>" +
            "        <div class='footer'>" +
            "            <p>此邮件由系统自动发送，请勿回复</p>" +
            "            <p>© 2025 CVS 高校志愿服务数字化系统</p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>",
            title, purpose, code
        );
    }
}