<template>
  <view class="loading-state">
    <view v-if="type === 'spinner'" class="spinner-container">
      <view class="spinner"></view>
      <view v-if="text" class="loading-text">{{ text }}</view>
    </view>
    
    <view v-else-if="type === 'skeleton'" class="skeleton-container">
      <view v-for="i in rows" :key="i" class="skeleton-row">
        <view class="skeleton-item" :style="{ width: getWidth(i) }"></view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  type: {
    type: String,
    default: 'spinner', // spinner | skeleton
    validator: (value) => ['spinner', 'skeleton'].includes(value)
  },
  text: {
    type: String,
    default: '加载中...'
  },
  rows: {
    type: Number,
    default: 3
  }
})

const getWidth = (index) => {
  const widths = ['100%', '80%', '60%']
  return widths[index % widths.length]
}
</script>

<style scoped>
.loading-state {
  padding: 40rpx;
}

.spinner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
}

.spinner {
  width: 80rpx;
  height: 80rpx;
  border: 4rpx solid #f0f0f0;
  border-top-color: #0052d9;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: #999;
}

.skeleton-container {
  padding: 24rpx 0;
}

.skeleton-row {
  margin-bottom: 24rpx;
}

.skeleton-item {
  height: 32rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 8rpx;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
