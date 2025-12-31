<template>
  <div class="reset-password">
    <div class="form-header">
      <h2 class="form-title">重置密码</h2>
      <p class="form-subtitle">请设置一个安全的新密码</p>
    </div>

    <el-form
      ref="resetFormRef"
      :model="resetForm"
      :rules="resetRules"
      label-width="0"
      size="large"
      class="reset-form"
    >
      <el-form-item prop="newPassword">
        <el-input
          v-model="resetForm.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
          clearable
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <el-input
          v-model="resetForm.confirmPassword"
          type="password"
          placeholder="请确认新密码"
          show-password
          clearable
          @keyup.enter="handleSubmit"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <div class="password-hints">
        <span>• 至少6个字符</span>
        <span>• 必须包含字母</span>
      </div>

      <el-button
        type="primary"
        size="large"
        class="primary-btn"
        :loading="loading"
        @click="handleSubmit"
      >
        完成重置
      </el-button>

      <div class="form-footer">
        <el-link type="primary" class="back-link" @click="router.push('/auth/login')">
          返回登录
        </el-link>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { authAPI } from '@/api'

const router = useRouter()
const route = useRoute()
const resetFormRef = ref()
const loading = ref(false)
const token = ref('')

const resetForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validatePassword = (rule, value, callback) => {
  if (!value || value.length < 6) {
    callback(new Error('请输入至少6位密码'))
  } else if (!/[A-Za-z]/.test(value)) {
    callback(new Error('密码需包含字母'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== resetForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const resetRules = {
  newPassword: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

onMounted(() => {
  const queryToken = route.query.token
  if (!queryToken || typeof queryToken !== 'string') {
    ElMessage.error('链接无效或已过期')
    router.replace('/auth/login')
    return
  }
  token.value = queryToken
})

const handleSubmit = async () => {
  if (!resetFormRef.value) return
  try {
    await resetFormRef.value.validate()
    loading.value = true
    const payload = {
      token: token.value,
      newPassword: resetForm.newPassword
    }
    const res = await authAPI.resetPassword(payload)
    if (res.code === 200) {
      ElMessage.success('密码重置成功，请使用新密码登录')
      router.push('/auth/login')
    }
  } catch (error) {
    const backendCode = error.response?.data?.code
    if (backendCode === 1006 || backendCode === 1001) {
      ElMessage.error('链接无效或已过期')
      router.replace('/auth/forgot-password')
    } else if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (!error?.fields) {
      ElMessage.error('重置密码失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.reset-password {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.form-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
}

.form-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  letter-spacing: -0.01em;
}

.form-subtitle {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.reset-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.password-hints {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #9ca3af;
  margin-top: -8px;
  padding-left: 4px;
}

.primary-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 0.02em;
  margin-top: 8px;
  background-color: #409EFF !important;
  border-color: #409EFF !important;
  color: #FFFFFF !important;
}

.primary-btn:hover {
  background-color: #66B1FF !important;
  border-color: #66B1FF !important;
}

.primary-btn:active {
  background-color: #3A8EE6 !important;
  border-color: #3A8EE6 !important;
}

.form-footer {
  text-align: center;
  margin-top: 8px;
}

.back-link {
  font-size: 14px;
  text-decoration: none;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 12px 16px;
  height: 48px;
  box-shadow: none;
  transition: all 0.2s ease;
}

:deep(.el-input__inner) {
  font-size: 15px;
}

:deep(.el-input__prefix) {
  font-size: 16px;
  color: #9ca3af;
}
</style>
