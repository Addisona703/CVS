package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.request.SignupSearchDTO;
import com.hngy.cvs.dto.response.SignupVO;
import com.hngy.cvs.entity.Signup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 报名数据访问层
 * 
 * @author CVS Team
 */
@Mapper
public interface SignupMapper extends BaseMapper<Signup> {

    /**
     * 查询用户的报名记录
     */
    IPage<SignupVO> selectMySignups(IPage<SignupVO> page, @Param("userId") Long userId);

    /**
     * 查询活动的报名记录
     */
    IPage<SignupVO> selectActivitySignups(IPage<SignupVO> page, @Param("activityId") Long activityId);

    /**
     * 分页查询报名列表（带搜索条件）
     */
    IPage<SignupVO> selectSignupPage(IPage<SignupVO> page, @Param("searchDTO") SignupSearchDTO searchDTO);

    /**
     * 统计教师待审核的报名数
     */
    @Select("SELECT COUNT(*) FROM signup_twb asu " +
            "JOIN activity_twb va ON asu.activity_id = va.id " +
            "WHERE va.organizer_id = #{organizerId} AND asu.status = 'PENDING' AND va.deleted = 0")
    Long countPendingByOrganizer(@Param("organizerId") Long organizerId);

    /**
     * 统计学生的报名数
     */
    @Select("SELECT COUNT(*) FROM signup_twb WHERE user_id = #{userId}")
    Long countByUser(@Param("userId") Long userId);

    /**
     * 统计活动的已通过报名数
     */
    @Select("SELECT COUNT(*) FROM signup_twb WHERE activity_id = #{activityId} AND status = 'APPROVED'")
    Long countApprovedByActivity(@Param("activityId") Long activityId);
}
