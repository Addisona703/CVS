package com.hngy.cvs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.RedemptionQueryRequest;
import com.hngy.cvs.dto.request.RedemptionRequest;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.RedemptionVO;
import com.hngy.cvs.entity.Redemption;

/**
 * 兑换服务接口
 *
 * @author CVS Team
 */
public interface RedemptionService extends IService<Redemption> {

    /**
     * 兑换商品
     *
     * @param userId 用户ID
     * @param request 兑换请求
     * @return 兑换记录VO
     */
    RedemptionVO redeemProduct(Long userId, RedemptionRequest request);

    /**
     * 获取用户兑换记录
     *
     * @param userId 用户ID
     * @param queryRequest 查询请求
     * @return 分页的兑换记录
     */
    PageVO<RedemptionVO> getUserRedemptions(Long userId, PageDTO<RedemptionQueryRequest> queryRequest);

    /**
     * 获取兑换详情
     *
     * @param id 兑换记录ID
     * @param userId 当前用户ID（用于权限检查）
     * @return 兑换记录VO
     */
    RedemptionVO getRedemptionDetail(Long id, Long userId);

    /**
     * 核销兑换凭证
     *
     * @param voucherCode 凭证编号
     * @param staffId 核销人员ID
     */
    void verifyRedemption(String voucherCode, Long staffId);

    /**
     * 取消兑换（退还积分和库存）
     *
     * @param id 兑换记录ID
     * @param userId 用户ID（用于权限检查）
     */
    void cancelRedemption(Long id, Long userId);

    /**
     * 获取所有兑换记录（学工处端）
     *
     * @param queryRequest 查询请求
     * @return 分页的兑换记录
     */
    PageVO<RedemptionVO> getAllRedemptions(PageDTO<RedemptionQueryRequest> queryRequest);

    /**
     * 根据凭证编号获取兑换记录
     *
     * @param voucherCode 凭证编号
     * @return 兑换记录VO
     */
    RedemptionVO getRedemptionByVoucherCode(String voucherCode);

    /**
     * 生成唯一凭证编号
     *
     * @return 凭证编号
     */
    String generateVoucherCode();

    /**
     * 根据状态类型获取兑换记录列表
     *
     * @param pageRequest 分页请求
     * @param statusType 状态类型：TODAY(今日核销)、VERIFIED(累计核销)、PENDING(待核销)
     * @return 分页的兑换记录
     */
    PageVO<RedemptionVO> getRedemptionsByStatus(PageDTO<RedemptionQueryRequest> pageRequest, String statusType);
}