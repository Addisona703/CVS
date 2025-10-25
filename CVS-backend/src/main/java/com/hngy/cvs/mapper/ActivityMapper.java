package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.request.ActivitySearchDTO;
import com.hngy.cvs.dto.response.ActivityVO;
import com.hngy.cvs.dto.response.DailyStatisticsVO;
import com.hngy.cvs.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 活动数据访问层
 *
 * @author CVS Team
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 获取每日活动统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每日统计数据列表
     */
    List<DailyStatisticsVO> getDailyStatistics(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
}
