package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.response.ServiceRecordVO;
import com.hngy.cvs.entity.RecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 记录数据访问层
 * 
 * @author CVS Team
 */
@Mapper
public interface RecordMapper extends BaseMapper<RecordEntity> {

    /**
     * 查询用户的服务记录
     */
    IPage<ServiceRecordVO> selectMyRecords(IPage<ServiceRecordVO> page, @Param("userId") Long userId);

    /**
     * 查询活动的服务记录
     */
    IPage<ServiceRecordVO> selectActivityRecords(IPage<ServiceRecordVO> page, @Param("activityId") Long activityId);

    /**
     * 统计教师组织活动的服务记录数
     */
    @Select("SELECT COUNT(*) FROM record_twb sr " +
            "JOIN activity_twb va ON sr.activity_id = va.id " +
            "WHERE va.organizer_id = #{organizerId} AND va.deleted = 0")
    Long countByOrganizer(@Param("organizerId") Long organizerId);

    /**
     * 统计用户从服务记录中获得的总积分
     */
    @Select("SELECT COALESCE(SUM(points_earned), 0) FROM record_twb WHERE user_id = #{userId}")
    Long sumPointsByUser(@Param("userId") Long userId);
}
