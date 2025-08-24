package com.hngy.cvs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.request.CertificateApprovalDTO;
import com.hngy.cvs.dto.request.CertificateCreateDTO;
import com.hngy.cvs.dto.response.CertificateVO;
import com.hngy.cvs.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

/**
 * 志愿证明管理控制器
 * 
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/certificates")
@Tag(name = "志愿证明管理", description = "志愿证明相关接口")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "申请志愿证明")
    public Result<CertificateVO> applyCertificate(
            @Valid @RequestBody CertificateCreateDTO dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        CertificateVO certificateVO = certificateService.applyCertificate(dto, principal.getUserId());
        return Result.success("申请证明成功", certificateVO);
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "审批证明申请")
    public Result<Void> approveCertificate(
            @Valid @RequestBody CertificateApprovalDTO dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        certificateService.approveCertificate(dto, principal.getUserId());
        return Result.success("审批成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取证明详情")
    public Result<CertificateVO> getCertificate(@Parameter(description = "证明ID") @PathVariable Long id) {
        CertificateVO certificateVO = certificateService.getCertificateById(id);
        return Result.success(certificateVO);
    }

    @GetMapping("/number/{certificateNumber}")
    @Operation(summary = "根据证明编号查询证明")
    public Result<CertificateVO> getCertificateByNumber(
            @Parameter(description = "证明编号") @PathVariable String certificateNumber) {
        CertificateVO certificateVO = certificateService.getCertificateByCertificateNumber(certificateNumber);
        return Result.success(certificateVO);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "获取我的证明记录")
    public Result<PageVO<CertificateVO>> getMyCertificates(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @AuthenticationPrincipal UserPrincipal principal) {
        IPage<CertificateVO> certificatePage = certificateService.getMyCertificates(principal.getUserId(), page, size);
        return Result.success(PageUtil.convert(certificatePage));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有证明记录")
    public Result<PageVO<CertificateVO>> getAllCertificates(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        IPage<CertificateVO> certificatePage = certificateService.getAllCertificates(page, size);
        return Result.success(PageUtil.convert(certificatePage));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取待审核的证明记录")
    public Result<PageVO<CertificateVO>> getPendingCertificates(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        IPage<CertificateVO> certificatePage = certificateService.getPendingCertificates(page, size);
        return Result.success(PageUtil.convert(certificatePage));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "删除证明申请")
    public Result<Void> deleteCertificate(
            @Parameter(description = "证明ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        certificateService.deleteCertificate(id, principal.getUserId());
        return Result.success("删除成功");
    }

    @GetMapping("/count/{userId}")
    @Operation(summary = "统计用户的证明数量")
    public Result<Long> countCertificatesByUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        Long count = certificateService.countCertificatesByUser(userId);
        return Result.success(count);
    }
}
