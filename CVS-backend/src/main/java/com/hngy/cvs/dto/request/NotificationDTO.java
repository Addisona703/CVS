package com.hngy.cvs.dto.request;

import com.hngy.cvs.entity.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 通知创建数据传输对象
 */
@Data
@Schema(description = "通知创建请求")
public class NotificationDTO {

    @Schema(description = "接收用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "通知类型")
    @NotNull(message = "通知类型不能为空")
    private NotificationType type;

    @Schema(description = "通知标题")
    @NotBlank(message = "通知标题不能为空")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "跳转链接")
    private String linkUrl;

    @Schema(description = "模板数据")
    private Map<String, Object> templateData;
}