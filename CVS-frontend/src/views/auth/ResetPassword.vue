<template>
  <div class="reset-password-page">
    <el-form
      ref="resetFormRef"
      :model="resetForm"
      :rules="resetRules"
      label-width="0"
      size="large"
    >
      <h2 class="form-title">重置密码</h2>
      <p class="form-subtitle">请设置您的新密码</p>
      
      <!-- 验证信息显示 -->
      <div class="verify-info">
        <el-icon size="32" color="#67c23a"><CircleCheck /></el-icon>
        <p>身份验证成功</p>
        <p class="contact-info">{{ maskedContact }}</p>
      </div>
      
      <el-form-item prop="newPassword">
        <el-input
          v-model="resetForm.newPassword"
          type="password"
          placeholder="请输入新密码"
          prefix-icon="Lock"
          show-password
          clearable
        />
        <div class="password-strength">
          <div class="strength-bar">
            <div 
              class="strength-fill" 
              :class="passwordStrengthClass"
              :style="{ width: passwordStrengthWidth }"
            ></div>
          </div>
          <span class="strength-text" :class="passwordStrengthClass">
            {{ passwordStrengthText }}
          </span>
        </div>
      </el-form-item>
      
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="resetForm.confirmPassword"
          type="password"
          placeholder="请确认新密码"
          prefix-icon="Lock"
          show-password
          clearable
          @keyup.enter="handleResetPassword"
        />
      </el-form-item>
      
      <!-- 密码要求提示 -->
      <div class="password-requirements">
        <h4>密码要求：</h4>
        <ul>
          <li :class="{ valid: hasMinLength }">
            <el-icon><Check v-if="hasMinLength" /><Close v-else /></el-icon>
            至少8个字符
          </li>
          <li :class="{ valid: hasUpperCase }">
            <el-icon><Check v-if="hasUpperCase" /><Close v-else /></el-icon>
            包含大写字母
          </li>
          <li :class="{ valid: hasLowerCase }">
            <el-icon><Check v-if="hasLowerCase" /><Close v-else /></el-icon>
            包含小写字母
          </li>
          <li :class="{ valid: hasNumber }">
            <el-icon><Check v-if="hasNumber" /><Close v-else /></el-icon>
            包含数字
          </li>
        </ul>
      </div>
      
      <el-form-item>
        <el-button
          type="primary"
          size="large"
          style="width: 100%"
          :loading="loading"
          @click="handleResetPassword"
        >
          重置密码
        </el-button>
      </el-form-item>
      
      <div class="form-footer">
        <span>记起密码了？</span>
        <el-link type="primary" @click="$router.push('/auth/login')">
          返回登录
        </el-link>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, Check, Close } from '@element-plus/icons-vue'
import { authAPI } from '@/api'

const router = useRouter()
const route = useRoute()

const resetFormRef = ref()
const loading = ref(false)

const resetForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const verifyInfo = reactive({
  token: '',
  type: '',
  contact: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== resetForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 8) {
    callback(new Error('密码长度不能少于8位'))
  } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
    callback(new Error('密码必须包含大小写字母和数字'))
  } else {
    callback()
  }
}

const resetRules = {
  newPassword: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 密码强度检查
const hasMinLength = computed(() => resetForm.newPassword.length >= 8)
const hasUpperCase = computed(() => /[A-Z]/.test(resetForm.newPassword))
const hasLowerCase = computed(() => /[a-z]/.test(resetForm.newPassword))
const hasNumber = computed(() => /\d/.test(resetForm.newPassword))

const passwordStrength = computed(() => {
  let strength = 0
  if (hasMinLength.value) strength++
  if (hasUpperCase.value) strength++
  if (hasLowerCase.value) strength++
  if (hasNumber.value) strength++
  return strength
})

const passwordStrengthClass = computed(() => {
  if (passwordStrength.value <= 1) return 'weak'
  if (passwordStrength.value <= 2) return 'medium'
  if (passwordStrength.value <= 3) return 'good'
  return 'strong'
})

const passwordStrengthWidth = computed(() => {
  return `${(passwordStrength.value / 4) * 100}%`
})

const passwordStrengthText = computed(() => {
  if (passwordStrength.value <= 1) return '弱'
  if (passwordStrength.value <= 2) return '中等'
  if (passwordStrength.value <= 3) return '良好'
  return '强'
})

const maskedContact = computed(() => {
  if (verifyInfo.type === 'email' && verifyInfo.contact) {
    const [name, domain] = verifyInfo.contact.split('@')
    return `${name.substring(0, 2)}***@${domain}`
  } else if (verifyInfo.type === 'phone' && verifyInfo.contact) {
    return `${verifyInfo.contact.substring(0, 3)}****${verifyInfo.contact.substring(7)}`
  }
  return ''
})

const handleResetPassword = async () => {
  try {
    await resetFormRef.value.validate()
    loading.value = true
    
    const resetData = {
      token: verifyInfo.token,
      newPassword: resetForm.newPassword
    }
    
    const response = await authAPI.resetPassword(resetData)
    if (response.code === 200) {
      ElMessage.success('密码重置成功，请使用新密码登录')
      router.push('/auth/login')
    }
  } catch (error) {
    console.error('重置密码失败:', error)
    ElMessage.error('重置密码失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 获取验证信息
  const { token, type, contact } = route.query
  
  if (!token || !type || !contact) {
    ElMessage.error('验证信息无效，请重新操作')
    router.push('/auth/forgot-password')
    return
  }
  
  verifyInfo.token = token
  verifyInfo.type = type
  verifyInfo.contact = contact
})
</script>

<style lang="scss" scoped>
.reset-password-page {
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

.verify-info {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #e1f5fe;
  
  p {
    margin: 8px 0;
    color: #606266;
    
    &.contact-info {
      font-weight: 600;
      color: #303133;
    }
  }
}

.password-strength {
  margin-top: 8px;
  
  .strength-bar {
    height: 4px;
    background: #f5f7fa;
    border-radius: 2px;
    overflow: hidden;
    margin-bottom: 4px;
    
    .strength-fill {
      height: 100%;
      transition: all 0.3s;
      
      &.weak {
        background: #f56c6c;
      }
      
      &.medium {
        background: #e6a23c;
      }
      
      &.good {
        background: #409eff;
      }
      
      &.strong {
        background: #67c23a;
      }
    }
  }
  
  .strength-text {
    font-size: 12px;
    
    &.weak {
      color: #f56c6c;
    }
    
    &.medium {
      color: #e6a23c;
    }
    
    &.good {
      color: #409eff;
    }
    
    &.strong {
      color: #67c23a;
    }
  }
}

.password-requirements {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  
  h4 {
    margin: 0 0 12px 0;
    font-size: 14px;
    color: #303133;
  }
  
  ul {
    margin: 0;
    padding: 0;
    list-style: none;
    
    li {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      font-size: 14px;
      color: #909399;
      
      .el-icon {
        margin-right: 8px;
        font-size: 16px;
      }
      
      &.valid {
        color: #67c23a;
      }
      
      &:last-child {
        margin-bottom: 0;
      }
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
</style>
