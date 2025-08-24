package com.hngy.cvs.service;

import com.hngy.cvs.dto.request.SendCodeDTO;
import com.hngy.cvs.dto.request.VerifyCodeDTO;
import com.hngy.cvs.dto.response.VerifyCodeVO;

/**
 * 邮件服务接口
 * 
 * @author CVS Team
 */
public interface EmailService {

    /**
     * 发送简单文本邮件
     * 
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendSimpleMessage(String to, String subject, String content);

    /**
     * 发送HTML格式邮件
     * 
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param htmlContent HTML格式的邮件内容
     */
    void sendHtmlMessage(String to, String subject, String htmlContent);

    /**
     * 发送邮箱验证码
     * 
     * @param sendCodeDTO 发送验证码请求
     */
    void sendVerificationCode(SendCodeDTO sendCodeDTO);

    /**
     * 验证验证码
     * 
     * @param verifyCodeDTO 验证验证码请求
     * @return 验证结果
     */
    VerifyCodeVO verifyCode(VerifyCodeDTO verifyCodeDTO);
}