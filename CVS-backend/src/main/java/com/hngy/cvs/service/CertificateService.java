package com.hngy.cvs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.request.CertificateApprovalDTO;
import com.hngy.cvs.dto.request.CertificateCreateDTO;
import com.hngy.cvs.dto.response.CertificateVO;
import com.hngy.cvs.entity.Certificate;

/**
 * 志愿证明服务接口
 * 
 * @author CVS Team
 */
public interface CertificateService {

    /**
     * 申请志愿证明
     */
    CertificateVO applyCertificate(CertificateCreateDTO request, Long userId);

    /**
     * 审批证明申请
     */
    void approveCertificate(CertificateApprovalDTO request, Long approverId);

    /**
     * 获取证明详情
     */
    CertificateVO getCertificateById(Long id);

    /**
     * 根据证明编号查询证明
     */
    CertificateVO getCertificateByCertificateNumber(String certificateNumber);

    /**
     * 获取我的证明记录
     */
    IPage<CertificateVO> getMyCertificates(Long userId, int page, int size);

    /**
     * 获取所有证明记录（管理员）
     */
    IPage<CertificateVO> getAllCertificates(int page, int size);

    /**
     * 获取待审核的证明记录
     */
    IPage<CertificateVO> getPendingCertificates(int page, int size);

    /**
     * 删除证明申请
     */
    void deleteCertificate(Long id, Long userId);

    /**
     * 统计用户的证明数量
     */
    Long countCertificatesByUser(Long userId);

    /**
     * 统计待审核的证明数量
     */
    Long countPendingCertificates();

    /**
     * 生成证明编号
     */
    String generateCertificateNumber();

    /**
     * 按条件统计证明数量
     */
    Long countByCondition(LambdaQueryWrapper<Certificate> wrapper);
}
