<template>
  <view class="management-page">
    <!-- 管理功能区 -->
    <view class="management-section">
      <view class="section-title">系统管理</view>
      <view class="menu-list">
        <view
          class="menu-item"
          v-for="menu in managementMenus"
          :key="menu.key"
          @click="handleAction(menu.path)"
        >
          <view class="menu-icon">
            <ph-icon :name="menu.icon" :size="52" color="#FA8C16" />
          </view>
          <view class="menu-content">
            <text class="menu-title">{{ menu.title }}</text>
            <text class="menu-desc">{{ menu.desc }}</text>
          </view>
          <ph-icon name="caret-right" :size="28" color="#ccc" />
        </view>
      </view>
    </view>

    <!-- 商品核销区 -->
    <view class="verify-section">
      <view class="section-title">商品核销</view>
      <view class="verify-card" @click="handleScanCode">
        <view class="verify-icon">
          <ph-icon name="qr-code" :size="72" color="#fff" />
        </view>
        <view class="verify-content">
          <text class="verify-title">扫码核销</text>
          <text class="verify-desc">扫描学生兑换码进行核销</text>
        </view>
        <view class="verify-action">
          <text class="action-btn">立即扫码</text>
        </view>
      </view>
      
      <view class="verify-stats">
        <view class="stat-item" @click="handleViewRedemptions('TODAY')">
          <text class="stat-value">{{ verifyStats.todayCount }}</text>
          <text class="stat-label">今日核销</text>
        </view>
        <view class="stat-item" @click="handleViewRedemptions('VERIFIED')">
          <text class="stat-value">{{ verifyStats.totalCount }}</text>
          <text class="stat-label">累计核销</text>
        </view>
        <view class="stat-item" @click="handleViewRedemptions('PENDING')">
          <text class="stat-value">{{ verifyStats.pendingCount }}</text>
          <text class="stat-label">待核销</text>
        </view>
      </view>
    </view>

    <!-- Custom Tab Bar -->
    <custom-tabbar :current="2" role="admin" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getVerifyStatistics } from '@/api/statistics'
import CustomTabbar from '@/components/common/custom-tabbar/custom-tabbar.vue'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'

const managementMenus = ref([
  {
    key: 'category',
    title: '分类管理',
    desc: '管理商品分类',
    icon: 'folder',
    path: '/pages/admin/category-management/category-management'
  },
  {
    key: 'points',
    title: '积分管理',
    desc: '查看和管理用户积分',
    icon: 'star',
    path: '/pages/admin/points-management/points-management'
  },
  {
    key: 'service',
    title: '服务记录',
    desc: '查看和管理服务记录',
    icon: 'file-text',
    path: '/pages/admin/service-records/service-records'
  },
  {
    key: 'statistics',
    title: '数据统计',
    desc: '查看商品兑换和积分统计',
    icon: 'chart-bar',
    path: '/pages/admin/statistics/statistics'
  }
])

const verifyStats = ref({
  todayCount: 0,
  totalCount: 0,
  pendingCount: 0
})

const handleAction = (path) => {
  uni.navigateTo({ url: path })
}

const handleScanCode = () => {
  // Check if scanCode is supported (not available in H5/browser)
  // #ifdef H5
  uni.showModal({
    title: '提示',
    content: '扫码功能仅在移动端APP中可用，H5环境暂不支持',
    showCancel: false
  })
  return
  // #endif
  
  uni.scanCode({
    success: async (res) => {
      console.log('扫码结果:', res)
      console.log('扫码内容:', res.result)
      
      // 解析二维码内容
      let token = res.result
      
      // 尝试解析JSON格式
      try {
        const data = JSON.parse(res.result)
        if (data.type === 'redemption' && data.code) {
          token = data.code
        } else if (data.redemptionId) {
          token = data.redemptionId
        }
      } catch (error) {
        // 不是JSON格式，直接使用原始字符串作为token
        console.log('二维码不是JSON格式，使用原始内容作为token')
      }
      
      console.log('解析后的token:', token)
      
      // 检查是否是兑换凭证码（MALL开头或其他格式）
      if (token && typeof token === 'string' && token.length > 0) {
        console.log('开始核销，token:', token)
        console.log('token类型:', typeof token)
        console.log('token长度:', token.length)
        
        try {
          // 直接调用核销API
          const { verifyRedemption } = await import('@/api/mall')
          console.log('准备调用核销API，参数:', { voucherCode: token })
          const response = await verifyRedemption({ voucherCode: token })
          console.log('核销API响应:', response)
          
          uni.showToast({
            title: '核销成功',
            icon: 'success'
          })
          
          // 刷新统计数据
          loadVerifyStats()
        } catch (error) {
          console.error('核销失败，完整错误:', error)
          console.error('错误类型:', error.constructor.name)
          console.error('错误消息:', error.message)
          console.error('错误响应:', error.response)
          console.error('错误数据:', error.response?.data)
          
          let errorMsg = '核销失败'
          if (error.message) {
            if (error.message.includes('已核销')) {
              errorMsg = '该兑换已核销'
            } else if (error.message.includes('不存在') || error.message.includes('无效')) {
              errorMsg = '兑换凭证无效'
            } else if (error.message.includes('过期')) {
              errorMsg = '兑换凭证已过期'
            } else {
              errorMsg = error.message
            }
          }
          
          uni.showToast({
            title: errorMsg,
            icon: 'none',
            duration: 2000
          })
        }
      } else {
        uni.showToast({
          title: '无效的兑换码',
          icon: 'none',
          duration: 2000
        })
      }
    },
    fail: (error) => {
      console.error('扫码失败:', error)
      uni.showToast({
        title: '扫码失败',
        icon: 'none'
      })
    }
  })
}

const loadVerifyStats = async () => {
  try {
    const data = await getVerifyStatistics()
    verifyStats.value = {
      todayCount: data.todayCount || 0,
      totalCount: data.totalCount || 0,
      pendingCount: data.pendingCount || 0
    }
  } catch (error) {
    console.error('加载核销统计失败:', error)
  }
}

// 查看兑换记录列表
const handleViewRedemptions = (statusType) => {
  uni.navigateTo({
    url: `/pages/admin/redemption-list/redemption-list?statusType=${statusType}`
  })
}

// 下拉刷新
onPullDownRefresh(async () => {
  try {
    await loadVerifyStats()
  } finally {
    uni.stopPullDownRefresh()
  }
})

onMounted(() => {
  loadVerifyStats()
})


</script>

<style scoped>
.management-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
  padding-bottom: 120rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #333;
  margin-bottom: 20rpx;
  padding: 0 8rpx;
}

/* 管理功能区 */
.management-section {
  margin-bottom: 32rpx;
}

.menu-list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  width: 80rpx;
  height: 80rpx;
  background: #FFF7E6;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.menu-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.menu-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.menu-desc {
  font-size: 24rpx;
  color: #999;
}



/* 商品核销区 */
.verify-section {
  margin-bottom: 32rpx;
}

.verify-card {
  display: flex;
  align-items: center;
  padding: 32rpx 24rpx;
  background: linear-gradient(135deg, #FA8C16 0%, #FF6B35 100%);
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(250, 140, 22, 0.3);
}

.verify-icon {
  width: 96rpx;
  height: 96rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.verify-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.verify-title {
  font-size: 32rpx;
  color: #fff;
  font-weight: 600;
}

.verify-desc {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.verify-action {
  padding: 16rpx 32rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 32rpx;
  backdrop-filter: blur(10rpx);
}

.action-btn {
  font-size: 26rpx;
  color: #fff;
  font-weight: 500;
}

.verify-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-item:active {
  transform: scale(0.95);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 40rpx;
  font-weight: 700;
  color: #FA8C16;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}


</style>
