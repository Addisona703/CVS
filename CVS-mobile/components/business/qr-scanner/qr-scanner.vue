<template>
  <view class="qr-scanner">
    <view class="scanner-header">
      <text class="title">{{ title }}</text>
      <text class="tip">{{ tip }}</text>
    </view>
    
    <view class="scanner-body">
      <button class="scan-btn" @click="handleScan">
        <text class="scan-icon">📷</text>
        <text class="scan-text">点击扫描二维码</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: '扫描二维码'
  },
  tip: {
    type: String,
    default: '将二维码放入框内，即可自动扫描'
  },
  continuous: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['success', 'fail'])

const scanning = ref(false)

const handleScan = async () => {
  if (scanning.value) return
  
  scanning.value = true
  
  try {
    const res = await uni.scanCode({
      scanType: ['qrCode'],
      autoDecodeCharSet: true
    })
    
    if (res.result) {
      emit('success', res.result)
      
      if (props.continuous) {
        setTimeout(() => {
          handleScan()
        }, 1000)
      }
    }
  } catch (error) {
    if (error.errMsg && !error.errMsg.includes('cancel')) {
      emit('fail', error)
      uni.showToast({
        title: '扫描失败',
        icon: 'none'
      })
    }
  } finally {
    scanning.value = false
  }
}
</script>

<style scoped>
.qr-scanner {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx;
}

.scanner-header {
  text-align: center;
  margin-bottom: 60rpx;
}

.title {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 16rpx;
}

.tip {
  display: block;
  font-size: 28rpx;
  color: #999;
}

.scanner-body {
  width: 100%;
}

.scan-btn {
  width: 100%;
  height: 400rpx;
  border: 2rpx dashed #0052d9;
  border-radius: 12rpx;
  background: #f5f9ff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.scan-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.scan-text {
  font-size: 32rpx;
  color: #0052d9;
}
</style>
