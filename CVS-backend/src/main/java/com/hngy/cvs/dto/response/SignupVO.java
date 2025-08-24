package com.hngy.cvs.dto.response;

import com.hngy.cvs.entity.enums.SignupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名视图对象
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "报名信息")
public class SignupVO {

    @Schema(description = "报名ID", example = "1")
    private Long id;

    @Schema(description = "活动ID", example = "1")
    private Long activityId;

    @Schema(description = "活动标题", example = "社区环保志愿活动")
    private String activityTitle;

    @Schema(description = "用户ID", example = "4")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @Schema(description = "报名状态", example = "APPROVED")
    private SignupStatus status;

    @Schema(description = "报名理由", example = "我对环保很感兴趣")
    private String reason;

    @Schema(description = "拒绝原因", example = "时间冲突")
    private String rejectReason;

    @Schema(description = "是否签到", example = "true")
    private Boolean signedIn;

    @Schema(description = "是否签退", example = "false")
    private Boolean signedOut;

    @Schema(description = "签到时间", example = "2024-03-01T08:50:00")
    private LocalDateTime signInTime;

    @Schema(description = "签退时间", example = "2024-03-01T17:10:00")
    private LocalDateTime signOutTime;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
}
