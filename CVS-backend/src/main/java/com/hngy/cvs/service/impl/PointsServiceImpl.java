package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.PointsSearchDTO;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.PointsRecordVO;
import com.hngy.cvs.dto.response.PointsStatsVO;
import com.hngy.cvs.dto.response.PointsRankingVO;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.Points;
import com.hngy.cvs.entity.User;
import com.hngy.cvs.entity.RecordEntity;
import com.hngy.cvs.entity.enums.UserRole;
import com.hngy.cvs.mapper.ActivityMapper;
import com.hngy.cvs.mapper.PointsMapper;
import com.hngy.cvs.mapper.RecordMapper;
import com.hngy.cvs.mapper.UserMapper;
import com.hngy.cvs.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 积分服务实现类
 *
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsServiceImpl
        extends ServiceImpl<PointsMapper, Points>
        implements PointsService {

    private final RecordMapper recordMapper;
    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;

    @Override
    public PointsStatsVO getCurrentUserPointsStats(Long userId) {
        // 参数校验
        AssertUtils.notNull(userId, "用户ID不能为空");

        // 验证用户存在
        User user = userMapper.selectById(userId);
        AssertUtils.notNull(user, "用户不存在");
        AssertUtils.isFalse(user.getDeleted() == 1, "用户已被删除");

        log.debug("获取用户 {} 的积分统计信息", userId);

        PointsStatsVO stats = new PointsStatsVO();
        stats.setUserId(userId);
        stats.setUserName(user.getName());

        // 获取服务记录积分
        LambdaQueryWrapper<RecordEntity> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(RecordEntity::getUserId, userId);
        List<RecordEntity> records = recordMapper.selectList(recordWrapper);
        long servicePoints = records.stream()
                .mapToLong(record -> record.getPointsEarned() != null ? record.getPointsEarned() : 0)
                .sum();
        stats.setServicePoints(servicePoints);

        // 获取用户当前总积分（积分表存储的是用户总积分）
        Points userPoints = this.getOne(
                new LambdaQueryWrapper<Points>().eq(Points::getUserId, userId)
        );
        long totalPoints = userPoints != null && userPoints.getPoints() != null
                ? userPoints.getPoints().longValue()
                : servicePoints;
        stats.setTotalPoints(totalPoints);

        long adjustmentPoints = totalPoints - servicePoints;
        if (adjustmentPoints < 0) {
            adjustmentPoints = 0L;
        }
        stats.setAdjustmentPoints(adjustmentPoints);

        // 获取用户排名（仅对学生用户计算排名）
        if (UserRole.STUDENT.equals(user.getRole())) {
            Long ranking = calculateUserRanking(userId);
            stats.setCurrentRanking(ranking);
        } else {
            stats.setCurrentRanking(null);
        }

        // 获取服务记录统计
        Long recordsCount = (long) records.size();
        stats.setServiceRecordsCount(recordsCount);

        // 获取总服务时长（小时）
        long totalMinutes = records.stream()
                .mapToLong(record -> record.getDurationMinutes() != null ? record.getDurationMinutes() : 0)
                .sum();
        Double totalHours = totalMinutes / 60.0;
        stats.setTotalServiceHours(totalHours);

        return stats;
    }

    @Override
    public PageVO<PointsRankingVO> getPointsRankingPage(int page, int size) {
        // 参数校验
        AssertUtils.isTrue(page > 0, "页码必须大于0");
        AssertUtils.isTrue(size > 0, "每页大小必须大于0");
        AssertUtils.isTrue(size <= 100, "每页大小不能超过100");

        log.debug("获取积分排行榜，页码: {}, 每页大小: {}", page, size);

        // 获取所有学生用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRole, UserRole.STUDENT)
                   .eq(User::getDeleted, 0);
        List<User> students = userMapper.selectList(userWrapper);

        // 计算每个学生的总积分并排序
        List<PointsRankingVO> rankings = new ArrayList<>();
        for (User student : students) {
            PointsRankingVO ranking = new PointsRankingVO();
            ranking.setUserId(student.getId());
            ranking.setUsername(student.getUsername());
            ranking.setName(student.getName());

            // 计算总积分
            Long totalPoints = calculateUserTotalPoints(student.getId());
            ranking.setTotalPoints(totalPoints);

            rankings.add(ranking);
        }

        // 按积分降序排序并设置排名
        rankings.sort((a, b) -> Long.compare(b.getTotalPoints(), a.getTotalPoints()));
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRanking((long) (i + 1));
        }

        // 手动分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, rankings.size());
        List<PointsRankingVO> pageData = rankings.subList(start, end);

        // 构造分页结果
        Page<PointsRankingVO> resultPage = new Page<>(page, size, rankings.size());
        resultPage.setRecords(pageData);

        return PageUtil.convert(resultPage);
    }

    @Override
    public PageVO<PointsRecordVO> getAllPointsRecords(PageDTO<PointsSearchDTO> pageRequest) {
        // 参数校验
        AssertUtils.notNull(pageRequest, "分页请求参数不能为空");
        AssertUtils.isTrue(pageRequest.getPageNum() > 0, "页码必须大于0");
        AssertUtils.isTrue(pageRequest.getPageSize() > 0, "每页大小必须大于0");
        AssertUtils.isTrue(pageRequest.getPageSize() <= 100, "每页大小不能超过100");

        PointsSearchDTO searchParams = Optional.ofNullable(pageRequest.getParams()).orElseGet(PointsSearchDTO::new);

        log.debug("获取所有用户积分记录，页码: {}, 每页大小: {}, 搜索条件: {}",
                pageRequest.getPageNum(), pageRequest.getPageSize(), searchParams);

        // 构建查询条件
        LambdaQueryWrapper<RecordEntity> wrapper = new LambdaQueryWrapper<>();
        
        // 用户筛选
        if (StringUtils.hasText(searchParams.getName()) || StringUtils.hasText(searchParams.getUsername())) {
            List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                            .eq(User::getDeleted, 0)
                            .like(StringUtils.hasText(searchParams.getName()), User::getName, searchParams.getName())
                            .like(StringUtils.hasText(searchParams.getUsername()), User::getUsername, searchParams.getUsername()))
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            if (userIds.isEmpty()) {
                return PageUtil.convert(new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize(), 0));
            }
            wrapper.in(RecordEntity::getUserId, userIds);
        }

        // 积分范围过滤
        wrapper.ge(searchParams.getMinPoints() != null, RecordEntity::getPointsEarned, searchParams.getMinPoints())
               .le(searchParams.getMaxPoints() != null, RecordEntity::getPointsEarned, searchParams.getMaxPoints());

        // 分页查询服务记录
        Page<RecordEntity> recordPage = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        wrapper.orderByDesc(RecordEntity::getCreatedAt);
        IPage<RecordEntity> recordResult = recordMapper.selectPage(recordPage, wrapper);

        // 转换为 VO
        List<RecordEntity> records = recordResult.getRecords();
        Set<Long> userIds = records.stream().map(RecordEntity::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, userIds))
                        .stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

        Set<Long> activityIds = records.stream().map(RecordEntity::getActivityId).collect(Collectors.toSet());
        Map<Long, Activity> activityMap = activityIds.isEmpty() ? Collections.emptyMap() :
                activityMapper.selectList(new LambdaQueryWrapper<Activity>().in(Activity::getId, activityIds))
                        .stream()
                        .collect(Collectors.toMap(Activity::getId, activity -> activity));

        List<PointsRecordVO> voList = records.stream().map(record -> {
            PointsRecordVO vo = new PointsRecordVO();
            vo.setId(record.getId());
            vo.setUserId(record.getUserId());
            vo.setPoints(record.getPointsEarned());
            vo.setPointsSource("SERVICE");
            vo.setDurationMinutes(record.getDurationMinutes());
            vo.setRating(record.getRating());
            vo.setCreatedAt(record.getCreatedAt());

            User user = userMap.get(record.getUserId());
            if (user != null) {
                vo.setName(user.getName());
                vo.setUsername(user.getUsername());
            }

            Activity activity = activityMap.get(record.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
            }

            vo.setTotalPoints(calculateUserTotalPoints(record.getUserId()));
            return vo;
        }).collect(Collectors.toList());

        // 构造分页结果
        Page<PointsRecordVO> resultPage = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize(), recordResult.getTotal());
        resultPage.setRecords(voList);

        return PageUtil.convert(resultPage);
    }

    @Override
    @Transactional
    public void awardPoints(Long userId, Integer points) {
        // 参数校验
        AssertUtils.notNull(userId, "用户ID不能为空");
        AssertUtils.notNull(points, "积分数量不能为空");

        // 验证用户存在性和有效性
        User user = userMapper.selectById(userId);
        AssertUtils.notNull(user, "用户不存在");
        AssertUtils.isFalse(user.getDeleted() == 1, "用户已被删除");

        // 查询用户是否已有积分记录
        Points existingPoints = this.getOne(
            new LambdaQueryWrapper<Points>().eq(Points::getUserId, userId)
        );

        if (existingPoints != null) {
            // 更新现有积分
            existingPoints.setPoints(existingPoints.getPoints() + points);
            boolean success = this.updateById(existingPoints);
            AssertUtils.isTrue(success, "积分更新失败");
        } else {
            // 创建新的积分记录
            Points pointsRecord = new Points();
            pointsRecord.setUserId(userId);
            pointsRecord.setPoints(points);
            boolean success = this.save(pointsRecord);
            AssertUtils.isTrue(success, "积分发放失败");
        }

        log.info("成功为用户 {} 发放积分 {}", userId, points);
    }

    @Override
    public Integer getUserTotalPoints(Long userId) {
        // 参数校验
        AssertUtils.notNull(userId, "用户ID不能为空");

        // 验证用户存在
        User user = userMapper.selectById(userId);
        AssertUtils.notNull(user, "用户不存在");
        AssertUtils.isFalse(user.getDeleted() == 1, "用户已被删除");

        log.debug("获取用户 {} 的总积分", userId);

        // 查询用户积分记录
        Points userPoints = this.getOne(
            new LambdaQueryWrapper<Points>().eq(Points::getUserId, userId)
        );

        if (userPoints != null && userPoints.getPoints() != null) {
            return userPoints.getPoints();
        }

        // 如果没有积分记录，返回0
        return 0;
    }

    @Override
    @Transactional
    public void deductPoints(Long userId, Integer points, String reason) {
        // 参数校验
        AssertUtils.notNull(userId, "用户ID不能为空");
        AssertUtils.notNull(points, "积分数量不能为空");
        AssertUtils.isTrue(points > 0, "积分数量必须大于0");

        // 验证用户存在
        User user = userMapper.selectById(userId);
        AssertUtils.notNull(user, "用户不存在");
        AssertUtils.isFalse(user.getDeleted() == 1, "用户已被删除");

        log.debug("扣除用户 {} 积分 {}，原因: {}", userId, points, reason);

        // 获取用户当前积分
        Integer currentPoints = getUserTotalPoints(userId);
        AssertUtils.isTrue(currentPoints >= points, "积分不足，当前积分: " + currentPoints + "，需要扣除: " + points);

        // 查询用户积分记录
        Points userPoints = this.getOne(
            new LambdaQueryWrapper<Points>().eq(Points::getUserId, userId)
        );

        if (userPoints != null) {
            // 更新现有积分
            userPoints.setPoints(userPoints.getPoints() - points);
            boolean success = this.updateById(userPoints);
            AssertUtils.isTrue(success, "积分扣除失败");
        } else {
            // 创建新的积分记录（负数表示扣除）
            Points pointsRecord = new Points();
            pointsRecord.setUserId(userId);
            pointsRecord.setPoints(-points);
            boolean success = this.save(pointsRecord);
            AssertUtils.isTrue(success, "积分扣除失败");
        }

        log.info("成功扣除用户 {} 积分 {}，原因: {}", userId, points, reason);
    }

    @Override
    @Transactional
    public void refundPoints(Long userId, Integer points, String reason) {
        // 参数校验
        AssertUtils.notNull(userId, "用户ID不能为空");
        AssertUtils.notNull(points, "积分数量不能为空");
        AssertUtils.isTrue(points > 0, "积分数量必须大于0");

        // 验证用户存在
        User user = userMapper.selectById(userId);
        AssertUtils.notNull(user, "用户不存在");
        AssertUtils.isFalse(user.getDeleted() == 1, "用户已被删除");

        log.debug("退还用户 {} 积分 {}，原因: {}", userId, points, reason);

        // 查询用户积分记录
        Points userPoints = this.getOne(
            new LambdaQueryWrapper<Points>().eq(Points::getUserId, userId)
        );

        if (userPoints != null) {
            // 更新现有积分
            userPoints.setPoints(userPoints.getPoints() + points);
            boolean success = this.updateById(userPoints);
            AssertUtils.isTrue(success, "积分退还失败");
        } else {
            // 创建新的积分记录
            Points pointsRecord = new Points();
            pointsRecord.setUserId(userId);
            pointsRecord.setPoints(points);
            boolean success = this.save(pointsRecord);
            AssertUtils.isTrue(success, "积分退还失败");
        }

        log.info("成功退还用户 {} 积分 {}，原因: {}", userId, points, reason);
    }

    /**
     * 计算用户总积分
     */
    private Long calculateUserTotalPoints(Long userId) {
        // 服务记录积分
        Points userPoints = this.getOne(
                new LambdaQueryWrapper<Points>().eq(Points::getUserId, userId)
        );
        if (userPoints != null && userPoints.getPoints() != null) {
            return userPoints.getPoints().longValue();
        }

        LambdaQueryWrapper<RecordEntity> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(RecordEntity::getUserId, userId);
        List<RecordEntity> records = recordMapper.selectList(recordWrapper);
        return records.stream()
                .mapToLong(record -> record.getPointsEarned() != null ? record.getPointsEarned() : 0)
                .sum();
    }

    /**
     * 计算用户排名
     */
    private Long calculateUserRanking(Long userId) {
        // 获取所有学生用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRole, UserRole.STUDENT)
                   .eq(User::getDeleted, 0);
        List<User> students = userMapper.selectList(userWrapper);

        // 计算每个学生的总积分
        List<Long> totalPointsList = new ArrayList<>();
        Long currentUserPoints = null;
        
        for (User student : students) {
            Long totalPoints = calculateUserTotalPoints(student.getId());
            totalPointsList.add(totalPoints);
            
            if (student.getId().equals(userId)) {
                currentUserPoints = totalPoints;
            }
        }

        if (currentUserPoints == null) {
            return null;
        }

        // 计算排名（比当前用户积分高的人数 + 1）
        Long finalCurrentUserPoints = currentUserPoints;

        return totalPointsList.stream()
                .mapToLong(points -> points)
                .filter(points -> points > finalCurrentUserPoints)
                .count() + 1;
    }
}
