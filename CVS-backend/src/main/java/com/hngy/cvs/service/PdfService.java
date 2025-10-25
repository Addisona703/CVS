package com.hngy.cvs.service;

import java.util.Map;

/**
 * PDF生成服务接口
 *
 * @author CVS Team
 */
public interface PdfService {
    
    /**
     * 生成志愿证书PDF
     *
     * @param data 证书数据（name, username, serial, issueDate）
     * @return PDF字节数组
     */
    byte[] generateCertificatePdf(Map<String, String> data);
}
