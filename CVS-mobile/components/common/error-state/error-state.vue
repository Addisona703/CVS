<template>
  <view class="error-state">
    <view class="error-icon">{{ icon }}</view>
    <view class="error-text">{{ text }}</view>
    <view v-if="description" class="error-description">{{ description }}</view>
    <button v-if="showRetry" class="retry-btn" @click="handleRetry">
      {{ retryText }}
    </button>
    <slot name="action"></slot>
  </view>
</template>

<script setup>
const props = defineProps({
  icon: {
    type: String,
    default: '❌'
  },
  text: {
    type: String,
    default: '加载失败'
  },
  description: {
    type: String,
    default: ''
  },
  showRetry: {
    type: Boolean,
    default: true
  },
  retryText: {
    type: String,
    default: '重试'
  }
})

const emit = defineEmits(['retry'])

const handleRetry = () => {
  emit('retry')
}
</script>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
}

.error-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.error-text {
  font-size: 32rpx;
  color: #ff4d4f;
  margin-bottom: 16rpx;
}

.error-description {
  font-size: 28rpx;
  color: #999;
  text-align: center;
  line-height: 1.6;
  margin-bottom: 32rpx;
}

.retry-btn {
  width: 240rpx;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  background: #0052d9;
  color: #fff;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: none;
}
</style>
