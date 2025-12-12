<template>
  <view class="reset-password-page">
    <view class="page-header">
      <text class="page-title">重置密码</text>
      <text class="page-subtitle">设置新的登录密码</text>
    </view>
    
    <view class="form-container">
      <view class="form-item">
        <text class="form-label">新密码</text>
        <input 
          v-model="formData.newPassword" 
          class="form-input" 
          type="password"
          placeholder="请输入新密码（6-20位）"
          :maxlength="20"
        />
        <view v-if="passwordStrength" class="password-strength">
          <text :class="['strength-text', `strength-${passwordStrength}`]">
            密码强度：{{ passwordStrengthText }}
          </text>
          <view class="strength-bar">
            <view 
              :class="['strength-item', { active: passwordStrength >= 1 }]"
            ></view>
            <view 
              :class="['strength-item', { active: passwordStrength >= 2 }]"
            ></view>
            <view 
              :class="['strength-item', { active: passwordStrength >= 3 }]"
            ></view>
          </view>
        </view>
      </view>
      
      <view class="form-item">
        <text class="form-label">确认密码</text>
        <input 
          v-model="formData.confirmPassword" 
          class="form-input" 
          type="password"
          placeholder="请再次输入新密码"
          :maxlength="20"
        />
      </view>
      
      <button 
        class="submit-btn" 
        :disabled="loading"
        @click="handleResetPassword"
      >
        {{ loading ? '提交中...' : '确认重置' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onLoad } from 'vue'
import { resetPassword } from '@/api/auth'
import { validatePassword, checkPasswordStrength } from '@/utils/validate'

const formData = ref({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const loading = ref(false)

// 密码强度
const passwordStrength = computed(() => {
  if (!formData.value.newPassword) return 0
  return checkPasswordStrength(formData.value.newPassword)
})

const passwordStrengthText = computed(() => {
  const texts = ['', '弱', '中', '强']
  return texts[passwordStrength.value] || ''
})

// 页面加载时获取参数
onLoad((options) => {
  if (options.email) {
    formData.value.email = options.email
  }
  if (options.code) {
    formData.value.code = options.code
  }
})

// 表单验证
const validateForm = () => {
  if (!formData.value.newPassword) {
    uni.showToast({ title: '请输入新密码', icon: 'none' })
    return false
  }
  
  if (!validatePassword(formData.value.newPassword)) {
    uni.showToast({ title: '密码格式不正确（6-20位）', icon: 'none' })
    return false
  }
  
  if (passwordStrength.value < 2) {
    uni.showToast({ title: '密码强度太弱，请使用更复杂的密码', icon: 'none' })
    return false
  }
  
  if (formData.value.newPassword !== formData.value.confirmPassword) {
    uni.showToast({ title: '两次密码输入不一致', icon: 'none' })
    return false
  }
  
  return true
}

// 重置密码
const handleResetPassword = async () => {
  if (!validateForm()) return
  
  loading.value = true
  
  try {
    await resetPassword({
      email: formData.value.email,
      code: formData.value.code,
      newPassword: formData.value.newPassword
    })
    
    uni.showToast({ title: '密码重置成功', icon: 'success' })
    
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/index/index'
      })
    }, 1500)
  } catch (error) {
    console.error('重置密码失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reset-password-page {
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

.password-strength {
  margin-top: 16rpx;
}

.strength-text {
  display: block;
  font-size: 24rpx;
  margin-bottom: 8rpx;
}

.strength-text.strength-1 {
  color: #e34d59;
}

.strength-text.strength-2 {
  color: #ed7b2f;
}

.strength-text.strength-3 {
  color: #2ba471;
}

.strength-bar {
  display: flex;
  gap: 8rpx;
}

.strength-item {
  flex: 1;
  height: 8rpx;
  background: #e0e0e0;
  border-radius: 4rpx;
}

.strength-item.active {
  background: currentColor;
}

.strength-1 .strength-item.active {
  background: #e34d59;
}

.strength-2 .strength-item.active {
  background: #ed7b2f;
}

.strength-3 .strength-item.active {
  background: #2ba471;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #0052d9;
  color: #fff;
  border-radius: 12rpx;
  font-size: 32rpx;
  border: none;
}

.submit-btn[disabled] {
  opacity: 0.6;
}
</style>
