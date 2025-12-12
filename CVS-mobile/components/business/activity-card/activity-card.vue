<template>
  <view class="activity-card" @click="handleClick">
    <view class="card-header">
      <view class="title">{{ activity.title }}</view>
      <view class="status-badge" :class="statusClass">{{ statusText }}</view>
    </view>
    
    <view class="card-body">
      <view class="info-item">
        <ph-icon name="calendar" :size="32" color="#0052d9" class="icon" />
        <text class="text">{{ formatDate(activity.startTime) }} - {{ formatDate(activity.endTime) }}</text>
      </view>
      
      <view class="info-item">
        <ph-icon name="map-pin" :size="32" color="#FF8C42" class="icon" />
        <text class="text">{{ activity.location }}</text>
      </view>
      
      <view class="info-item">
        <ph-icon name="users" :size="32" color="#7BC96F" class="icon" />
        <text class="text">{{ activity.currentSignups || 0 }}/{{ activity.maxParticipants }} 人</text>
      </view>
      
      <view v-if="activity.points" class="info-item">
        <ph-icon name="star" :size="32" color="#FFB800" weight="fill" class="icon" />
        <text class="text">{{ activity.points }} 积分</text>
      </view>
    </view>
    
    <view v-if="showActions" class="card-footer">
      <slot name="actions">
        <button 
          v-if="canSignup" 
          class="action-btn primary" 
          @click.stop="handleSignup"
        >
          报名
        </button>
        <button 
          v-if="canEdit" 
          class="action-btn" 
          @click.stop="handleEdit"
        >
          编辑
        </button>
        <button 
          v-if="canDelete" 
          class="action-btn danger" 
          @click.stop="handleDelete"
        >
          删除
        </button>
      </slot>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { formatDateTime } from '@/utils/format'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'

const props = defineProps({
  activity: {
    type: Object,
    required: true
  },
  showActions: {
    type: Boolean,
    default: false
  },
  canSignup: {
    type: Boolean,
    default: false
  },
  canEdit: {
    type: Boolean,
    default: false
  },
  canDelete: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click', 'signup', 'edit', 'delete'])

const statusClass = computed(() => {
  const statusMap = {
    'DRAFT': 'status-draft',
    'PUBLISHED': 'status-published',
    'ONGOING': 'status-ongoing',
    'COMPLETED': 'status-completed',
    'CANCELLED': 'status-cancelled'
  }
  return statusMap[props.activity.status] || ''
})

const statusText = computed(() => {
  const textMap = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'ONGOING': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return textMap[props.activity.status] || '未知'
})

const formatDate = (dateStr) => {
  return formatDateTime(dateStr, 'MM-DD HH:mm')
}

const handleClick = () => {
  emit('click', props.activity)
}

const handleSignup = () => {
  emit('signup', props.activity)
}

const handleEdit = () => {
  emit('edit', props.activity)
}

const handleDelete = () => {
  emit('delete', props.activity)
}
</script>

<style scoped>
.activity-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.activity-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 82, 217, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24rpx;
}

.title {
  flex: 1;
  font-size: 34rpx;
  font-weight: 700;
  color: #2D3748;
  line-height: 1.4;
}

.status-badge {
  padding: 10rpx 18rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
  margin-left: 16rpx;
  white-space: nowrap;
  font-weight: 500;
}

.status-draft {
  background: #EDF2F7;
  color: #718096;
}

.status-published {
  background: #E8F5E6;
  color: #7BC96F;
}

.status-ongoing {
  background: linear-gradient(135deg, #E6F0FF 0%, #E8F5E6 100%);
  background: linear-gradient(135deg, #0052d9 0%, #7BC96F 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 600;
}

.status-completed {
  background: #EDF2F7;
  color: #718096;
}

.status-cancelled {
  background: #FFF5F5;
  color: #FF6B6B;
}

.card-body {
  margin-bottom: 24rpx;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 18rpx;
  font-size: 28rpx;
  color: #718096;
}

.info-item:last-child {
  margin-bottom: 0;
}

.icon {
  margin-right: 12rpx;
  flex-shrink: 0;
}

.text {
  flex: 1;
  font-weight: 500;
}

.card-footer {
  display: flex;
  gap: 16rpx;
  padding-top: 24rpx;
  border-top: 2rpx solid #E2E8F0;
}

.action-btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
  transition: all 0.3s ease;
}

.action-btn {
  background: #fff;
  color: #718096;
  border: 2rpx solid #E2E8F0;
}

.action-btn:active {
  background: #F7FAFC;
}

.action-btn.primary {
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
  color: #fff;
  border: none;
  box-shadow: 0 4rpx 16rpx rgba(0, 82, 217, 0.3);
}

.action-btn.primary:active {
  transform: translateY(2rpx);
  box-shadow: 0 2rpx 8rpx rgba(0, 82, 217, 0.3);
}

.action-btn.danger {
  color: #FF6B6B;
  border-color: #FF6B6B;
}

.action-btn.danger:active {
  background: #FFF5F5;
}
</style>
