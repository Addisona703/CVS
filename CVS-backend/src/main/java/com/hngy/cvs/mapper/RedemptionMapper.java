package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.cvs.dto.response.ProductRankingVO;
import com.hngy.cvs.dto.response.RedemptionVO;
import com.hngy.cvs.entity.Redemption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 兑换记录数据访问层
 *
 * @author CVS Team
 */
@Mapper
public interface RedemptionMapper extends BaseMapper<Redemption> {

    /**
     * 根据凭证编号查询兑换记录
     *
     * @param voucherCode 凭证编号
     * @return 兑换记录
     */
    Redemption selectByVoucherCode(@Param("voucherCode") String voucherCode);

    /**
     * 统计指定时间范围内的兑换次数
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 兑换次数
     */
    Long countRedemptionsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 统计指定时间范围内的总消耗积分
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总消耗积分
     */
    Long sumPointsSpentByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 统计指定状态的兑换次数
     *
     * @param status 兑换状态
     * @return 兑换次数
     */
    Long countRedemptionsByStatus(@Param("status") Integer status);

    /**
     * 获取商品兑换排行
     *
     * @param limit 限制数量
     * @return 商品兑换排行列表
     */
    List<ProductRankingVO> selectProductRanking(@Param("limit") Integer limit);

    /**
     * 统计已核销的兑换数量
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 已核销数量
     */
    Long countVerifiedRedemptions(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 统计待核销的兑换数量
     *
     * @return 待核销数量
     */
    Long countPendingRedemptions();

    /**
     * 导出兑换记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 兑换记录列表
     */
    List<RedemptionVO> selectRedemptionsForExport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
