<template>
  <div class="forgot-password-page">
    <el-form
      ref="forgotFormRef"
      :model="forgotForm"
      :rules="forgotRules"
      label-width="0"
      size="large"
    >
      <h2 class="form-title">找回密码</h2>
      <p class="form-subtitle">请选择验证方式来重置您的密码</p>
      
      <!-- 第一步：选择验证方式 -->
      <div v-if="currentStep === 1" class="step-content">
        <el-form-item prop="verifyType">
          <el-radio-group v-model="forgotForm.verifyType" @change="handleVerifyTypeChange">
            <el-radio label="email">邮箱验证</el-radio>
            <el-radio label="phone">手机验证</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item v-if="forgotForm.verifyType === 'email'" prop="email">
          <el-input
            v-model="forgotForm.email"
            placeholder="请输入您的邮箱地址"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>
        
        <el-form-item v-if="forgotForm.verifyType === 'phone'" prop="phone">
          <el-input
            v-model="forgotForm.phone"
            placeholder="请输入您的手机号码"
            prefix-icon="Phone"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleSendCode"
          >
            发送验证码
          </el-button>
        </el-form-item>
      </div>
      
      <!-- 第二步：验证验证码 -->
      <div v-if="currentStep === 2" class="step-content">
        <div class="verify-info">
          <el-icon size="48" color="#67c23a"><CircleCheck /></el-icon>
          <p>验证码已发送至</p>
          <p class="contact-info">{{ maskedContact }}</p>
        </div>
        
        <el-form-item prop="verifyCode">
          <el-input
            v-model="forgotForm.verifyCode"
            placeholder="请输入6位验证码"
            prefix-icon="Key"
            maxlength="6"
            clearable
            @keyup.enter="handleVerifyCode"
          />
        </el-form-item>
        
        <div class="resend-section">
          <span>没有收到验证码？</span>
          <el-button
            type="text"
            :disabled="countdown > 0"
            @click="handleResendCode"
          >
            {{ countdown > 0 ? `${countdown}秒后重新发送` : '重新发送' }}
          </el-button>
        </div>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleVerifyCode"
          >
            验证
          </el-button>
        </el-form-item>
      </div>
      
      <!-- 步骤指示器 -->
      <div class="steps-indicator">
        <div class="step" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
          <div class="step-number">1</div>
          <div class="step-label">选择验证方式</div>
        </div>
        <div class="step-line" :class="{ active: currentStep > 1 }"></div>
        <div class="step" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
          <div class="step-number">2</div>
          <div class="step-label">验证身份</div>
        </div>
        <div class="step-line" :class="{ active: currentStep > 2 }"></div>
        <div class="step" :class="{ active: currentStep >= 3 }">
          <div class="step-number">3</div>
          <div class="step-label">重置密码</div>
        </div>
      </div>
      
      <div class="form-footer">
        <span>想起密码了？</span>
        <el-link type="primary" @click="$router.push('/auth/login')">
          返回登录
        </el-link>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck } from '@element-plus/icons-vue'
import { authAPI } from '@/api'
import { useCountdown } from '@/composables/useCountdown'

const router = useRouter()

const forgotFormRef = ref()
const loading = ref(false)
const currentStep = ref(1)
const { countdown, start: startCountdown } = useCountdown(60)

const forgotForm = reactive({
  verifyType: 'email',
  email: '',
  phone: '',
  verifyCode: ''
})

const validateEmail = (rule, value, callback) => {
  if (forgotForm.verifyType === 'email' && !value) {
    callback(new Error('请输入邮箱地址'))
  } else if (forgotForm.verifyType === 'email' && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (forgotForm.verifyType === 'phone' && !value) {
    callback(new Error('请输入手机号码'))
  } else if (forgotForm.verifyType === 'phone' && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号码'))
  } else {
    callback()
  }
}

const forgotRules = {
  verifyType: [
    { required: true, message: '请选择验证方式', trigger: 'change' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ]
}

const maskedContact = computed(() => {
  if (forgotForm.verifyType === 'email' && forgotForm.email) {
    const [name, domain] = forgotForm.email.split('@')
    return `${name.substring(0, 2)}***@${domain}`
  } else if (forgotForm.verifyType === 'phone' && forgotForm.phone) {
    return `${forgotForm.phone.substring(0, 3)}****${forgotForm.phone.substring(7)}`
  }
  return ''
})

const handleVerifyTypeChange = () => {
  // 清空表单验证
  forgotFormRef.value?.clearValidate()
  forgotForm.email = ''
  forgotForm.phone = ''
}



const handleSendCode = async () => {
  try {
    await forgotFormRef.value.validate()
    loading.value = true
    
    const sendData = {
      type: forgotForm.verifyType,
      contact: forgotForm.verifyType === 'email' ? forgotForm.email : forgotForm.phone
    }
    
    const response = await authAPI.sendCode(sendData)
    if (response.code === 200) {
      ElMessage.success('验证码发送成功')
      currentStep.value = 2
      startCountdown()
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error('发送验证码失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleResendCode = async () => {
  try {
    loading.value = true
    
    const sendData = {
      type: forgotForm.verifyType,
      contact: forgotForm.verifyType === 'email' ? forgotForm.email : forgotForm.phone
    }
    
    const response = await authAPI.sendCode(sendData)
    if (response.code === 200) {
      ElMessage.success('验证码重新发送成功')
      startCountdown()
    }
  } catch (error) {
    console.error('重新发送验证码失败:', error)
    ElMessage.error('重新发送失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleVerifyCode = async () => {
  try {
    if (!forgotForm.verifyCode) {
      ElMessage.error('请输入验证码')
      return
    }
    
    loading.value = true
    
    const verifyData = {
      type: forgotForm.verifyType,
      contact: forgotForm.verifyType === 'email' ? forgotForm.email : forgotForm.phone,
      code: forgotForm.verifyCode
    }
    
    const response = await authAPI.verifyCode(verifyData)
    if (response.code === 200) {
      ElMessage.success('验证成功')
      // 跳转到重置密码页面，携带验证信息
      router.push({
        name: 'ResetPassword',
        query: {
          token: response.data.token,
          type: forgotForm.verifyType,
          contact: forgotForm.verifyType === 'email' ? forgotForm.email : forgotForm.phone
        }
      })
    }
  } catch (error) {
    console.error('验证码验证失败:', error)
    ElMessage.error('验证码错误，请重新输入')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.forgot-password-page {
  width: 100%;
}

.form-title {
  text-align: center;
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.form-subtitle {
  text-align: center;
  margin: 0 0 30px 0;
  color: #606266;
  font-size: 14px;
}

.step-content {
  margin-bottom: 30px;
}

.verify-info {
  text-align: center;
  margin-bottom: 24px;
  
  p {
    margin: 8px 0;
    color: #606266;
    
    &.contact-info {
      font-weight: 600;
      color: #303133;
    }
  }
}

.resend-section {
  text-align: center;
  margin-bottom: 20px;
  font-size: 14px;
  color: #909399;
  
  .el-button {
    margin-left: 8px;
    padding: 0;
  }
}

.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 30px 0;
  
  .step {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .step-number {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #f5f7fa;
      color: #909399;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      margin-bottom: 8px;
      transition: all 0.3s;
    }
    
    .step-label {
      font-size: 12px;
      color: #909399;
      transition: all 0.3s;
    }
    
    &.active {
      .step-number {
        background: #409eff;
        color: white;
      }
      
      .step-label {
        color: #409eff;
      }
    }
    
    &.completed {
      .step-number {
        background: #67c23a;
        color: white;
      }
      
      .step-label {
        color: #67c23a;
      }
    }
  }
  
  .step-line {
    width: 60px;
    height: 2px;
    background: #f5f7fa;
    margin: 0 16px;
    transition: all 0.3s;
    
    &.active {
      background: #67c23a;
    }
  }
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  color: #909399;
  font-size: 14px;
  
  span {
    margin-right: 8px;
  }
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  padding: 12px 16px;
}

:deep(.el-radio-group) {
  width: 100%;
  display: flex;
  justify-content: space-around;
  
  .el-radio {
    margin-right: 0;
  }
}
</style>
