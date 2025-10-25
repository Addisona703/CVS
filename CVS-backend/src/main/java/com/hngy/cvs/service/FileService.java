package com.hngy.cvs.service;

import com.hngy.cvs.dto.response.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 *
 * @author CVS Team
 */
public interface FileService {

    /**
     * 上传图片
     *
     * @param file 图片文件
     * @return 上传结果
     */
    FileUploadVO uploadImage(MultipartFile file);

    /**
     * 上传文档
     *
     * @param file 文档文件
     * @return 上传结果
     */
    FileUploadVO uploadDocument(MultipartFile file);

    /**
     * 删除文件
     *
     * @param fileName 文件名
     */
    void deleteFile(String fileName);
}