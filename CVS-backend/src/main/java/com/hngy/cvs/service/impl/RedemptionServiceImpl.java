package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.RedemptionQueryRequest;
import com.hngy.cvs.dto.request.RedemptionRequest;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.RedemptionVO;
import com.hngy.cvs.entity.Product;
import com.hngy.cvs.entity.Redemption;
import com.hngy.cvs.entity.User;
import com.hngy.cvs.entity.enums.ProductStatus;
import com.hngy.cvs.entity.enums.RedemptionStatus;
import com.hngy.cvs.entity.enums.UserRole;
import com.hngy.cvs.mapper.ProductMapper;
import com.hngy.cvs.mapper.RedemptionMapper;
import com.hngy.cvs.mapper.UserMapper;
import com.hngy.cvs.service.PointsService;
import com.hngy.cvs.service.RedemptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 兑换服务实现类
 *
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedemptionServiceImpl extends ServiceImpl<RedemptionMapper, Redemption> implements RedemptionService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final PointsService pointsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RedemptionVO redeemProduct(Long userId, RedemptionRequest request) {
        // 参数校验
        AssertUtils.notNull(userId, "用户ID不能为空");
        AssertUtils.notNull(request, "兑换请求不能为空");
        AssertUtils.notNull(request.getProductId(), "商品ID不能为空");

        log.debug("用户 {} 兑换商品 {}", userId, request.getProductId());

        // 1. 验证用户存在
        User user = userMapper.selectById(userId);
        AssertUtils.notNull(user, "用户不存在");
        AssertUtils.isFalse(user.getDeleted() == 1, "用户已被删除");

        // 2. 查询商品信息
        Product product = productMapper.selectById(request.getProductId());
        AssertUtils.notNull(product, "商品不存在");
        AssertUtils.isFalse(product.getDeleted() == 1, "商品已被删除");

        // 3. 检查商品状态
        AssertUtils.isTrue(ProductStatus.ONLINE.getCode().equals(product.getStatus()), "商品已下架");

        // 4. 检查库存（加锁扣减）
        int stockUpdated = productMapper.decreaseStock(product.getId());
        AssertUtils.isTrue(stockUpdated > 0, "商品库存不足");

        try {
            // 5. 检查用户积分
            Integer userPoints = pointsService.getUserTotalPoints(userId);
            if (userPoints < product.getPointsRequired()) {
                // 回滚库存
                productMapper.increaseStock(product.getId());
                AssertUtils.fail("积分不足，当前积分: " + userPoints + "，需要积分: " + product.getPointsRequired());
            }

            // 6. 扣除积分
            pointsService.deductPoints(userId, product.getPointsRequired(), "兑换商品：" + product.getName());

            // 7. 创建兑换记录
            Redemption redemption = new Redemption();
            redemption.setUserId(userId);
            redemption.setProductId(product.getId());
            redemption.setPointsSpent(product.getPointsRequired());
            redemption.setVoucherCode(generateVoucherCode());
            redemption.setStatus(RedemptionStatus.PENDING.getCode());

            boolean saved = this.save(redemption);
            AssertUtils.isTrue(saved, "兑换记录创建失败");

            log.info("用户 {} 成功兑换商品 {}，消耗积分 {}，凭证编号 {}", 
                    userId, product.getName(), product.getPointsRequired(), redemption.getVoucherCode());

            // 8. 返回兑换结果
            return buildRedemptionVO(redemption, user, product, null);

        } catch (Exception e) {
            // 发生异常时回滚库存
            productMapper.increaseStock(product.getId());
            throw e;
        }
    }

    @Override
    public PageVO<RedemptionVO> getUserRedemptions(Long userId, PageDTO<RedemptionQueryRequest> queryRequest) {
        // 参数校验
        AssertUtils.notNull(userId, "用户ID不能为空");
        if (queryRequest == null) {
            queryRequest = new PageDTO<>();
        }
        
        RedemptionQueryRequest params = queryRequest.getParams();
        if (params == null) {
            params = new RedemptionQueryRequest();
        }

        log.debug("获取用户 {} 的兑换记录", userId);

        // 构建查询条件
        LambdaQueryWrapper<Redemption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Redemption::getUserId, userId);

        // 状态筛选
        if (params.getStatus() != null) {
            wrapper.eq(Redemption::getStatus, params.getStatus());
        }

        // 时间范围筛选
        if (params.getStartTime() != null) {
            wrapper.ge(Redemption::getCreatedAt, params.getStartTime());
        }
        if (params.getEndTime() != null) {
            wrapper.le(Redemption::getCreatedAt, params.getEndTime());
        }

        // 分页查询
        Page<Redemption> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        wrapper.orderByDesc(Redemption::getCreatedAt);
        IPage<Redemption> result = this.page(page, wrapper);

        // 转换为VO
        List<RedemptionVO> voList = buildRedemptionVOList(result.getRecords());

        // 构造分页结果
        Page<RedemptionVO> resultPage = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(), result.getTotal());
        resultPage.setRecords(voList);

        return PageUtil.convert(resultPage);
    }

    @Override
    public RedemptionVO getRedemptionDetail(Long id, Long userId) {
        // 参数校验
        AssertUtils.notNull(id, "兑换记录ID不能为空");
        AssertUtils.notNull(userId, "用户ID不能为空");

        log.debug("获取兑换记录 {} 详情", id);

        // 查询兑换记录
        Redemption redemption = this.getById(id);
        AssertUtils.notNull(redemption, "兑换记录不存在");

        // 权限检查（只能查看自己的兑换记录，或者学工处可以查看所有）
        User currentUser = userMapper.selectById(userId);
        AssertUtils.notNull(currentUser, "用户不存在");
        
        if (!UserRole.ADMIN.equals(currentUser.getRole()) && !redemption.getUserId().equals(userId)) {
            AssertUtils.fail("无权限查看该兑换记录");
        }

        // 构建VO
        List<RedemptionVO> voList = buildRedemptionVOList(Collections.singletonList(redemption));
        return voList.isEmpty() ? null : voList.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyRedemption(String voucherCode, Long staffId) {
        // 参数校验
        AssertUtils.notEmpty(voucherCode, "凭证编号不能为空");
        AssertUtils.notNull(staffId, "核销人员ID不能为空");

        log.debug("核销凭证 {}，核销人员 {}", voucherCode, staffId);

        // 验证核销人员权限
        User staff = userMapper.selectById(staffId);
        AssertUtils.notNull(staff, "核销人员不存在");
        AssertUtils.isTrue(UserRole.ADMIN.equals(staff.getRole()), "只有学工处人员可以核销");

        // 查询兑换记录
        LambdaQueryWrapper<Redemption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Redemption::getVoucherCode, voucherCode);
        Redemption redemption = this.getOne(wrapper);
        AssertUtils.notNull(redemption, "凭证不存在");

        // 检查兑换状态
        AssertUtils.isTrue(RedemptionStatus.PENDING.getCode().equals(redemption.getStatus()), 
                getStatusErrorMessage(redemption.getStatus()));

        // 更新兑换状态
        redemption.setStatus(RedemptionStatus.VERIFIED.getCode());
        redemption.setVerifiedBy(staffId);
        redemption.setVerifiedAt(LocalDateTime.now());

        boolean updated = this.updateById(redemption);
        AssertUtils.isTrue(updated, "核销失败");

        log.info("凭证 {} 核销成功，核销人员 {}", voucherCode, staff.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRedemption(Long id, Long userId) {
        // 参数校验
        AssertUtils.notNull(id, "兑换记录ID不能为空");
        AssertUtils.notNull(userId, "用户ID不能为空");

        log.debug("取消兑换记录 {}，用户 {}", id, userId);

        // 查询兑换记录
        Redemption redemption = this.getById(id);
        AssertUtils.notNull(redemption, "兑换记录不存在");

        // 权限检查
        AssertUtils.isTrue(redemption.getUserId().equals(userId), "只能取消自己的兑换记录");

        // 检查状态（只能取消待领取的兑换）
        AssertUtils.isTrue(RedemptionStatus.PENDING.getCode().equals(redemption.getStatus()), 
                "只能取消待领取状态的兑换记录");

        // 查询商品信息
        Product product = productMapper.selectById(redemption.getProductId());
        AssertUtils.notNull(product, "商品不存在");

        // 退还积分
        pointsService.refundPoints(userId, redemption.getPointsSpent(), "取消兑换退还积分");

        // 恢复库存
        productMapper.increaseStock(redemption.getProductId());

        // 更新兑换状态
        redemption.setStatus(RedemptionStatus.CANCELLED.getCode());
        boolean updated = this.updateById(redemption);
        AssertUtils.isTrue(updated, "取消兑换失败");

        log.info("用户 {} 成功取消兑换记录 {}，退还积分 {}", userId, id, redemption.getPointsSpent());
    }

    @Override
    public PageVO<RedemptionVO> getAllRedemptions(PageDTO<RedemptionQueryRequest> queryRequest) {
        if (queryRequest == null) {
            queryRequest = new PageDTO<>();
        }
        
        RedemptionQueryRequest params = queryRequest.getParams();
        if (params == null) {
            params = new RedemptionQueryRequest();
        }

        log.debug("获取所有兑换记录，查询条件: {}", params);

        // 构建查询条件
        LambdaQueryWrapper<Redemption> wrapper = new LambdaQueryWrapper<>();

        // 用户筛选
        if (params.getUserId() != null) {
            wrapper.eq(Redemption::getUserId, params.getUserId());
        }

        // 商品筛选
        if (params.getProductId() != null) {
            wrapper.eq(Redemption::getProductId, params.getProductId());
        }

        // 状态筛选
        if (params.getStatus() != null) {
            wrapper.eq(Redemption::getStatus, params.getStatus());
        }

        // 时间范围筛选
        if (params.getStartTime() != null) {
            wrapper.ge(Redemption::getCreatedAt, params.getStartTime());
        }
        if (params.getEndTime() != null) {
            wrapper.le(Redemption::getCreatedAt, params.getEndTime());
        }

        // 用户姓名模糊搜索
        if (StringUtils.hasText(params.getUserName())) {
            List<Long> userIds = userMapper.selectList(
                    new LambdaQueryWrapper<User>()
                            .like(User::getName, params.getUserName())
                            .eq(User::getDeleted, 0)
            ).stream().map(User::getId).collect(Collectors.toList());
            
            if (userIds.isEmpty()) {
                // 没有匹配的用户，返回空结果
                Page<RedemptionVO> emptyPage = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(), 0);
                return PageUtil.convert(emptyPage);
            }
            wrapper.in(Redemption::getUserId, userIds);
        }

        // 商品名称模糊搜索
        if (StringUtils.hasText(params.getProductName())) {
            List<Long> productIds = productMapper.selectList(
                    new LambdaQueryWrapper<Product>()
                            .like(Product::getName, params.getProductName())
                            .eq(Product::getDeleted, 0)
            ).stream().map(Product::getId).collect(Collectors.toList());
            
            if (productIds.isEmpty()) {
                // 没有匹配的商品，返回空结果
                Page<RedemptionVO> emptyPage = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(), 0);
                return PageUtil.convert(emptyPage);
            }
            wrapper.in(Redemption::getProductId, productIds);
        }

        // 分页查询
        Page<Redemption> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        wrapper.orderByDesc(Redemption::getCreatedAt);
        IPage<Redemption> result = this.page(page, wrapper);

        // 转换为VO
        List<RedemptionVO> voList = buildRedemptionVOList(result.getRecords());

        // 构造分页结果
        Page<RedemptionVO> resultPage = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(), result.getTotal());
        resultPage.setRecords(voList);

        return PageUtil.convert(resultPage);
    }

    @Override
    public RedemptionVO getRedemptionByVoucherCode(String voucherCode) {
        // 参数校验
        AssertUtils.notEmpty(voucherCode, "凭证编号不能为空");

        log.debug("根据凭证编号 {} 查询兑换记录", voucherCode);

        // 查询兑换记录
        LambdaQueryWrapper<Redemption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Redemption::getVoucherCode, voucherCode);
        Redemption redemption = this.getOne(wrapper);
        AssertUtils.notNull(redemption, "凭证不存在");

        // 构建VO
        List<RedemptionVO> voList = buildRedemptionVOList(Collections.singletonList(redemption));
        return voList.isEmpty() ? null : voList.get(0);
    }

    @Override
    public String generateVoucherCode() {
        // 生成格式：MALL + 时间戳 + 4位随机数
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        String voucherCode = "MALL" + timestamp + random;

        // 检查唯一性，如果重复则重新生成
        LambdaQueryWrapper<Redemption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Redemption::getVoucherCode, voucherCode);
        if (this.count(wrapper) > 0) {
            // 递归重新生成
            return generateVoucherCode();
        }

        return voucherCode;
    }

    /**
     * 构建兑换记录VO列表
     */
    private List<RedemptionVO> buildRedemptionVOList(List<Redemption> redemptions) {
        if (redemptions == null || redemptions.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取用户信息
        Set<Long> userIds = redemptions.stream().map(Redemption::getUserId).collect(Collectors.toSet());
        Set<Long> verifierIds = redemptions.stream()
                .map(Redemption::getVerifiedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        userIds.addAll(verifierIds);

        Map<Long, User> userMap = userIds.isEmpty() ? new HashMap<>() :
                userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, userIds))
                        .stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

        // 获取商品信息
        Set<Long> productIds = redemptions.stream().map(Redemption::getProductId).collect(Collectors.toSet());
        Map<Long, Product> productMap = productIds.isEmpty() ? new HashMap<>() :
                productMapper.selectList(new LambdaQueryWrapper<Product>().in(Product::getId, productIds))
                        .stream()
                        .collect(Collectors.toMap(Product::getId, product -> product));

        // 构建VO列表
        return redemptions.stream().map(redemption -> {
            User user = userMap.get(redemption.getUserId());
            Product product = productMap.get(redemption.getProductId());
            User verifier = redemption.getVerifiedBy() != null ? userMap.get(redemption.getVerifiedBy()) : null;
            return buildRedemptionVO(redemption, user, product, verifier);
        }).collect(Collectors.toList());
    }

    /**
     * 构建单个兑换记录VO
     */
    private RedemptionVO buildRedemptionVO(Redemption redemption, User user, Product product, User verifier) {
        RedemptionVO vo = new RedemptionVO();
        vo.setId(redemption.getId());
        vo.setUserId(redemption.getUserId());
        vo.setProductId(redemption.getProductId());
        vo.setPointsSpent(redemption.getPointsSpent());
        vo.setVoucherCode(redemption.getVoucherCode());
        vo.setStatus(redemption.getStatus());
        vo.setVerifiedBy(redemption.getVerifiedBy());
        vo.setVerifiedAt(redemption.getVerifiedAt());
        vo.setCreatedAt(redemption.getCreatedAt());
        vo.setUpdatedAt(redemption.getUpdatedAt());

        // 设置状态文本
        RedemptionStatus status = RedemptionStatus.fromCode(redemption.getStatus());
        vo.setStatusText(status != null ? status.getDescription() : "未知状态");

        // 设置用户信息
        if (user != null) {
            vo.setUserName(user.getName());
            vo.setUserUsername(user.getUsername());
        }

        // 设置商品信息
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setProductImageUrl(product.getImageUrl());
        }

        // 设置核销人员信息
        if (verifier != null) {
            vo.setVerifiedByName(verifier.getName());
        }

        // 设置是否可以取消兑换
        vo.setCanCancel(RedemptionStatus.PENDING.getCode().equals(redemption.getStatus()));

        return vo;
    }

    /**
     * 获取状态错误信息
     */
    private String getStatusErrorMessage(Integer status) {
        RedemptionStatus redemptionStatus = RedemptionStatus.fromCode(status);
        if (redemptionStatus == null) {
            return "兑换状态异常";
        }
        
        switch (redemptionStatus) {
            case VERIFIED:
                return "该凭证已被使用";
            case CANCELLED:
                return "该兑换已被取消";
            default:
                return "兑换状态异常";
        }
    }

    @Override
    public PageVO<RedemptionVO> getRedemptionsByStatus(PageDTO<RedemptionQueryRequest> pageRequest, String statusType) {
        // 参数校验
        AssertUtils.notNull(pageRequest, "分页请求不能为空");

        log.debug("根据状态类型查询兑换记录，statusType: {}", statusType);

        // 构建分页对象
        Page<Redemption> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Redemption> wrapper = new LambdaQueryWrapper<>();
        
        // 根据状态类型筛选
        if (StringUtils.hasText(statusType)) {
            switch (statusType.toUpperCase()) {
                case "TODAY":
                    // 今日核销：状态为已核销，且核销时间在今天
                    LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
                    LocalDateTime todayEnd = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
                    wrapper.eq(Redemption::getStatus, RedemptionStatus.VERIFIED.getCode())
                           .between(Redemption::getVerifiedAt, todayStart, todayEnd);
                    break;
                case "VERIFIED":
                    // 累计核销：状态为已核销
                    wrapper.eq(Redemption::getStatus, RedemptionStatus.VERIFIED.getCode());
                    break;
                case "PENDING":
                    // 待核销：状态为待核销
                    wrapper.eq(Redemption::getStatus, RedemptionStatus.PENDING.getCode());
                    break;
                default:
                    log.warn("未知的状态类型: {}", statusType);
                    break;
            }
        }

        // 其他查询条件
        RedemptionQueryRequest params = pageRequest.getParams();
        if (params != null) {
            // 用户名搜索
            if (StringUtils.hasText(params.getUserName())) {
                List<User> users = userMapper.selectList(
                    new LambdaQueryWrapper<User>()
                        .like(User::getName, params.getUserName())
                );
                if (!users.isEmpty()) {
                    Set<Long> userIds = users.stream().map(User::getId).collect(Collectors.toSet());
                    wrapper.in(Redemption::getUserId, userIds);
                } else {
                    // 没有匹配的用户，返回空结果
                    return PageUtil.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
                }
            }

            // 商品名搜索
            if (StringUtils.hasText(params.getProductName())) {
                List<Product> products = productMapper.selectList(
                    new LambdaQueryWrapper<Product>()
                        .like(Product::getName, params.getProductName())
                );
                if (!products.isEmpty()) {
                    Set<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toSet());
                    wrapper.in(Redemption::getProductId, productIds);
                } else {
                    // 没有匹配的商品，返回空结果
                    return PageUtil.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
                }
            }

            // 状态筛选（如果没有使用statusType）
            if (!StringUtils.hasText(statusType) && params.getStatus() != null) {
                wrapper.eq(Redemption::getStatus, params.getStatus());
            }
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(Redemption::getCreatedAt);

        // 执行查询
        IPage<Redemption> result = this.page(page, wrapper);

        // 构建VO列表
        List<RedemptionVO> voList = buildRedemptionVOList(result.getRecords());

        // 构造分页结果
        Page<RedemptionVO> resultPage = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize(), result.getTotal());
        resultPage.setRecords(voList);

        return PageUtil.convert(resultPage);
    }
}