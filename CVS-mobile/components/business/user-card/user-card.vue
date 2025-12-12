<template>
  <view class="user-card" @click="handleClick">
    <view class="user-info">
      <view class="avatar">
        <text class="avatar-text">{{ avatarText }}</text>
      </view>
      
      <view class="info">
        <view class="name-row">
          <text class="name">{{ user.realName || user.username }}</text>
          <view class="role-badge" :class="roleClass">{{ roleText }}</view>
        </view>
        
        <view class="detail-row">
          <text class="username">@{{ user.username }}</text>
        </view>
        
        <view v-if="user.email" class="detail-row">
          <text class="email">{{ user.email }}</text>
        </view>
      </view>
    </view>
    
    <view v-if="showActions" class="card-actions">
      <slot name="actions">
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

const props = defineProps({
  user: {
    type: Object,
    required: true
  },
  showActions: {
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

const emit = defineEmits(['click', 'edit', 'delete'])

const avatarText = computed(() => {
  const name = props.user.realName || props.user.username
  return name ? name.charAt(0).toUpperCase() : 'U'
})

const roleClass = computed(() => {
  const roleMap = {
    'STUDENT': 'role-student',
    'TEACHER': 'role-teacher',
    'ADMIN': 'role-admin'
  }
  return roleMap[props.user.role] || ''
})

const roleText = computed(() => {
  const textMap = {
    'STUDENT': '学生',
    'TEACHER': '教师',
    'ADMIN': '学工处'
  }
  return textMap[props.user.role] || '未知'
})

const handleClick = () => {
  emit('click', props.user)
}

const handleEdit = () => {
  emit('edit', props.user)
}

const handleDelete = () => {
  emit('delete', props.user)
}
</script>

<style scoped>
.user-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.avatar-text {
  font-size: 40rpx;
  font-weight: 600;
  color: #fff;
}

.info {
  flex: 1;
  overflow: hidden;
}

.name-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-right: 12rpx;
}

.role-badge {
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  font-size: 22rpx;
}

.role-student {
  background: #e6f7ff;
  color: #1890ff;
}

.role-teacher {
  background: #f6ffed;
  color: #52c41a;
}

.role-admin {
  background: #fff7e6;
  color: #fa8c16;
}

.detail-row {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.card-actions {
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

.action-btn.danger {
  color: #ff4d4f;
  border-color: #ff4d4f;
}
</style>
