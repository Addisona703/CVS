package com.hngy.cvs.dto.request;

import com.hngy.cvs.entity.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 通知查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知查询参数")
public class NotificationQuery extends PageDTO {

    @Schema(description = "通知类型")
    private NotificationType type;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Override
    @Min(value = 1, message = "页码必须大于0")
    @Max(value = 1000, message = "页码不能超过1000")
    public void setPageNum(Integer pageNum) {
        super.setPageNum(pageNum);
    }

    @Override
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    public void setPageSize(Integer pageSize) {
        super.setPageSize(pageSize);
    }
}