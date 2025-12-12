<template>
  <view class="register-page" :class="'role-' + registerRole.toLowerCase()">
    <view class="register-form">
      <view class="form-item">
        <text class="form-label">用户名</text>
        <input 
          v-model="formData.username" 
          class="form-input" 
          :placeholder="getUsernamePlaceholder()"
          :maxlength="20"
        />
      </view>
      
      <view class="form-item">
        <text class="form-label">真实姓名</text>
        <input 
          v-model="formData.name" 
          class="form-input" 
          placeholder="请输入真实姓名"
          :maxlength="20"
        />
      </view>
      
      <view class="form-item">
        <text class="form-label">邮箱</text>
        <view class="input-with-btn">
          <input 
            v-model="formData.email" 
            class="form-input flex-1" 
            placeholder="请输入邮箱"
            type="text"
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
      
      <view class="form-item">
        <text class="form-label">验证码</text>
        <input 
          v-model="formData.code" 
          class="form-input" 
          placeholder="请输入邮箱验证码"
          :maxlength="6"
        />
      </view>
      
      <view class="form-item">
        <text class="form-label">手机号</text>
        <input 
          v-model="formData.phone" 
          class="form-input" 
          placeholder="请输入手机号"
          type="number"
          :maxlength="11"
        />
      </view>
      
      <view class="form-item">
        <text class="form-label">密码</text>
        <input 
          v-model="formData.password" 
          class="form-input" 
          type="password"
          placeholder="请输入密码（6-20位）"
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
          placeholder="请再次输入密码"
          :maxlength="20"
        />
      </view>
      
      <button 
        class="register-btn" 
        :disabled="loading"
        @click="handleRegister"
      >
        {{ loading ? '注册中...' : '注册' }}
      </button>
      
      <view class="login-link">
        <text class="link-text">已有账号？</text>
        <text class="link-action" @click="handleGoLogin">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { sendCode, register } from '@/api/auth'
import { 
  validateUsername, 
  validateEmail, 
  validatePhone, 
  validatePassword,
  checkPasswordStrength 
} from '@/utils/validate'

const formData = ref({
  username: '',
  name: '',
  email: '',
  code: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const registerRole = ref('STUDENT') // 默认学生身份

onMounted(() => {
  // 获取URL参数中的角色
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || currentPage.$page?.options || {}
  
  if (options.role) {
    registerRole.value = options.role
  }
})

// 根据角色获取用户名输入框提示
const getUsernamePlaceholder = () => {
  const placeholderMap = {
    'STUDENT': '请输入学号',
    'TEACHER': '请输入工号',
    'ADMIN': '请输入用户名'
  }
  return placeholderMap[registerRole.value] || '请输入学号'
}

const loading = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

// 密码强度
const passwordStrength = computed(() => {
  if (!formData.value.password) return 0
  return checkPasswordStrength(formData.value.password)
})

const passwordStrengthText = computed(() => {
  const texts = ['', '弱', '中', '强']
  return texts[passwordStrength.value] || ''
})

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
      type: 'register'
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

// 表单验证
const validateForm = () => {
  if (!formData.value.username) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return false
  }
  
  if (!validateUsername(formData.value.username)) {
    uni.showToast({ title: '用户名格式不正确（4-20位字母数字）', icon: 'none' })
    return false
  }
  
  if (!formData.value.name) {
    uni.showToast({ title: '请输入真实姓名', icon: 'none' })
    return false
  }
  
  if (!formData.value.email) {
    uni.showToast({ title: '请输入邮箱', icon: 'none' })
    return false
  }
  
  if (!validateEmail(formData.value.email)) {
    uni.showToast({ title: '邮箱格式不正确', icon: 'none' })
    return false
  }
  
  if (!formData.value.code) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return false
  }
  
  if (!formData.value.phone) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return false
  }
  
  if (!validatePhone(formData.value.phone)) {
    uni.showToast({ title: '手机号格式不正确', icon: 'none' })
    return false
  }
  
  if (!formData.value.password) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return false
  }
  
  if (!validatePassword(formData.value.password)) {
    uni.showToast({ title: '密码格式不正确（6-20位）', icon: 'none' })
    return false
  }
  
  if (passwordStrength.value < 2) {
    uni.showToast({ title: '密码强度太弱，请使用更复杂的密码', icon: 'none' })
    return false
  }
  
  if (formData.value.password !== formData.value.confirmPassword) {
    uni.showToast({ title: '两次密码输入不一致', icon: 'none' })
    return false
  }
  
  return true
}

// 注册
const handleRegister = async () => {
  if (!validateForm()) return
  
  loading.value = true
  
  try {
    await register({
      username: formData.value.username,
      name: formData.value.name,
      email: formData.value.email,
      code: formData.value.code,
      phone: formData.value.phone,
      password: formData.value.password,
      role: registerRole.value // 传递角色信息
    })
    
    uni.showToast({ title: '注册成功', icon: 'success' })
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

// 返回登录
const handleGoLogin = () => {
  // 使用 reLaunch 跳转到登录页，因为可能是从展示页直接跳转过来的
  uni.reLaunch({
    url: '/pages/index/index'
  })
}

// 清理定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.register-page {
  height: 100vh;
  background-color: #f5f5f5;
  padding: 32rpx;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.register-form {
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
.register-page.role-student .code-btn {
  background: #0052d9;
}

/* 教师角色 - 绿色 */
.register-page.role-teacher .code-btn {
  background: #52c41a;
}

/* 管理员角色 - 橙色 */
.register-page.role-admin .code-btn {
  background: #FF8C42;
}

.code-btn[disabled] {
  opacity: 0.6;
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

.register-btn {
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
.register-page.role-student .register-btn {
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
}

/* 教师角色 - 绿色 */
.register-page.role-teacher .register-btn {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

/* 管理员角色 - 橙色 */
.register-page.role-admin .register-btn {
  background: linear-gradient(135deg, #FF8C42 0%, #FFB84D 100%);
}

.register-btn[disabled] {
  opacity: 0.6;
}

.login-link {
  text-align: center;
  font-size: 26rpx;
}

.link-text {
  color: #999;
}

/* 登录链接根据角色显示不同颜色 */
.register-page.role-student .link-action {
  color: #0052d9;
  margin-left: 8rpx;
}

.register-page.role-teacher .link-action {
  color: #52c41a;
  margin-left: 8rpx;
}

.register-page.role-admin .link-action {
  color: #FF8C42;
  margin-left: 8rpx;
}
</style>
