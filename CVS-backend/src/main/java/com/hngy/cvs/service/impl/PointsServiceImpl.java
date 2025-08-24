package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.cvs.dto.response.PointsRecordVO;
import com.hngy.cvs.entity.Points;
import com.hngy.cvs.mapper.PointsMapper;
import com.hngy.cvs.mapper.RecordMapper;
import com.hngy.cvs.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 积分服务实现类
 * 
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final PointsMapper pointsMapper;
    private final RecordMapper recordMapper;

    @Override
    @Transactional
    public void adjustUserPoints(Long userId, Integer points) {
        Points pointsRecord = new Points();
        pointsRecord.setUserId(userId);
        pointsRecord.setPoints(points);

        pointsMapper.insert(pointsRecord);
        log.info("用户 {} 手动调整积分: {}", userId, points);
    }

    @Override
    public IPage<PointsRecordVO> getUserPointsRecords(Long userId, int page, int size) {
        Page<PointsRecordVO> pointsPage = new Page<>(page, size);
        return pointsMapper.selectMyPoints(pointsPage, userId);
    }

    @Override
    public Long getUserTotalPoints(Long userId) {
        // 计算用户总积分：服务记录积分 + 手动调整积分
        Long recordPoints = recordMapper.sumPointsByUser(userId);
        Long adjustmentPoints = pointsMapper.sumPointsByUser(userId);

        return (recordPoints != null ? recordPoints : 0L) + (adjustmentPoints != null ? adjustmentPoints : 0L);
    }

    @Override
    public List<PointsRecordVO> getPointsRanking(int limit) {
        return pointsMapper.selectPointsRanking(limit);
    }

    @Override
    public Long getUserPointsRanking(Long userId) {
        return pointsMapper.getUserPointsRanking(userId);
    }



    @Override
    public Long countPointsRecords() {
        return pointsMapper.selectCount(null);
    }
}
