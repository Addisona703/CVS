package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.response.PendingSignStudentVO;
import com.hngy.cvs.dto.response.SignupVO;
import com.hngy.cvs.entity.Signup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 报名数据访问层
 * 
 * @author CVS Team
 */
@Mapper
public interface SignupMapper extends BaseMapper<Signup> {

    /**
     * 查询未签到的报名学生
     */
    @Select("""
            SELECT u.name AS name,
                   u.username AS username
            FROM signup_twb s
                     INNER JOIN user_twb u ON s.user_id = u.id
            WHERE s.activity_id = #{activityId}
              AND s.status = 'APPROVED'
              AND (s.signed_in IS NULL OR s.signed_in = 0)
            """)
    List<PendingSignStudentVO> selectPendingCheckInStudents(@Param("activityId") Long activityId);

    /**
     * 查询未签退的报名学生（已签到但未签退）
     */
    @Select("""
            SELECT u.name AS name,
                   u.username AS username
            FROM signup_twb s
                     INNER JOIN user_twb u ON s.user_id = u.id
            WHERE s.activity_id = #{activityId}
              AND s.status = 'APPROVED'
              AND s.signed_in = 1
              AND (s.signed_out IS NULL OR s.signed_out = 0)
            """)
    List<PendingSignStudentVO> selectPendingCheckOutStudents(@Param("activityId") Long activityId);

    /**
     * 分页查询指定活动的报名列表
     */
    IPage<SignupVO> selectSignupPage(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Signup> page,
            @Param("activityId") Long activityId,
            @Param("status") com.hngy.cvs.entity.enums.SignupStatus status,
            @Param("userName") String userName,
            @Param("signedIn") Boolean signedIn,
            @Param("signedOut") Boolean signedOut
    );
}
