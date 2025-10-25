<template>
  <div class="login-page">
    <t-form
      ref="loginFormRef"
      :data="loginForm"
      :rules="loginRules"
      :colon="false"
      :label-width="0"
      class="login-form"
      @submit="handleLogin"
    >
      <div class="form-header">
        <h2 class="form-title">用户登录</h2>
        <p class="form-subtitle">欢迎回来，请登录以继续</p>
      </div>
      
      <t-form-item name="username">
        <t-input
          v-model="loginForm.username"
          clearable
          placeholder="请输入用户名/学号/工号"
          size="large"
        >
          <template #prefix-icon>
            <t-icon name="user" />
          </template>
        </t-input>
      </t-form-item>
      
      <t-form-item name="password">
        <t-input
          v-model="loginForm.password"
          type="password"
          clearable
          placeholder="请输入密码"
          size="large"
          @enter="handleLogin"
        >
          <template #prefix-icon>
            <t-icon name="lock-on" />
          </template>
        </t-input>
      </t-form-item>
      
      <t-form-item>
        <div class="form-meta">
          <t-checkbox v-model="loginForm.rememberMe">
            记住我
          </t-checkbox>
          <t-link
            theme="primary"
            hover="color"
            class="forgot-link"
            @click="$router.push('/auth/forgot-password')"
          >
            忘记密码？
          </t-link>
        </div>
      </t-form-item>
      
      <t-form-item class="form-submit">
        <t-button
          theme="primary"
          type="submit"
          block
          size="large"
          :loading="loading"
          class="login-button"
        >
          登录
        </t-button>
      </t-form-item>
      
      <div class="form-footer">
        <span>还没有账号？</span>
        <t-link theme="primary" hover="color" @click="$router.push('/auth/register')">
          立即注册
        </t-link>
      </div>
    </t-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async ({ validateResult, firstError }) => {
  if (validateResult !== true) {
    MessagePlugin.warning(firstError || '请检查表单输入')
    return
  }

  try {
    loading.value = true
    
    const credentials = {
      username: loginForm.username,
      password: loginForm.password
    }
    
    const result = await authStore.login(credentials)
    
    // 登录成功后跳转
    const redirectPath = route.query.redirect || getDefaultRoute(result.role)
    router.push(redirectPath)
    
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

const getDefaultRoute = (role) => {
  switch (role) {
    case 'ADMIN':
      return '/admin/dashboard'
    case 'TEACHER':
      return '/teacher/dashboard'
    case 'STUDENT':
      return '/student/dashboard'
    default:
      return '/'
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
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

.form-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  font-size: 14px;
}

.forgot-link {
  font-size: 14px;
  cursor: pointer;
}

.form-submit {
  margin-top: 8px;
  margin-bottom: 0;
}

.login-button {
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.form-footer {
  text-align: center;
  margin-top: 8px;
  color: #94a3b8;
  font-size: 14px;

  span {
    margin-right: 6px;
  }
}

// TDesign 组件样式调整
:deep(.t-input) {
  border-radius: 8px;
  
  .t-input__inner {
    height: 48px;
    font-size: 15px;
  }
}

:deep(.t-form__item) {
  margin-bottom: 0;
}

:deep(.t-checkbox) {
  font-size: 14px;
}

:deep(.t-link) {
  font-size: 14px;
  text-decoration: none;
}
</style>
