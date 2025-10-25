package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应VO
 *
 * @author CVS Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传响应")
public class FileUploadVO {

    @Schema(description = "文件名", example = "product_20231024_123456.jpg")
    private String fileName;

    @Schema(description = "原始文件名", example = "商品图片.jpg")
    private String originalFileName;

    @Schema(description = "文件URL", example = "/uploads/images/product_20231024_123456.jpg")
    private String fileUrl;

    @Schema(description = "文件大小（字节）", example = "1024000")
    private Long fileSize;

    @Schema(description = "文件类型", example = "image/jpeg")
    private String contentType;

    @Schema(description = "上传时间", example = "2023-10-24T12:34:56")
    private String uploadTime;
}