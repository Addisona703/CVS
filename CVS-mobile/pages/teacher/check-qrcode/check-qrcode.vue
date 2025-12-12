<template>
  <view class="check-qrcode">
    <!-- 活动选择 -->
    <view class="activity-selector">
      <view class="selector-label">选择活动</view>
      <picker
        mode="selector"
        :range="activities"
        range-key="title"
        :value="selectedActivityIndex"
        @change="onActivityChange"
      >
        <view class="picker-input">
          {{ selectedActivity ? selectedActivity.title : '请选择活动' }}
        </view>
      </picker>
    </view>

    <!-- 二维码类型切换 -->
    <view v-if="selectedActivity" class="qrcode-type-tabs">
      <view
        class="type-tab"
        :class="{ active: qrcodeType === 'CHECK_IN' }"
        @click="qrcodeType = 'CHECK_IN'"
      >
        签到二维码
      </view>
      <view
        class="type-tab"
        :class="{ active: qrcodeType === 'CHECK_OUT' }"
        @click="qrcodeType = 'CHECK_OUT'"
      >
        签退二维码
      </view>
    </view>

    <!-- 二维码展示 -->
    <view v-if="selectedActivity" class="qrcode-container">
      <qr-display
        :data="qrcodeData"
        :type="qrcodeType"
        :activity="selectedActivity"
        @refresh="refreshQrcode"
      />
    </view>

    <!-- 提示信息 -->
    <view v-if="selectedActivity" class="tips">
      <view class="tip-item">
        <text class="tip-icon">ℹ️</text>
        <text class="tip-text">二维码每5分钟自动刷新一次</text>
      </view>
      <view class="tip-item">
        <text class="tip-icon">⏰</text>
        <text class="tip-text">二维码有效期为5分钟</text>
      </view>
      <view class="tip-item">
        <text class="tip-icon">🔒</text>
        <text class="tip-text">二维码包含安全签名，防止伪造</text>
      </view>
    </view>

    <!-- 空状态 -->
    <empty-state
      v-if="!selectedActivity && !loading && activities.length === 0"
      description="暂无进行中的活动"
    />
    <empty-state
      v-else-if="!selectedActivity && !loading"
      description="请先选择一个活动"
    />
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { getActivityList } from '@/api/activity'
import { generateCheckinToken, generateCheckoutToken } from '@/api/check'
import { useAuthStore } from '@/stores/auth'
import QrDisplay from '@/components/business/qr-display/qr-display.vue'
import EmptyState from '@/components/common/empty-state/empty-state.vue'

const activities = ref([])
const selectedActivityIndex = ref(0)
const selectedActivity = computed(() => activities.value[selectedActivityIndex.value])
const qrcodeType = ref('CHECK_IN')
const qrcodeData = ref('')
const loading = ref(false)
const refreshTimer = ref(null)

// 监听二维码类型变化，自动重新生成
watch(qrcodeType, () => {
  if (selectedActivity.value) {
    generateQrcode()
  }
})

onMounted(() => {
  loadActivities()
})

onUnmounted(() => {
  clearRefreshTimer()
})

const loadActivities = async () => {
  loading.value = true
  try {
    // 获取当前用户信息
    const authStore = useAuthStore()
    const userId = authStore.userId

    const res = await getActivityList({
      pageNum: 1,
      pageSize: 100,
      params: {
        organizerId: userId,
        status: 'ONGOING' // 只获取进行中的活动
      }
    })
    activities.value = res.records || []
    
    if (activities.value.length > 0) {
      generateQrcode()
      startAutoRefresh()
    } else {
      uni.showToast({
        title: '暂无进行中的活动',
        icon: 'none',
        duration: 2000
      })
    }
  } catch (error) {
    console.error('加载活动列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const onActivityChange = (e) => {
  selectedActivityIndex.value = e.detail.value
  generateQrcode()
}

const generateQrcode = async () => {
  if (!selectedActivity.value) return

  try {
    loading.value = true
    
    // 调用后端API生成token
    let response
    
    if (qrcodeType.value === 'CHECK_IN') {
      response = await generateCheckinToken(selectedActivity.value.id)
    } else {
      response = await generateCheckoutToken(selectedActivity.value.id)
    }
    
    // 后端返回的token直接作为二维码内容
    qrcodeData.value = response.token
    
    console.log('生成二维码token:', response.token)
    console.log('过期时间:', response.expiresAt)
  } catch (error) {
    console.error('生成二维码失败:', error)
    uni.showToast({
      title: '生成二维码失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const refreshQrcode = () => {
  generateQrcode()
  uni.showToast({
    title: '二维码已刷新',
    icon: 'success',
    duration: 1500
  })
}

const startAutoRefresh = () => {
  clearRefreshTimer()
  refreshTimer.value = setInterval(() => {
    generateQrcode()
  }, 5 * 60 * 1000) // 每5分钟刷新一次
}

const clearRefreshTimer = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}
</script>

<style lang="scss" scoped>
.check-qrcode {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 32rpx;
}

.activity-selector {
  background: white;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.selector-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
  font-weight: bold;
}

.picker-input {
  padding: 24rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
  background: white;
}

.qrcode-type-tabs {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.type-tab {
  flex: 1;
  padding: 24rpx;
  text-align: center;
  background: white;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #666;
  transition: all 0.3s;

  &.active {
    color: white;
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    font-weight: bold;
  }
}

.qrcode-container {
  margin-bottom: 24rpx;
}

.tips {
  background: white;
  border-radius: 16rpx;
  padding: 32rpx;
}

.tip-item {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.tip-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}
</style>
