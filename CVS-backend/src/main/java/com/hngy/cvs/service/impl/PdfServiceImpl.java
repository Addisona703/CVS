package com.hngy.cvs.service.impl;

import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Service
public class PdfServiceImpl implements PdfService {

    @Value("${certificate.template.path:assets/cert_template.pdf}")
    private String templatePath;

    // 缓存模板和字体，避免重复加载
    private byte[] templateCache;
    private byte[] fontCache;

    /**
     * 应用启动时预加载模板和字体到内存
     */
    @PostConstruct
    public void init() {
        try {
            // 预加载 PDF 模板
            String resourcePath = templatePath.replace("classpath:", "");
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (InputStream is = resource.getInputStream()) {
                templateCache = is.readAllBytes();
                log.info("✓ PDF模板预加载成功，大小: {} KB", templateCache.length / 1024);
            }

            // 预加载字体文件
            ClassPathResource fontRes = new ClassPathResource("fonts/SimHei.ttf");
            try (InputStream is = fontRes.getInputStream()) {
                fontCache = is.readAllBytes();
                log.info("✓ 中文字体预加载成功，大小: {} KB", fontCache.length / 1024);
            }
        } catch (IOException e) {
            log.error("预加载资源失败，PDF生成功能可能无法正常工作", e);
        }
    }

    @Override
    public byte[] generateCertificatePdf(Map<String, String> data) {
        long startTime = System.currentTimeMillis();
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PDDocument document = Loader.loadPDF(templateCache)) {

            // 1. 获取表单
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm == null) {
                AssertUtils.fail("PDF模板不包含表单字段");
            }

            // PDFBox 3 推荐开启 NeedAppearances = true
            acroForm.setNeedAppearances(true);

            // 2. 从缓存加载中文字体（避免重复读取文件）
            PDType0Font font;
            try (InputStream fontStream = new ByteArrayInputStream(fontCache)) {
                font = PDType0Font.load(document, fontStream, false);
            }

            PDResources dr = acroForm.getDefaultResources();
            if (dr == null) {
                dr = new PDResources();
            }

            // 获取字体 key（如 F1）
            COSName fontKey = dr.add(font);
            String fontName = fontKey.getName();

            // 映射 /Helv 到自定义字体
            dr.put(COSName.getPDFName("Helv"), font);
            acroForm.setDefaultResources(dr);

            // 3. 页资源同步字体（部分阅读器必需）
            for (PDPage page : document.getPages()) {
                PDResources pr = page.getResources();
                if (pr == null) pr = new PDResources();
                pr.put(fontKey, font);
                pr.put(COSName.getPDFName("Helv"), font);
                page.setResources(pr);
            }

            // 4. 填充表单字段
            fillField(acroForm, fontName, "text_12iepk", data.get("name"));
            fillField(acroForm, fontName, "text_14sfkf", data.get("username"));
            fillField(acroForm, fontName, "text_15uyeh", data.get("serial"));
            fillField(acroForm, fontName, "text_17devf", data.get("issueDate"));

            // 5. Flatten 表单（PDFBox 3 会自动处理外观）
            acroForm.flatten();

            document.save(baos);
            byte[] result = baos.toByteArray();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("PDF生成成功 [证书号: {}, 耗时: {}ms, 大小: {}KB]", 
                data.get("serial"), duration, result.length / 1024);
            
            return result;

        } catch (IOException e) {
            log.error("生成PDF证书失败 [证书号: {}]", data.get("serial"), e);
            AssertUtils.fail("证书生成失败，请稍后重试");
            return null;
        }
    }

    /**
     * 填充单个 PDF Text 字段
     */
    private void fillField(PDAcroForm acroForm, String fontName, String fieldName, String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return;
            }

            PDField field = acroForm.getField(fieldName);
            if (!(field instanceof PDTextField tf)) {
                log.warn("PDF字段不存在或类型错误: {}", fieldName);
                return;
            }

            // 设置字段级 DA
            // 0 g = 黑色填充，15 = 字号
            String da = String.format("/%s 15 Tf 0 g", fontName);
            tf.getCOSObject().setString(COSName.DA, da);

            // 设置值
            tf.setValue(value);

        } catch (Exception e) {
            log.error("填充字段失败 [字段: {}, 值: {}]", fieldName, value, e);
        }
    }
}
