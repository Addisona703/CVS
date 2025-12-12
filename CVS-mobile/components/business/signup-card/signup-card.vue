<template>
  <view class="signup-card" @click="handleClick">
    <view class="card-header">
      <view class="activity-title">{{ signup.activityTitle }}</view>
      <view class="status-badge" :class="statusClass">{{ statusText }}</view>
    </view>
    
    <view class="card-body">
      <view class="info-item">
        <text class="label">报名时间：</text>
        <text class="value">{{ formatDate(signup.createdAt) }}</text>
      </view>
      
      <view v-if="signup.reason" class="info-item">
        <text class="label">报名理由：</text>
        <text class="value">{{ signup.reason }}</text>
      </view>
      
      <view v-if="signup.reviewComment" class="info-item">
        <text class="label">审核意见：</text>
        <text class="value">{{ signup.reviewComment }}</text>
      </view>
      
      <view v-if="signup.reviewedAt" class="info-item">
        <text class="label">审核时间：</text>
        <text class="value">{{ formatDate(signup.reviewedAt) }}</text>
      </view>
    </view>
    
    <view v-if="showActions" class="card-footer">
      <slot name="actions">
        <button 
          v-if="canCancel" 
          class="action-btn danger" 
          @click.stop="handleCancel"
        >
          取消报名
        </button>
        <button 
          v-if="canApprove" 
          class="action-btn success" 
          @click.stop="handleApprove"
        >
          通过
        </button>
        <button 
          v-if="canReject" 
          class="action-btn danger" 
          @click.stop="handleReject"
        >
          拒绝
        </button>
      </slot>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { formatDateTime } from '@/utils/format'

const props = defineProps({
  signup: {
    type: Object,
    required: true
  },
  showActions: {
    type: Boolean,
    default: false
  },
  canCancel: {
    type: Boolean,
    default: false
  },
  canApprove: {
    type: Boolean,
    default: false
  },
  canReject: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click', 'cancel', 'approve', 'reject'])

const statusClass = computed(() => {
  const statusMap = {
    'PENDING': 'status-pending',
    'APPROVED': 'status-approved',
    'REJECTED': 'status-rejected',
    'CANCELLED': 'status-cancelled'
  }
  return statusMap[props.signup.status] || ''
})

const statusText = computed(() => {
  const textMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消'
  }
  return textMap[props.signup.status] || '未知'
})

const formatDate = (dateStr) => {
  return formatDateTime(dateStr, 'YYYY-MM-DD HH:mm')
}

const handleClick = () => {
  emit('click', props.signup)
}

const handleCancel = () => {
  emit('cancel', props.signup)
}

const handleApprove = () => {
  emit('approve', props.signup)
}

const handleReject = () => {
  emit('reject', props.signup)
}
</script>

<style scoped>
.signup-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.activity-title {
  flex: 1;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.status-badge {
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  margin-left: 16rpx;
  white-space: nowrap;
}

.status-pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-approved {
  background: #f6ffed;
  color: #52c41a;
}

.status-rejected {
  background: #fff1f0;
  color: #ff4d4f;
}

.status-cancelled {
  background: #f5f5f5;
  color: #999;
}

.card-body {
  margin-bottom: 20rpx;
}

.info-item {
  display: flex;
  margin-bottom: 16rpx;
  font-size: 28rpx;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #999;
  white-space: nowrap;
}

.value {
  flex: 1;
  color: #333;
  word-break: break-all;
}

.card-footer {
  display: flex;
  gap: 16rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.action-btn {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: 1rpx solid #d9d9d9;
  background: #fff;
  color: #333;
}

.action-btn.success {
  background: #52c41a;
  color: #fff;
  border-color: #52c41a;
}

.action-btn.danger {
  color: #ff4d4f;
  border-color: #ff4d4f;
}
</style>
