package com.hngy.cvs.service;

import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.PointsSearchDTO;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.PointsRecordVO;
import com.hngy.cvs.dto.response.PointsStatsVO;
import com.hngy.cvs.dto.response.PointsRankingVO;

/**
 * 积分服务接口
 *
 * @author CVS Team
 */
public interface PointsService {

    /**
     * 获取当前用户积分统计信息（包括总积分和排名）
     *
     * @param userId 用户ID
     * @return 积分统计信息
     */
    PointsStatsVO getCurrentUserPointsStats(Long userId);

    /**
     * 获取积分排行榜（分页显示）
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页的积分排行榜
     */
    PageVO<PointsRankingVO> getPointsRankingPage(int page, int size);

    /**
     * 获取所有用户的积分记录（主要体现从服务记录获得的积分）
     *
     * @param pageRequest 分页请求参数
     * @return 分页的积分记录
     */
    PageVO<PointsRecordVO> getAllPointsRecords(PageDTO<PointsSearchDTO> pageRequest);

    /**
     * 发放积分给用户
     *
     * @param userId 用户ID，不能为空
     * @param points 要发放的积分数量，必须大于0
     * @throws BusinessException 当用户不存在或参数无效时抛出
     */
    void awardPoints(Long userId, Integer points);

    /**
     * 获取用户总积分
     *
     * @param userId 用户ID
     * @return 用户总积分
     */
    Integer getUserTotalPoints(Long userId);

    /**
     * 扣除用户积分
     *
     * @param userId 用户ID
     * @param points 要扣除的积分数量
     * @param reason 扣除原因
     * @throws BusinessException 当积分不足或参数无效时抛出
     */
    void deductPoints(Long userId, Integer points, String reason);

    /**
     * 退还积分给用户
     *
     * @param userId 用户ID
     * @param points 要退还的积分数量
     * @param reason 退还原因
     */
    void refundPoints(Long userId, Integer points, String reason);
}
