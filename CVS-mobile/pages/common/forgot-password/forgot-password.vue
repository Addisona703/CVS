<template>
  <view class="forgot-password-page" :class="'role-' + userRole.toLowerCase()">
    <view class="page-header">
      <text class="page-title">忘记密码</text>
      <text class="page-subtitle">通过邮箱验证找回密码</text>
    </view>
    
    <view class="form-container">
      <view class="form-item">
        <text class="form-label">邮箱</text>
        <input 
          v-model="formData.email" 
          class="form-input" 
          placeholder="请输入注册时使用的邮箱"
          type="text"
        />
      </view>
      
      <view class="form-item">
        <text class="form-label">验证码</text>
        <view class="input-with-btn">
          <input 
            v-model="formData.code" 
            class="form-input flex-1" 
            placeholder="请输入验证码"
            :maxlength="6"
          />
          <button 
            class="code-btn" 
            :disabled="codeCountdown > 0"
            @click="handleSendCode"
          >
            {{ codeCountdown > 0 ? `${codeCountdown}s` : '发送验证码' }}
          </button>
        </view>
      </view>
      
      <button 
        class="submit-btn" 
        :disabled="loading"
        @click="handleNext"
      >
        {{ loading ? '验证中...' : '下一步' }}
      </button>
      
      <view class="back-link">
        <text class="link-action" @click="handleGoBack">返回登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { sendCode } from '@/api/auth'
import { validateEmail } from '@/utils/validate'

const formData = ref({
  email: '',
  code: ''
})

const userRole = ref('STUDENT') // 默认学生

onMounted(() => {
  // 获取URL参数中的角色
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || currentPage.$page?.options || {}
  
  if (options.role) {
    userRole.value = options.role
  }
})

const loading = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

// 发送验证码
const handleSendCode = async () => {
  if (!formData.value.email) {
    uni.showToast({ title: '请输入邮箱', icon: 'none' })
    return
  }
  
  if (!validateEmail(formData.value.email)) {
    uni.showToast({ title: '邮箱格式不正确', icon: 'none' })
    return
  }
  
  try {
    await sendCode({
      email: formData.value.email,
      type: 'reset'
    })
    
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    
    // 开始倒计时
    codeCountdown.value = 60
    countdownTimer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

// 验证并跳转到重置密码页面
const handleNext = async () => {
  if (!formData.value.email) {
    uni.showToast({ title: '请输入邮箱', icon: 'none' })
    return
  }
  
  if (!validateEmail(formData.value.email)) {
    uni.showToast({ title: '邮箱格式不正确', icon: 'none' })
    return
  }
  
  if (!formData.value.code) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  
  // 跳转到重置密码页面，传递邮箱、验证码和角色
  uni.navigateTo({
    url: `/pages/common/reset-password/reset-password?email=${formData.value.email}&code=${formData.value.code}&role=${userRole.value}`
  })
}

// 返回登录
const handleGoBack = () => {
  uni.navigateBack()
}

// 清理定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 32rpx;
}

.page-header {
  padding: 48rpx 0 32rpx;
}

.page-title {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: #333;
  margin-bottom: 16rpx;
}

.page-subtitle {
  display: block;
  font-size: 28rpx;
  color: #666;
}

.form-container {
  background: #fff;
  border-radius: 16rpx;
  padding: 48rpx 32rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
}

.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.input-with-btn {
  display: flex;
  gap: 16rpx;
}

.flex-1 {
  flex: 1;
}

.code-btn {
  width: 200rpx;
  height: 88rpx;
  line-height: 88rpx;
  color: #fff;
  border-radius: 12rpx;
  font-size: 26rpx;
  border: none;
  padding: 0;
}

/* 学生角色 - 蓝色 */
.forgot-password-page.role-student .code-btn {
  background: #0052d9;
}

/* 教师角色 - 绿色 */
.forgot-password-page.role-teacher .code-btn {
  background: #52c41a;
}

/* 管理员角色 - 橙色 */
.forgot-password-page.role-admin .code-btn {
  background: #FF8C42;
}

.code-btn[disabled] {
  opacity: 0.6;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  color: #fff;
  border-radius: 12rpx;
  font-size: 32rpx;
  border: none;
  margin-bottom: 32rpx;
}

/* 学生角色 - 蓝色 */
.forgot-password-page.role-student .submit-btn {
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
}

/* 教师角色 - 绿色 */
.forgot-password-page.role-teacher .submit-btn {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

/* 管理员角色 - 橙色 */
.forgot-password-page.role-admin .submit-btn {
  background: linear-gradient(135deg, #FF8C42 0%, #FFB84D 100%);
}

.submit-btn[disabled] {
  opacity: 0.6;
}

.back-link {
  text-align: center;
  font-size: 26rpx;
}

/* 返回登录链接根据角色显示不同颜色 */
.forgot-password-page.role-student .link-action {
  color: #0052d9;
}

.forgot-password-page.role-teacher .link-action {
  color: #52c41a;
}

.forgot-password-page.role-admin .link-action {
  color: #FF8C42;
}
</style>
