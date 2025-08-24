package com.hngy.cvs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.response.PointsRecordVO;

import java.util.List;

/**
 * 积分服务接口
 * 
 * @author CVS Team
 */
public interface PointsService {

    /**
     * 手动调整用户积分（直接在points表中添加记录）
     */
    void adjustUserPoints(Long userId, Integer points);

    /**
     * 获取用户积分记录（从points表查询手动调整记录）
     */
    IPage<PointsRecordVO> getUserPointsRecords(Long userId, int page, int size);

    /**
     * 获取用户总积分（从record表和points表汇总）
     */
    Long getUserTotalPoints(Long userId);

    /**
     * 获取积分排行榜
     */
    List<PointsRecordVO> getPointsRanking(int limit);

    /**
     * 获取用户积分排名
     */
    Long getUserPointsRanking(Long userId);

    /**
     * 统计积分记录数
     */
    Long countPointsRecords();
}
