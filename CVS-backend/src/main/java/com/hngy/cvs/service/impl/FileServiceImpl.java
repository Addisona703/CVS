package com.hngy.cvs.service.impl;

import com.hngy.cvs.common.exception.BusinessException;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.dto.response.FileUploadVO;
import com.hngy.cvs.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务实现
 *
 * @author CVS Team
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Value("${file.upload.domain:http://localhost:8000}")
    private String uploadDomain;

    // 支持的图片格式
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    // 支持的文档格式
    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
    );

    // 图片最大大小：5MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    // 文档最大大小：10MB
    private static final long MAX_DOCUMENT_SIZE = 10 * 1024 * 1024;

    @Override
    public FileUploadVO uploadImage(MultipartFile file) {
        validateFile(file, IMAGE_EXTENSIONS, MAX_IMAGE_SIZE, "图片");
        return saveFile(file, "images");
    }

    @Override
    public FileUploadVO uploadDocument(MultipartFile file) {
        validateFile(file, DOCUMENT_EXTENSIONS, MAX_DOCUMENT_SIZE, "文档");
        return saveFile(file, "documents");
    }

    @Override
    public void deleteFile(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件名不能为空");
        }

        try {
            // 构建文件路径
            Path filePath = Paths.get(uploadPath, fileName);
            
            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                log.warn("要删除的文件不存在: {}", fileName);
                return;
            }

            // 删除文件
            Files.delete(filePath);
            log.info("文件删除成功: {}", fileName);
            
        } catch (IOException e) {
            log.error("删除文件失败: {}", fileName, e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "删除文件失败");
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file, List<String> allowedExtensions, 
                            long maxSize, String fileType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, fileType + "文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > maxSize) {
            throw new BusinessException(ResultCode.BAD_REQUEST, 
                    fileType + "文件大小不能超过" + (maxSize / 1024 / 1024) + "MB");
        }

        // 检查文件扩展名
        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFileName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件名不能为空");
        }

        String extension = getFileExtension(originalFileName).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, 
                    "不支持的" + fileType + "格式，支持的格式：" + String.join(", ", allowedExtensions));
        }

        // 检查文件内容类型
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无法识别文件类型");
        }
    }

    /**
     * 保存文件
     */
    private FileUploadVO saveFile(MultipartFile file, String subDir) {
        try {
            // 创建上传目录
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadDir = Paths.get(uploadPath, subDir, dateDir);
            Files.createDirectories(uploadDir);

            // 生成新文件名（确保文件名安全）
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName).toLowerCase();
            String newFileName = generateFileName() + "." + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(newFileName);
            Files.copy(file.getInputStream(), filePath);

            // 构建文件URL（使用正斜杠）
            String relativePath = subDir + "/" + dateDir + "/" + newFileName;
            String fileUrl = uploadDomain + "/uploads/" + relativePath;

            log.info("文件上传成功: {} -> {}", originalFileName, newFileName);
            log.info("文件URL: {}", fileUrl);

            return FileUploadVO.builder()
                    .fileName(newFileName)
                    .originalFileName(originalFileName)
                    .fileUrl(fileUrl)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .uploadTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();

        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "文件保存失败");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }

    /**
     * 生成文件名
     */
    private String generateFileName() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) 
                + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}