<template>
  <div class="reset-password">
    <div class="form-header">
      <h2>Set New Password</h2>
      <p>Please set a secure new password.</p>
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
          placeholder="New Password"
          show-password
          clearable
        />
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <el-input
          v-model="resetForm.confirmPassword"
          type="password"
          placeholder="Confirm New Password"
          show-password
          clearable
          @keyup.enter="handleSubmit"
        />
      </el-form-item>

      <div class="password-hints">
        <span>• At least 6 characters</span>
        <span>• Must contain letters</span>
      </div>

      <el-button
        type="primary"
        size="large"
        class="primary-btn"
        :loading="loading"
        @click="handleSubmit"
      >
        Complete Reset
      </el-button>

      <el-link type="primary" class="back-link" @click="router.push('/auth/login')">
        Return to Login
      </el-link>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
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
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.form-header {
  display: flex;
  flex-direction: column;
  gap: 8px;

  h2 {
    margin: 0;
    font-size: 32px;
    font-weight: 700;
    color: #1f2937;
    letter-spacing: -0.01em;
  }

  p {
    margin: 0;
    font-size: 14px;
    color: #6b7280;
  }
}

.reset-form {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.password-hints {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  color: #9ca3af;
  margin-top: -12px;
}

.primary-btn {
  width: 100%;
  height: 52px;
  border-radius: 18px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.back-link {
  margin-top: 24px;
  font-size: 14px;
  align-self: center;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-form-item__content) {
  border-bottom: none;
  padding-bottom: 0;
}

:deep(.el-form-item__inner) {
  padding-bottom: 0;
}

:deep(.el-input__wrapper) {
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  padding: 14px 18px;
  background-color: rgba(249, 250, 251, 0.85);
  box-shadow: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    border-color: rgba(37, 99, 235, 0.55);
  }

  &.is-focus {
    border-color: rgba(37, 99, 235, 0.85);
    box-shadow: 0 12px 24px rgba(37, 99, 235, 0.15);
  }
}
</style>
