<template>
  <view class="redemption-verify-container">
    <!-- 扫码核销按钮 -->
    <view class="scan-section">
      <button class="scan-btn" @click="handleScan">
        <text class="scan-icon">📷</text>
        <text class="scan-text">扫码核销</text>
      </button>
    </view>

    <!-- 待核销订单 -->
    <view class="pending-section">
      <view class="section-header">
        <text class="section-title">待核销订单</text>
      </view>
      <view class="redemption-list">
        <view
          class="redemption-item"
          v-for="redemption in pendingList"
          :key="redemption.id"
          @click="handleVerify(redemption)"
        >
          <view class="redemption-info">
            <text class="product-name">{{ redemption.productName }}</text>
            <text class="user-name">{{ redemption.userName }}</text>
            <text class="redemption-time">{{ formatDateTime(redemption.createdAt) }}</text>
          </view>
          <view class="redemption-status pending">待核销</view>
        </view>
        
        <empty-state v-if="!loading && pendingList.length === 0" message="暂无待核销订单" />
      </view>
    </view>

    <!-- 核销历史 -->
    <view class="history-section">
      <view class="section-header">
        <text class="section-title">核销历史</text>
      </view>
      <view class="redemption-list">
        <view
          class="redemption-item"
          v-for="redemption in historyList"
          :key="redemption.id"
        >
          <view class="redemption-info">
            <text class="product-name">{{ redemption.productName }}</text>
            <text class="user-name">{{ redemption.userName }}</text>
            <text class="redemption-time">{{ formatDateTime(redemption.verifiedAt) }}</text>
          </view>
          <view class="redemption-status verified">已核销</view>
        </view>
        
        <empty-state v-if="!loading && historyList.length === 0" message="暂无核销历史" />
        <loading-state v-if="loading" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRedemptionList, verifyRedemption } from '@/api/mall'
import { useQrCode } from '@/composables/useQrCode'
import { formatDateTime } from '@/utils/format'
import EmptyState from '@/components/common/empty-state/empty-state.vue'
import LoadingState from '@/components/common/loading-state/loading-state.vue'

const pendingList = ref([])
const historyList = ref([])
const loading = ref(false)

const { scanQrCode, validateQrCode } = useQrCode()

const loadData = async () => {
  loading.value = true
  try {
    // 加载待核销订单
    const pendingResult = await getRedemptionList({
      status: 'PENDING',
      pageSize: 20
    })
    pendingList.value = pendingResult.list || []

    // 加载核销历史
    const historyResult = await getRedemptionList({
      status: 'VERIFIED',
      pageSize: 20
    })
    historyList.value = historyResult.list || []
  } catch (error) {
    console.error('加载数据失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const handleScan = async () => {
  try {
    console.log('开始扫码核销')
    const result = await scanQrCode()
    console.log('扫码原始结果:', result)
    console.log('扫码结果类型:', typeof result)
    console.log('扫码结果长度:', result ? result.length : 0)
    
    if (!result) {
      uni.showToast({
        title: '二维码内容为空',
        icon: 'none',
        duration: 2000
      })
      return
    }
    
    // 确保token是字符串，并去除首尾空格
    const token = String(result).trim()
    console.log('处理后的token:', token)
    console.log('token长度:', token.length)
    
    if (token === '') {
      uni.showToast({
        title: '二维码内容为空',
        icon: 'none',
        duration: 2000
      })
      return
    }
    
    // 核销兑换 - 使用voucherCode字段
    console.log('准备调用核销API，参数:', { voucherCode: token })
    const response = await verifyRedemption({ voucherCode: token })
    console.log('核销API响应:', response)
    
    uni.showToast({
      title: '核销成功',
      icon: 'success'
    })
    
    // 刷新列表
    loadData()
  } catch (error) {
    console.error('核销失败，完整错误:', error)
    console.error('错误消息:', error.message)
    console.error('错误响应:', error.response)
    
    // 提供更友好的错误提示
    let errorMsg = '核销失败'
    if (error.message) {
      if (error.message.includes('已核销')) {
        errorMsg = '该兑换已核销，请勿重复核销'
      } else if (error.message.includes('不存在') || error.message.includes('无效')) {
        errorMsg = '兑换凭证无效或不存在'
      } else if (error.message.includes('过期')) {
        errorMsg = '兑换凭证已过期'
      } else if (error.message.includes('格式')) {
        errorMsg = '二维码格式错误：' + error.message
      } else {
        errorMsg = error.message
      }
    }
    
    uni.showToast({
      title: errorMsg,
      icon: 'none',
      duration: 3000
    })
  }
}

const handleVerify = async (redemption) => {
  uni.showModal({
    title: '确认核销',
    content: `确定要核销"${redemption.productName}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          // 使用voucherCode字段
          const voucherCode = redemption.voucherCode || redemption.id.toString()
          await verifyRedemption({ voucherCode })
          uni.showToast({
            title: '核销成功',
            icon: 'success'
          })
          loadData()
        } catch (error) {
          console.error('核销失败:', error)
          uni.showToast({
            title: error.message || '核销失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.redemption-verify-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.scan-section {
  padding: 40rpx 20rpx;
  background-color: #fff;
  margin-bottom: 20rpx;
}

.scan-btn {
  width: 100%;
  height: 120rpx;
  background: linear-gradient(135deg, #FF8C42 0%, #FF6B35 100%);
  color: #fff;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 82, 217, 0.3);
}

.scan-icon {
  font-size: 48rpx;
  margin-bottom: 8rpx;
}

.scan-text {
  font-size: 28rpx;
  font-weight: bold;
}

.pending-section,
.history-section {
  background-color: #fff;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.section-header {
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.redemption-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.redemption-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
}

.redemption-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.product-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.user-name {
  font-size: 26rpx;
  color: #666;
}

.redemption-time {
  font-size: 24rpx;
  color: #999;
}

.redemption-status {
  padding: 8rpx 20rpx;
  border-radius: 32rpx;
  font-size: 24rpx;
  white-space: nowrap;
}

.redemption-status.pending {
  background-color: #FFF3E0;
  color: #E37318;
}

.redemption-status.verified {
  background-color: #E8F5E9;
  color: #00A870;
}
</style>
