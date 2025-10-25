package com.hngy.cvs.service.impl;

import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * PDF生成服务实现
 *
 * @author CVS Team
 */
@Slf4j
@Service
public class PdfServiceImpl implements PdfService {

    @Value("${certificate.template.path:assets/cert_template.pdf}")
    private String templatePath;

    @Override
    public byte[] generateCertificatePdf(Map<String, String> data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 1. 加载PDF模板
            // 移除 classpath: 前缀（如果存在）
            String resourcePath = templatePath.replace("classpath:", "");
            ClassPathResource resource = new ClassPathResource(resourcePath);

            try (PDDocument document = PDDocument.load(resource.getInputStream())) {
                // 2. 获取PDF表单
                PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
                if (acroForm == null) {
                    AssertUtils.fail("PDF模板不包含表单字段");
                }

                // 调试：打印所有可用的字段名
                log.info("PDF模板中的所有字段:");
                for (PDField field : acroForm.getFields()) {
                    log.info("  字段名: {}, 类型: {}", field.getFullyQualifiedName(), field.getFieldType());
                }

                // 3. 填充表单字段
                // 根据实际PDF模板中的字段名进行映射
                fillFormField(acroForm, "text_12iepk", data.get("name"));      // 姓名
                fillFormField(acroForm, "text_14sfkf", data.get("username"));  // 学号
                fillFormField(acroForm, "text_15uyeh", data.get("serial"));    // 证书编号
                fillFormField(acroForm, "text_17devf", data.get("issueDate")); // 日期

                // 4. 不要flatten，保持表单可编辑以支持中文显示
                // PDF阅读器会使用自己的字体来渲染中文
                // acroForm.flatten();

                // 5. 保存到字节数组
                document.save(baos);

                byte[] pdfBytes = baos.toByteArray();
                log.info("PDF生成成功，大小: {} bytes", pdfBytes.length);
                return pdfBytes;

            }
            // 确保资源正确关闭

        } catch (IOException e) {
            log.error("生成PDF证书失败", e);
            AssertUtils.fail("证书生成失败，请稍后重试");
            return null; // 不会执行到这里
        }
    }
    
    /**
     * 填充PDF表单字段（支持中文）
     *
     * @param acroForm PDF表单对象
     * @param fieldName 字段名称
     * @param value 字段值
     */
    private void fillFormField(PDAcroForm acroForm, String fieldName, String value) {
        try {
            PDField field = acroForm.getField(fieldName);
            if (field != null) {
                // 直接设置字段的值，不触发外观生成
                field.getCOSObject().setString("V", value != null ? value : "");
                
                // 设置字段为只读，去除蓝色背景高亮
                field.setReadOnly(true);
                
                // 设置needAppearances为true，让PDF阅读器自己渲染
                acroForm.setNeedAppearances(true);
            } else {
                log.debug("PDF模板中未找到字段: {}", fieldName);
            }
        } catch (Exception e) {
            log.error("填充PDF字段失败: {}", fieldName, e);
        }
    }
}
