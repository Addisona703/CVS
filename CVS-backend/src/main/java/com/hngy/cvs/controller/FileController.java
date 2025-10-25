package com.hngy.cvs.controller;

import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.dto.response.FileUploadVO;
import com.hngy.cvs.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 *
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传相关接口")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload/image")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "上传图片", description = "支持jpg、jpeg、png、gif格式，最大5MB")
    public Result<FileUploadVO> uploadImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file) {
        FileUploadVO result = fileService.uploadImage(file);
        return Result.success("上传成功", result);
    }

    @PostMapping("/upload/document")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "上传文档", description = "支持pdf、doc、docx、xls、xlsx格式，最大10MB")
    public Result<FileUploadVO> uploadDocument(
            @Parameter(description = "文档文件") @RequestParam("file") MultipartFile file) {
        FileUploadVO result = fileService.uploadDocument(file);
        return Result.success("上传成功", result);
    }

    @DeleteMapping("/{fileName}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "删除文件")
    public Result<Void> deleteFile(
            @Parameter(description = "文件名") @PathVariable String fileName) {
        fileService.deleteFile(fileName);
        return Result.success("删除成功");
    }

    @GetMapping("/test")
    @Operation(summary = "测试文件服务")
    public Result<String> testFileService() {
        return Result.success("文件服务正常运行");
    }
}