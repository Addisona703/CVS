<template>
  <view class="login-page">
    <!-- 顶部Logo区域 -->
    <view class="logo-section">
      <view class="logo-container">
        <!-- 志愿服务logo：根据角色显示不同图标 -->
        <view class="logo-icon" :class="'role-' + selectedRole.toLowerCase()">
          <text class="role-emoji">{{ getRoleEmoji() }}</text>
        </view>
        <text class="app-title">志愿服务平台</text>
        <text class="app-subtitle">Community Volunteer Service</text>
      </view>
    </view>

    <!-- 登录卡片 -->
    <view class="login-card" :class="'role-' + selectedRole.toLowerCase()">
      <!-- 身份选择 -->
      <view class="role-selector">
        <view 
          class="role-item" 
          :class="{ active: selectedRole === 'STUDENT' }"
          @click="selectRole('STUDENT')"
        >
          <text class="role-text">学生</text>
        </view>
        <view 
          class="role-item" 
          :class="{ active: selectedRole === 'TEACHER' }"
          @click="selectRole('TEACHER')"
        >
          <text class="role-text">教师</text>
        </view>
        <view 
          class="role-item" 
          :class="{ active: selectedRole === 'ADMIN' }"
          @click="selectRole('ADMIN')"
        >
          <text class="role-text">学工处</text>
        </view>
      </view>

      <!-- 登录表单 -->
      <view class="login-form">
        <view class="form-item">
          <input
            v-model="formData.username"
            class="form-input"
            placeholder="请输入学号/工号"
            :maxlength="20"
          />
        </view>

        <view class="form-item">
          <input
            v-model="formData.password"
            class="form-input"
            type="password"
            placeholder="请输入密码"
            :maxlength="20"
          />
        </view>

        <view class="form-options">
          <label class="remember-checkbox">
            <checkbox
              :checked="formData.remember"
              @change="handleRememberChange"
              :color="getThemeColor()"
            />
            <text class="checkbox-label">记住密码</text>
          </label>
          <text class="forgot-link" @click="handleForgotPassword">忘记密码？</text>
        </view>

        <button
          class="login-btn"
          :class="{ loading: loading }"
          :disabled="loading"
          @click="handleLogin"
        >
          <text v-if="!loading">登 录</text>
          <text v-else>登录中...</text>
        </button>

        <view v-if="selectedRole !== 'ADMIN'" class="register-link">
          <text class="link-text">还没有账号？</text>
          <text class="link-action" @click="handleRegister">立即注册</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { validateUsername, validatePassword } from '@/utils/validate'
import { CacheManager } from '@/utils/storage'

const authStore = useAuthStore()

const selectedRole = ref('STUDENT')
const formData = ref({
  username: '',
  password: '',
  remember: false
})
const loading = ref(false)

const selectRole = (role) => {
  selectedRole.value = role
}

const getRoleEmoji = () => {
  const emojiMap = {
    'STUDENT': '🎓',
    'TEACHER': '👨‍🏫',
    'ADMIN': '🏫'
  }
  return emojiMap[selectedRole.value] || '🎓'
}

const getThemeColor = () => {
  const colorMap = {
    'STUDENT': '#0052d9',
    'TEACHER': '#52c41a',
    'ADMIN': '#FF8C42'
  }
  return colorMap[selectedRole.value] || '#0052d9'
}

onMounted(() => {
  // 清除可能存在问题的旧Token
  try {
    uni.removeStorageSync('accessToken')
    uni.removeStorageSync('refreshToken')
    uni.removeStorageSync('token')
  } catch (e) {
    console.log('清除缓存失败:', e)
  }
  
  const savedUsername = CacheManager.get('saved_username', false)
  const savedPassword = CacheManager.get('saved_password', false)

  if (savedUsername && savedPassword) {
    formData.value.username = savedUsername
    formData.value.password = savedPassword
    formData.value.remember = true
  }
})

const handleRememberChange = (e) => {
  formData.value.remember = e.detail.value.length > 0
}

const validateForm = () => {
  if (!formData.value.username) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return false
  }

  if (!validateUsername(formData.value.username)) {
    uni.showToast({ title: '用户名格式不正确', icon: 'none' })
    return false
  }

  if (!formData.value.password) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return false
  }

  if (!validatePassword(formData.value.password)) {
    uni.showToast({ title: '密码格式不正确', icon: 'none' })
    return false
  }

  return true
}

const handleLogin = async () => {
  if (!validateForm()) return

  loading.value = true

  try {
    const apiUrl = import.meta.env.VITE_API_BASE_URL
    console.log('开始登录，API地址:', apiUrl)
    console.log('登录信息:', { username: formData.value.username })
    
    // 测试网络连接
    uni.showLoading({ title: '连接服务器...' })
    
    await authStore.login({
      username: formData.value.username,
      password: formData.value.password
    })
    
    uni.hideLoading()

    if (formData.value.remember) {
      CacheManager.set('saved_username', formData.value.username, 'permanent')
      CacheManager.set('saved_password', formData.value.password, 'permanent')
    } else {
      CacheManager.remove('saved_username')
      CacheManager.remove('saved_password')
    }

    const homePage = authStore.getRoleHomePage()
    
    if (!homePage) {
      uni.showToast({
        title: '获取首页失败',
        icon: 'none'
      })
      return
    }
    
    uni.showToast({ 
      title: '登录成功', 
      icon: 'success',
      duration: 1500
    })

    setTimeout(() => {
      uni.reLaunch({
        url: homePage
      })
    }, 1500)
  } catch (error) {
    uni.hideLoading()
    console.error('登录失败:', error)
    
    let errorMsg = '登录失败，请重试'
    if (error.errMsg && error.errMsg.includes('timeout')) {
      errorMsg = '连接超时，请检查网络或后端地址配置'
    } else if (error.message) {
      errorMsg = error.message
    }
    
    uni.showToast({
      title: errorMsg,
      icon: 'none',
      duration: 3000
    })
  } finally {
    loading.value = false
  }
}

const handleForgotPassword = () => {
  uni.navigateTo({
    url: `/pages/common/forgot-password/forgot-password?role=${selectedRole.value}`
  })
}

const handleRegister = () => {
  uni.navigateTo({
    url: `/pages/auth/register/register?role=${selectedRole.value}`
  })
}

const handleWechatLogin = () => {
  uni.showToast({
    title: '微信登录功能开发中',
    icon: 'none'
  })
}
</script>

<style scoped>
/* 极简纯白背景 */
.login-page {
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0;
}

/* Logo区域 */
.logo-section {
  width: 100%;
  padding: 100rpx 0 80rpx;
  text-align: center;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 角色logo图标 */
.logo-icon {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.role-emoji {
  font-size: 100rpx;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10rpx); }
}

/* 学生角色 - 蓝色背景 */
.logo-icon.role-student {
  background: linear-gradient(135deg, rgba(0, 82, 217, 0.1) 0%, rgba(0, 65, 168, 0.1) 100%);
}

/* 教师角色 - 绿色背景 */
.logo-icon.role-teacher {
  background: linear-gradient(135deg, rgba(82, 196, 26, 0.1) 0%, rgba(56, 158, 13, 0.1) 100%);
}

/* 管理员角色 - 橙色背景 */
.logo-icon.role-admin {
  background: linear-gradient(135deg, rgba(255, 140, 66, 0.1) 0%, rgba(255, 184, 77, 0.1) 100%);
}

.app-title {
  font-size: 48rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 12rpx;
  letter-spacing: 2rpx;
}

.app-subtitle {
  font-size: 24rpx;
  color: #999;
  font-weight: 400;
  letter-spacing: 1rpx;
}

/* 登录卡片 */
.login-card {
  width: 670rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx 40rpx;
  box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.08);
  margin: 0 auto;
}

/* 身份选择器 */
.role-selector {
  display: flex;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 6rpx;
  margin-bottom: 48rpx;
}

.role-item {
  flex: 1;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8rpx;
  transition: all 0.3s ease;
  cursor: pointer;
}

.role-text {
  font-size: 28rpx;
  color: #666;
  font-weight: 500;
  transition: all 0.3s ease;
}

.role-item.active {
  background: #ffffff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

/* 根据角色显示不同颜色 */
.login-card.role-student .role-item.active .role-text {
  color: #0052d9;
  font-weight: 600;
}

.login-card.role-teacher .role-item.active .role-text {
  color: #52c41a;
  font-weight: 600;
}

.login-card.role-admin .role-item.active .role-text {
  color: #FF8C42;
  font-weight: 600;
}

/* 登录表单 */
.login-form {
  width: 100%;
}

.form-item {
  margin-bottom: 32rpx;
}

/* 扁平化输入框 */
.form-input {
  width: 100%;
  height: 96rpx;
  padding: 0 32rpx;
  font-size: 28rpx;
  color: #333;
  background: #f8f8f8;
  border: 2rpx solid #f8f8f8;
  border-radius: 12rpx;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

/* 输入框聚焦时根据角色显示不同颜色 */
.login-card.role-student .form-input:focus {
  background: #ffffff;
  border-color: #0052d9;
}

.login-card.role-teacher .form-input:focus {
  background: #ffffff;
  border-color: #52c41a;
}

.login-card.role-admin .form-input:focus {
  background: #ffffff;
  border-color: #FF8C42;
}

.form-input::placeholder {
  color: #bbb;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.remember-checkbox {
  display: flex;
  align-items: center;
}

.checkbox-label {
  margin-left: 12rpx;
  font-size: 26rpx;
  color: #666;
}

/* 忘记密码链接根据角色显示不同颜色 */
.login-card.role-student .forgot-link {
  font-size: 26rpx;
  color: #0052d9;
}

.login-card.role-teacher .forgot-link {
  font-size: 26rpx;
  color: #52c41a;
}

.login-card.role-admin .forgot-link {
  font-size: 26rpx;
  color: #FF8C42;
}

/* 登录按钮根据角色显示不同颜色 */
.login-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  letter-spacing: 4rpx;
  transition: all 0.3s ease;
}

/* 学生角色 - 蓝色按钮 */
.login-card.role-student .login-btn {
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
  box-shadow: 0 4rpx 16rpx rgba(0, 82, 217, 0.3);
}

.login-card.role-student .login-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 2rpx 8rpx rgba(0, 82, 217, 0.3);
}

/* 教师角色 - 绿色按钮 */
.login-card.role-teacher .login-btn {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  box-shadow: 0 4rpx 16rpx rgba(82, 196, 26, 0.3);
}

.login-card.role-teacher .login-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 2rpx 8rpx rgba(82, 196, 26, 0.3);
}

/* 管理员角色 - 橙色按钮 */
.login-card.role-admin .login-btn {
  background: linear-gradient(135deg, #FF8C42 0%, #FFB84D 100%);
  box-shadow: 0 4rpx 16rpx rgba(255, 140, 66, 0.3);
}

.login-card.role-admin .login-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 2rpx 8rpx rgba(255, 140, 66, 0.3);
}

.login-btn.loading {
  opacity: 0.7;
}

.login-btn[disabled] {
  opacity: 0.6;
}

/* 注册链接 */
.register-link {
  text-align: center;
  font-size: 26rpx;
  margin-top: 32rpx;
}

.link-text {
  color: #999;
}

/* 注册链接根据角色显示不同颜色 */
.login-card.role-student .link-action {
  color: #0052d9;
  margin-left: 8rpx;
  font-weight: 600;
}

.login-card.role-teacher .link-action {
  color: #52c41a;
  margin-left: 8rpx;
  font-weight: 600;
}

.login-card.role-admin .link-action {
  color: #FF8C42;
  margin-left: 8rpx;
  font-weight: 600;
}
</style>
