package com.hngy.cvs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.cvs.dto.request.CheckInRequest;
import com.hngy.cvs.dto.request.CheckOutRequest;
import com.hngy.cvs.dto.request.ReviewSearchRequest;
import com.hngy.cvs.dto.request.SignupReviewRequest;
import com.hngy.cvs.dto.response.PendingSignStudentVO;
import com.hngy.cvs.dto.response.SignTokenResponse;
import com.hngy.cvs.dto.response.SignupReviewVO;
import com.hngy.cvs.entity.Signup;

import java.util.List;

/**
 * 签到/签退业务服务
 */
public interface CheckService extends IService<Signup> {

    /**
     * 老师生成签到二维码token
     */
    SignTokenResponse createCheckInToken(Long activityId, Long teacherId);

    /**
     * 老师生成签退二维码token
     */
    SignTokenResponse createCheckOutToken(Long activityId, Long teacherId);

    /**
     * 学生扫码签到
     */
    void checkIn(Long studentId, CheckInRequest request);

    /**
     * 学生扫码签退
     */
    void checkOut(Long studentId, CheckOutRequest request);

    /**
     * 老师调整评分与评价
     */
    void review(Long signupId, SignupReviewRequest request, Long teacherId);

    /**
     * 查询待审核的签退列表
     */
    IPage<SignupReviewVO> searchReviews(ReviewSearchRequest request, Long teacherId, int pageNum, int pageSize);

    /**
     * 查询未签到学生列表
     */
    List<PendingSignStudentVO> listPendingCheckInStudents(Long activityId, Long teacherId);

    /**
     * 查询未签退学生列表（已签到但未签退）
     */
    List<PendingSignStudentVO> listPendingCheckOutStudents(Long activityId, Long teacherId);
}
