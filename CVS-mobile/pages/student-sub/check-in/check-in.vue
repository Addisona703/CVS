<template>
  <view class="check-in-page">
    <view class="header">
      <view class="title">扫码签到/签退</view>
      <view class="desc">请扫描活动现场的二维码</view>
    </view>

    <view class="scan-area">
      <view class="scan-icon">📷</view>
      <button class="scan-btn" @click="handleScan">开始扫描</button>
    </view>

    <view class="tips">
      <view class="tip-title">温馨提示</view>
      <view class="tip-item">• 请在活动现场扫描教师提供的二维码</view>
      <view class="tip-item">• 签到和签退需要分别扫描对应的二维码</view>
      <view class="tip-item">• 请确保在规定时间内完成签到签退</view>
      <view class="tip-item">• 签退后需等待教师审核才能获得积分</view>
    </view>

    <!-- 签退弹窗 -->
    <view v-if="showCheckoutDialog" class="checkout-dialog-mask" @click="showCheckoutDialog = false">
      <view class="checkout-dialog" @click.stop>
        <view class="dialog-header">
          <text class="dialog-title">签退评价</text>
          <text class="dialog-close" @click="showCheckoutDialog = false">✕</text>
        </view>
        
        <view class="dialog-body">
          <view class="form-item">
            <view class="form-label">服务自评 <text class="required">*</text></view>
            <view class="rating-group">
              <view 
                v-for="star in 5" 
                :key="star" 
                class="star-item"
                @click="checkoutForm.rating = star"
              >
                <text class="star-icon" :class="{ 'active': star <= checkoutForm.rating }">
                  {{ star <= checkoutForm.rating ? '⭐' : '☆' }}
                </text>
              </view>
            </view>
            <view class="rating-desc">{{ getRatingDesc(checkoutForm.rating) }}</view>
          </view>
          
          <view class="form-item">
            <view class="form-label">服务描述 <text class="optional">(选填)</text></view>
            <textarea
              v-model="checkoutForm.evaluation"
              class="evaluation-input"
              placeholder="请描述您在本次活动中的服务内容和感受..."
              placeholder-style="color: #999;"
              maxlength="200"
              :auto-height="true"
            />
            <view class="char-count">{{ checkoutForm.evaluation.length }}/200</view>
          </view>
        </view>
        
        <view class="dialog-footer">
          <button class="dialog-btn cancel-btn" @click="showCheckoutDialog = false">取消</button>
          <button class="dialog-btn confirm-btn" @click="confirmCheckout">提交签退</button>
        </view>
      </view>
    </view>
    
    <!-- 最近签到记录 -->
    <view class="recent-checks">
      <view class="section-title">最近签到记录</view>
      <view v-if="recentChecks.length === 0" class="empty">暂无记录</view>
      <view v-else class="check-list">
        <view v-for="check in recentChecks" :key="check.id" class="check-item">
          <view class="check-info">
            <view class="activity-name">{{ check.activityTitle }}</view>
            <view class="check-time">
              签到: {{ formatTime(check.checkInTime) }}
              <text v-if="check.checkOutTime"> | 签退: {{ formatTime(check.checkOutTime) }}</text>
            </view>
          </view>
          <view class="check-status" :class="getStatusClass(check.status)">
            {{ getStatusText(check.status) }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as checkApi from '@/api/check'
import { getMySignups } from '@/api/signup'
import { formatDateTime } from '@/utils/format'

const recentChecks = ref([])
const showCheckoutDialog = ref(false)
const checkoutToken = ref('')
const checkoutForm = ref({
  rating: 5,
  evaluation: ''
})

const handleScan = () => {
  uni.scanCode({
    success: async (res) => {
      try {
        let token = res.result
        console.log('=== 扫码开始 ===')
        console.log('扫描到的二维码原始内容:', token)
        console.log('原始内容类型:', typeof token)
        console.log('原始内容长度:', token ? token.length : 0)
        console.log('原始内容字节:', token ? Array.from(token).map(c => c.charCodeAt(0)) : [])
        
        // 检查二维码内容是否为空
        if (!token) {
          throw new Error('二维码内容为空')
        }
        
        // 清理token：去除首尾空格、换行符、特殊字符
        token = token.trim().replace(/[\r\n\t\s]/g, '').replace(/\u0000/g, '')
        
        if (token === '') {
          throw new Error('二维码内容为空')
        }
        
        console.log('清理后的token:', token)
        console.log('清理后的长度:', token.length)
        
        // 验证token格式（应该是一个32位的十六进制字符串）
        if (!/^[a-f0-9]{32}$/i.test(token)) {
          console.warn('token格式验证失败，但仍然尝试提交')
          console.log('token不符合32位十六进制格式，实际格式:', token)
        }
        
        console.log('准备提交token进行签到/签退')
        
        // 尝试签到
        try {
          await handleCheckIn(token)
        } catch (checkInError) {
          console.log('签到失败，尝试签退')
          console.log('签到错误:', checkInError)
          
          // 如果签到失败，尝试签退
          // 这样可以让学生不用关心是签到还是签退二维码
          try {
            await handleCheckOut(token)
          } catch (checkOutError) {
            // 两个都失败，显示更友好的错误信息
            console.error('签到和签退都失败')
            console.error('签到错误:', checkInError)
            console.error('签退错误:', checkOutError)
            
            // 优先使用签退的错误信息（因为签退是第二次尝试，错误信息可能更准确）
            const checkOutMsg = checkOutError.message || ''
            const checkInMsg = checkInError.message || ''
            
            // 根据错误信息给出更具体的提示
            if (checkOutMsg.includes('未签到')) {
              throw new Error('您还未签到，请先扫描签到二维码')
            } else if (checkOutMsg.includes('已签退')) {
              throw new Error('您已经签退过了')
            } else if (checkInMsg.includes('已签到')) {
              throw new Error('您已经签到过了，请扫描签退二维码')
            } else if (checkInMsg.includes('未报名') || checkOutMsg.includes('未报名')) {
              throw new Error('您未报名此活动或报名未通过审核')
            } else if (checkInMsg.includes('过期') && checkOutMsg.includes('过期')) {
              throw new Error('二维码已过期，请让教师刷新二维码')
            } else if (checkInMsg.includes('令牌无效') && checkOutMsg.includes('令牌无效')) {
              throw new Error('二维码无效，请确认是否为正确的签到/签退二维码')
            } else {
              // 显示更详细的错误信息
              throw new Error(checkOutMsg || checkInMsg || '操作失败')
            }
          }
        }
        console.log('=== 扫码结束 ===')
      } catch (error) {
        console.error('扫码处理失败:', error)
        uni.showToast({
          title: error.message || '扫码失败',
          icon: 'none',
          duration: 3000
        })
      }
    },
    fail: (error) => {
      console.error('扫码失败:', error)
      uni.showToast({
        title: '扫码失败，请重试',
        icon: 'none'
      })
    }
  })
}

const handleCheckIn = async (token) => {
  try {
    console.log('=== 开始签到 ===')
    console.log('签到token:', token)
    console.log('签到token长度:', token.length)
    console.log('签到请求体:', { token })
    
    const response = await checkApi.studentCheckin(
      { token: token },
      { suppressErrorToast: true } // 由页面自行处理 toast，避免重复提示
    )
    
    console.log('签到响应:', response)
    console.log('=== 签到成功 ===')
    
    uni.showToast({
      title: '签到成功',
      icon: 'success'
    })
    
    // 刷新记录
    loadRecentChecks()
  } catch (error) {
    console.error('=== 签到失败 ===')
    console.error('错误对象:', error)
    console.error('错误消息:', error.message)
    console.error('错误响应:', error.response)
    console.error('错误代码:', error.code || error.response?.data?.code)
    
    // 提供更详细的错误信息
    const errorMsg = error.message || error.msg || '签到失败'
    const errorCode = error.code || error.response?.data?.code
    
    // 根据错误代码和消息判断具体情况
    if (errorMsg.includes('已签到')) {
      throw new Error('您已经签到过了，请扫描签退二维码')
    } else if (errorMsg.includes('未报名')) {
      throw new Error('您未报名此活动或报名未通过审核')
    } else if (errorCode === 1006 && errorMsg.includes('令牌无效')) {
      // 令牌无效可能是已经签到了，让外层逻辑尝试签退
      throw error
    } else if (errorMsg.includes('过期')) {
      throw new Error('二维码已过期，请让教师重新生成')
    }
    throw error
  }
}

const handleCheckOut = async (token) => {
  // 直接显示签退弹窗（不预先验证，让用户填写后再提交）
  checkoutToken.value = token
  checkoutForm.value = {
    rating: 5,
    evaluation: ''
  }
  showCheckoutDialog.value = true
  
  // 注意：这里不抛出错误，因为我们要显示弹窗让用户填写评价
  // 实际的验证会在 confirmCheckout 中进行
}

const confirmCheckout = async () => {
  if (!checkoutForm.value.rating) {
    uni.showToast({
      title: '请选择服务自评分数',
      icon: 'none'
    })
    return
  }
  
  try {
    console.log('=== 开始签退 ===')
    console.log('签退token:', checkoutToken.value)
    console.log('签退请求体:', {
      token: checkoutToken.value,
      studentRating: checkoutForm.value.rating,
      studentEvaluation: checkoutForm.value.evaluation
    })
    
    const response = await checkApi.studentCheckout({
      token: checkoutToken.value,
      studentRating: checkoutForm.value.rating,
      studentEvaluation: checkoutForm.value.evaluation || undefined
    })
    
    console.log('签退响应:', response)
    console.log('=== 签退成功 ===')
    
    showCheckoutDialog.value = false
    
    // 使用自定义弹窗显示完整提示
    setTimeout(() => {
      uni.showModal({
        title: '签退成功',
        content: '您已成功签退，请等待教师审核后获得积分',
        showCancel: false,
        confirmText: '知道了',
        success: () => {
          // 刷新记录
          loadRecentChecks()
        }
      })
    }, 300)
  } catch (error) {
    console.error('=== 签退失败 ===')
    console.error('错误对象:', error)
    console.error('错误消息:', error.message)
    console.error('错误响应:', error.response)
    console.error('错误代码:', error.code || error.response?.data?.code)
    
    // 提供更详细的错误信息
    const errorMsg = error.message || error.msg || '签退失败'
    const errorCode = error.code || error.response?.data?.code
    
    // 根据错误代码和消息判断具体情况
    if (errorMsg.includes('未签到')) {
      throw new Error('您还未签到，请先扫描签到二维码')
    } else if (errorMsg.includes('已签退')) {
      throw new Error('您已经签退过了')
    } else if (errorMsg.includes('未报名')) {
      throw new Error('您未报名此活动或报名未通过审核')
    } else if (errorCode === 1006 && errorMsg.includes('令牌无效')) {
      // 令牌无效可能是因为这是签到二维码而不是签退二维码
      uni.showToast({
        title: '二维码无效，请确认是否为签退二维码',
        icon: 'none',
        duration: 3000
      })
      return
    } else if (errorMsg.includes('过期')) {
      uni.showToast({
        title: '二维码已过期，请让教师重新生成',
        icon: 'none',
        duration: 3000
      })
      return
    }
    
    // 显示通用错误
    uni.showToast({
      title: errorMsg,
      icon: 'none',
      duration: 3000
    })
  }
}

const getRatingDesc = (rating) => {
  const descMap = {
    1: '很不满意',
    2: '不满意',
    3: '一般',
    4: '满意',
    5: '非常满意'
  }
  return descMap[rating] || '请选择'
}

const loadRecentChecks = async () => {
  try {
    const res = await getMySignups({
      pageNum: 1,
      pageSize: 5
    })
    recentChecks.value = res.records || []
  } catch (error) {
    console.error('加载签到记录失败:', error)
  }
}

const formatTime = (time) => {
  return formatDateTime(time, 'MM-DD HH:mm')
}

const getStatusClass = (status) => {
  const classMap = {
    'CHECKED_IN': 'status-checkin',
    'CHECKED_OUT': 'status-checkout',
    'APPROVED': 'status-approved',
    'REJECTED': 'status-rejected'
  }
  return classMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    'CHECKED_IN': '已签到',
    'CHECKED_OUT': '待审核',
    'APPROVED': '已完成',
    'REJECTED': '已拒绝'
  }
  return textMap[status] || '未知'
}

onMounted(() => {
  loadRecentChecks()
})
</script>

<style scoped>
.check-in-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 24rpx;
}

.header {
  text-align: center;
  padding: 48rpx 0;
}

.title {
  font-size: 40rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 16rpx;
}

.desc {
  font-size: 28rpx;
  color: #999;
}

.scan-area {
  background: #fff;
  border-radius: 16rpx;
  padding: 80rpx 48rpx;
  text-align: center;
  margin-bottom: 32rpx;
}

.scan-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
}

.scan-btn {
  width: 400rpx;
  height: 88rpx;
  line-height: 88rpx;
  background: #0052d9;
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
  border: none;
  margin: 0 auto;
  display: block;
}

.scan-btn::after {
  border: none;
}

.tips {
  background: #fff;
  border-radius: 12rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
}

.tip-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 16rpx;
}

.tip-item {
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
  margin-bottom: 8rpx;
}

.recent-checks {
  background: #fff;
  border-radius: 12rpx;
  padding: 32rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
}

.empty {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 28rpx;
}

.check-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.check-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
}

.check-info {
  flex: 1;
}

.activity-name {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
}

.check-time {
  font-size: 24rpx;
  color: #999;
}

.check-status {
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  white-space: nowrap;
}

.status-checkin {
  background: #e6f7ff;
  color: #1890ff;
}

.status-checkout {
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

/* 签退弹窗样式 */
.checkout-dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
}

.checkout-dialog {
  width: 100%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
  flex-shrink: 0;
}

.dialog-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #333;
}

.dialog-close {
  font-size: 40rpx;
  color: #999;
  line-height: 1;
}

.dialog-body {
  flex: 1;
  overflow-y: auto;
  padding: 32rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
  font-weight: 600;
}

.required {
  color: #ff4d4f;
}

.optional {
  font-size: 24rpx;
  color: #999;
  font-weight: 400;
}

.rating-group {
  display: flex;
  gap: 16rpx;
  justify-content: center;
  padding: 20rpx 0;
}

.star-item {
  cursor: pointer;
}

.star-icon {
  font-size: 56rpx;
  color: #d9d9d9;
  transition: all 0.3s ease;
}

.star-icon.active {
  color: #fadb14;
}

.rating-desc {
  text-align: center;
  font-size: 26rpx;
  color: #666;
  margin-top: 12rpx;
}

.evaluation-input {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  box-sizing: border-box;
}

.char-count {
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 12rpx;
}

.dialog-footer {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx 32rpx;
  border-top: 1rpx solid #f0f0f0;
  flex-shrink: 0;
}

.dialog-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
}

.dialog-btn::after {
  border: none;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.confirm-btn {
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
  color: #fff;
}
</style>
