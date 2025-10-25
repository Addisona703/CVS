package com.hngy.cvs.dto.response;

import com.hngy.cvs.entity.enums.CertificateStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 证明视图对象
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "证明信息")
public class CertificateVO {

    @Schema(description = "证明ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "4")
    private Long userId;

    @Schema(description = "用户名", example = "student001")
    private String username;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "申请目的", example = "申请奖学金需要")
    private String purpose;

    @Schema(description = "服务开始日期", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "服务结束日期", example = "2024-03-01")
    private LocalDate endDate;

    @Schema(description = "证明状态", example = "APPROVED")
    private CertificateStatus status;

    @Schema(description = "拒绝原因", example = "服务时长不足")
    private String rejectReason;

    @Schema(description = "审批人ID", example = "1")
    private Long approverId;

    @Schema(description = "审批人姓名", example = "管理员")
    private String approverName;

    @Schema(description = "审批时间", example = "2024-01-01T10:00:00")
    private LocalDateTime approvedAt;

    @Schema(description = "证明编号", example = "CVS202400001")
    private String certificateNumber;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "服务统计信息")
    private ServiceStatistics statistics;

    /**
     * 服务统计信息内部类
     */
    @Data
    @Schema(description = "服务统计信息")
    public static class ServiceStatistics {
        @Schema(description = "完成的活动数量", example = "5")
        private Integer completedActivityCount;

        @Schema(description = "总服务时长（小时）", example = "20.5")
        private Double totalServiceHours;
    }
}
