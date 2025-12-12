<template>
  <view class="record-card" @click="handleClick">
    <view class="card-header">
      <view class="activity-info">
        <view class="activity-title">{{ record.activityTitle || '未知活动' }}</view>
        <view class="activity-date">{{ formatDate(record.checkInTime || record.createdAt) }}</view>
      </view>
      <view class="points">+{{ record.pointsEarned || record.points || 0 }}</view>
    </view>
    
    <view class="card-body">
      <view class="info-row">
        <view class="info-item">
          <text class="info-icon">⏱️</text>
          <text class="info-text">{{ formatDuration(record.durationMinutes || record.duration) }}</text>
        </view>
        <view v-if="record.rating" class="info-item">
          <text class="info-icon">⭐</text>
          <text class="info-text">{{ record.rating }}分</text>
        </view>
      </view>
      
      <view v-if="record.description" class="description-section">
        <text class="description-label">服务描述：</text>
        <text class="description-content">{{ record.description }}</text>
      </view>
      
      <view v-if="record.evaluation" class="comment-section">
        <text class="comment-label">教师评价：</text>
        <text class="comment-content">{{ record.evaluation }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { formatDateTime, formatDuration } from '@/utils/format'

const props = defineProps({
  record: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

const formatDate = (dateStr) => {
  return formatDateTime(dateStr, 'YYYY-MM-DD HH:mm')
}

const handleClick = () => {
  emit('click', props.record)
}
</script>

<style scoped>
.record-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  transform: translateZ(0);
  will-change: transform;
  transition: all 0.3s ease;
}

.record-card:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.activity-info {
  flex: 1;
  min-width: 0;
}

.activity-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-date {
  font-size: 24rpx;
  color: #999;
}

.points {
  padding: 10rpx 20rpx;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
  color: #fa8c16;
  margin-left: 16rpx;
  white-space: nowrap;
  flex-shrink: 0;
}

.card-body {
  font-size: 28rpx;
}

.info-row {
  display: flex;
  gap: 32rpx;
  margin-bottom: 16rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.info-icon {
  font-size: 28rpx;
}

.info-text {
  font-size: 26rpx;
  color: #666;
  font-weight: 500;
}

.description-section,
.comment-section {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f5f5f5;
}

.description-label,
.comment-label {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.description-content,
.comment-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.comment-content {
  color: #0052d9;
}
</style>
