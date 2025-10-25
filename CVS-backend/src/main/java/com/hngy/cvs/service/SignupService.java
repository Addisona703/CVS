package com.hngy.cvs.service;

import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.SignupApprovalDTO;
import com.hngy.cvs.dto.request.SignupCreateDTO;
import com.hngy.cvs.dto.request.SignupSearchDTO;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.SignupVO;

import java.util.List;

/**
 * 报名服务接口
 * 
 * @author CVS Team
 */
public interface SignupService {

    /**
     * 报名活动
     */
    SignupVO signupActivity(SignupCreateDTO request, Long userId);

    /**
     * 根据ID获取报名详情
     */
    SignupVO getSignupById(Long id);

    /**
     * 分页查询报名列表
     * 支持按活动名称、学生名称模糊查询，按报名状态筛选
     */
    PageVO<SignupVO> getSignupList(PageDTO<SignupSearchDTO> pageRequest, Long teacherId);

    /**
     * 获取我的报名列表
     */
    PageVO<SignupVO> getMySignups(PageDTO<SignupSearchDTO> pageRequest, Long userId);

    /**
     * 审核通过报名
     */
    void approveSignup(Long signupId, Long operatorId);

    /**
     * 拒绝报名
     */
    void rejectSignup(Long signupId, String rejectReason, Long operatorId);

    /**
     * 批量审核通过报名
     */
    void batchApproveSignups(List<Long> signupIds, Long operatorId);

    /**
     * 批量拒绝报名
     */
    void batchRejectSignups(List<Long> signupIds, String rejectReason, Long operatorId);

    /**
     * 获取已签到但未签退的人员列表（教师仪表盘用）
     */
    PageVO<SignupVO> getCheckedInSignups(PageDTO<SignupSearchDTO> pageRequest, Long teacherId);

    /**
     * 取消报名
     */
    void cancelSignup(Long signupId, Long userId);

    /**
     * 统计总报名数
     */
    Long countSignups();

    /**
     * 统计用户的报名数
     */
    Long countSignupsByUser(Long userId);

    /**
     * 统计教师所有活动的已通过报名总人数
     */
    Long countApprovedSignupsByOrganizer(Long organizerId);

    /**
     * 统计教师所有活动的待审核报名人数
     */
    Long countPendingSignupsByOrganizer(Long organizerId);

    /**
     * 获取指定活动的报名列表
     */
    PageVO<SignupVO> getActivitySignups(Long activityId, PageDTO<SignupSearchDTO> pageRequest);
}
