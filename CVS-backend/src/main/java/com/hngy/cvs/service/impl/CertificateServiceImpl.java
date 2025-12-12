package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.cvs.common.exception.BusinessException;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.BeanUtil;
import com.hngy.cvs.dto.request.CertificateApprovalDTO;
import com.hngy.cvs.dto.request.CertificateCreateDTO;
import com.hngy.cvs.dto.response.CertificateVO;
import com.hngy.cvs.entity.Certificate;
import com.hngy.cvs.entity.enums.CertificateStatus;
import com.hngy.cvs.mapper.CertificateMapper;
import com.hngy.cvs.service.CertificateService;
import com.hngy.cvs.service.strategy.EligibilityResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 志愿证明服务实现类
 * 
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final CertificateMapper certificateMapper;
    private final com.hngy.cvs.service.strategy.CertificateEligibilityStrategy eligibilityStrategy;
    private final com.hngy.cvs.service.PdfService pdfService;
    private final com.hngy.cvs.mapper.UserMapper userMapper;

    @Override
    @Transactional
    public CertificateVO applyCertificate(CertificateCreateDTO request, Long userId) {
        // 1. 验证资格（使用策略模式）
        EligibilityResult eligibilityResult = eligibilityStrategy.validate(userId);
        AssertUtils.isTrue(eligibilityResult.isEligible(), eligibilityResult.getMessage());
        
        // 2. 检查是否有待审核的申请
        long pendingCount = certificateMapper.selectCount(
            new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getUserId, userId)
                .eq(Certificate::getStatus, CertificateStatus.PENDING)
        );
        com.hngy.cvs.common.util.AssertUtils.isTrue(pendingCount == 0, "您有待审核的证书申请，请等待审核完成");
        
        // 3. 验证日期
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "结束日期不能早于开始日期");
        }

        // 4. 创建证明申请
        Certificate certificate = BeanUtil.to(request, Certificate.class);
        certificate.setUserId(userId);
        certificate.setStatus(CertificateStatus.PENDING);

        certificateMapper.insert(certificate);
        log.info("用户 {} 申请志愿证明成功", userId);

        return convertToVO(certificate);
    }

    @Override
    @Transactional
    public void approveCertificate(CertificateApprovalDTO request, Long approverId) {
        Certificate certificate = certificateMapper.selectById(request.getCertificateId());
        if (certificate == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "证明申请不存在");
        }

        // 检查状态
        if (certificate.getStatus() != CertificateStatus.PENDING) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "只能审批待审核的证明申请");
        }

        // 如果是拒绝，需要提供拒绝原因
        if (request.getStatus() == CertificateStatus.REJECTED && 
            (request.getRejectReason() == null || request.getRejectReason().trim().isEmpty())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "拒绝申请必须提供拒绝原因");
        }

        certificate.setStatus(request.getStatus());
        certificate.setApproverId(approverId);
        certificate.setApprovedAt(LocalDateTime.now());

        if (request.getRejectReason() != null) {
            certificate.setRejectReason(request.getRejectReason());
        }

        // 如果通过，生成证明编号
        if (request.getStatus() == CertificateStatus.APPROVED) {
            certificate.setCertificateNumber(generateCertificateNumber());
        }

        certificateMapper.updateById(certificate);
        log.info("审批证明申请 {} 成功，状态: {}", request.getCertificateId(), request.getStatus());
    }

    @Override
    public CertificateVO getCertificateById(Long id) {
        Certificate certificate = certificateMapper.selectById(id);
        if (certificate == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "证明不存在");
        }
        return convertToVO(certificate);
    }

    @Override
    public CertificateVO getCertificateByCertificateNumber(String certificateNumber) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getCertificateNumber, certificateNumber);
        Certificate certificate = certificateMapper.selectOne(wrapper);
        if (certificate == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "证明不存在");
        }
        return convertToVO(certificate);
    }

    @Override
    public IPage<CertificateVO> getMyCertificates(Long userId, int page, int size) {
        Page<CertificateVO> certificatePage = new Page<>(page, size);
        return certificateMapper.selectMyCertificates(certificatePage, userId);
    }

    @Override
    public IPage<CertificateVO> getAllCertificates(int page, int size) {
        Page<CertificateVO> certificatePage = new Page<>(page, size);
        return certificateMapper.selectAllCertificates(certificatePage);
    }

    @Override
    public IPage<CertificateVO> getPendingCertificates(int page, int size) {
        Page<CertificateVO> certificatePage = new Page<>(page, size);
        return certificateMapper.selectPendingCertificates(certificatePage);
    }

    @Override
    @Transactional
    public void deleteCertificate(Long id, Long userId) {
        Certificate certificate = certificateMapper.selectById(id);
        if (certificate == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "证明不存在");
        }

        // 检查权限
        if (!certificate.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.INSUFFICIENT_PERMISSIONS);
        }

        // 检查状态，只有待审核的申请可以删除
        if (certificate.getStatus() != CertificateStatus.PENDING) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "只能删除待审核的证明申请");
        }

        certificateMapper.deleteById(id);
        log.info("删除证明申请成功: {}", id);
    }

    @Override
    public Long countCertificatesByUser(Long userId) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getUserId, userId);
        return certificateMapper.selectCount(wrapper);
    }

    @Override
    public Long countPendingCertificates() {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getStatus, CertificateStatus.PENDING);
        return certificateMapper.selectCount(wrapper);
    }

    /**
     * 统计用户的已通过证明数
     */
    public Long countApprovedByUser(Long userId) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getUserId, userId)
               .eq(Certificate::getStatus, CertificateStatus.APPROVED);
        return certificateMapper.selectCount(wrapper);
    }

    @Override
    public String generateCertificateNumber() {
        // 生成格式：CVS + 年月日 + 5位序号
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 这里简化处理，实际应该查询当天的最大序号
        long sequence = System.currentTimeMillis() % 100000;

        return String.format("CVS%s%05d", dateStr, sequence);
    }

    @Override
    public Long countByCondition(LambdaQueryWrapper<Certificate> wrapper) {
        return certificateMapper.selectCount(wrapper);
    }

    @Override
    public byte[] generateCertificatePdf(Long certificateId, Long userId, com.hngy.cvs.entity.enums.UserRole userRole) {
        // 1. 验证权限
        validateCertificateAccess(certificateId, userId, userRole);
        
        // 2. 获取证书记录
        Certificate certificate = certificateMapper.selectById(certificateId);
        com.hngy.cvs.common.util.AssertUtils.notNull(certificate, "证书不存在");
        
        // 3. 验证证书状态
        com.hngy.cvs.common.util.AssertUtils.isTrue(
            certificate.getStatus() == CertificateStatus.APPROVED,
            "证书尚未通过审核，无法生成"
        );
        
        // 4. 获取用户信息
        com.hngy.cvs.entity.User user = userMapper.selectById(certificate.getUserId());
        com.hngy.cvs.common.util.AssertUtils.notNull(user, "用户不存在");
        
        // 5. 准备PDF数据
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("name", user.getName());
        data.put("username", user.getUsername());
        data.put("serial", certificate.getCertificateNumber());
        data.put("issueDate", formatDate(certificate.getApprovedAt()));
        
        // 6. 调用PdfService生成PDF
        return pdfService.generateCertificatePdf(data);
    }

    /**
     * 验证证书访问权限
     *
     * @param certificateId 证书ID
     * @param userId 用户ID
     * @param role 用户角色
     */
    private void validateCertificateAccess(Long certificateId, Long userId, com.hngy.cvs.entity.enums.UserRole role) {
        Certificate certificate = certificateMapper.selectById(certificateId);
        com.hngy.cvs.common.util.AssertUtils.notNull(certificate, "证书不存在");
        
        // 学工处可以访问所有证书
        if (role == com.hngy.cvs.entity.enums.UserRole.ADMIN) {
            return;
        }
        
        // 普通用户只能访问自己的证书
        com.hngy.cvs.common.util.AssertUtils.isTrue(
            certificate.getUserId().equals(userId),
            "无权访问该证书"
        );
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
    }

    private CertificateVO convertToVO(Certificate certificate) {
        CertificateVO vo = BeanUtil.to(certificate, CertificateVO.class);
        
        // 填充用户信息（username 和 name）
        if (certificate.getUserId() != null) {
            com.hngy.cvs.entity.User user = userMapper.selectById(certificate.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setName(user.getName());
            }
        }
        
        // 填充审批人姓名
        if (certificate.getApproverId() != null) {
            com.hngy.cvs.entity.User approver = userMapper.selectById(certificate.getApproverId());
            if (approver != null) {
                vo.setApproverName(approver.getName());
            }
        }
        
        return vo;
    }
}
