package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.response.PointsRecordVO;
import com.hngy.cvs.entity.Points;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 积分数据访问层
 * 
 * @author CVS Team
 */
@Mapper
public interface PointsMapper extends BaseMapper<Points> {

    /**
     * 查询用户的积分记录
     */
    IPage<PointsRecordVO> selectMyPoints(IPage<PointsRecordVO> page, @Param("userId") Long userId);

    /**
     * 统计用户的总积分
     */
    @Select("SELECT COALESCE(SUM(points), 0) FROM points_twb WHERE user_id = #{userId}")
    Long sumPointsByUser(@Param("userId") Long userId);

    /**
     * 获取积分排行榜
     */
    @Select("SELECT u.id, u.username, u.name, " +
            "(COALESCE(SUM(pr.points), 0) + COALESCE(SUM(sr.points_earned), 0)) as total_points " +
            "FROM user_twb u " +
            "LEFT JOIN points_twb pr ON u.id = pr.user_id " +
            "LEFT JOIN record_twb sr ON u.id = sr.user_id " +
            "WHERE u.deleted = 0 AND u.role = 'STUDENT' " +
            "GROUP BY u.id, u.username, u.name " +
            "ORDER BY total_points DESC " +
            "LIMIT #{limit}")
    List<PointsRecordVO> selectPointsRanking(@Param("limit") int limit);

    /**
     * 获取用户积分排名
     */
    @Select("SELECT ranking FROM (" +
            "SELECT u.id, ROW_NUMBER() OVER (ORDER BY " +
            "(COALESCE(SUM(pr.points), 0) + COALESCE(SUM(sr.points_earned), 0)) DESC) as ranking " +
            "FROM user_twb u " +
            "LEFT JOIN points_twb pr ON u.id = pr.user_id " +
            "LEFT JOIN record_twb sr ON u.id = sr.user_id " +
            "WHERE u.deleted = 0 AND u.role = 'STUDENT' " +
            "GROUP BY u.id" +
            ") ranked WHERE id = #{userId}")
    Long getUserPointsRanking(@Param("userId") Long userId);
}
