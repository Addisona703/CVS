<template>
  <div class="login-page">
    <el-form
      ref="loginFormRef"
      :model="loginForm"
      :rules="loginRules"
      label-width="0"
      size="large"
    >
      <h2 class="form-title">用户登录</h2>
      
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          placeholder="请输入用户名/学号/工号"
          prefix-icon="User"
          clearable
        />
      </el-form-item>
      
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          prefix-icon="Lock"
          show-password
          clearable
          @keyup.enter="handleLogin"
        />
      </el-form-item>
      
      <el-form-item>
        <div class="form-options">
          <el-checkbox v-model="loginForm.rememberMe">
            记住我
          </el-checkbox>
          <el-link type="primary" :underline="false" @click="$router.push('/auth/forgot-password')">
            忘记密码？
          </el-link>
        </div>
      </el-form-item>
      
      <el-form-item>
        <el-button
          type="primary"
          size="large"
          style="width: 100%"
          :loading="loading"
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form-item>
      
      <div class="form-footer">
        <span>还没有账号？</span>
        <el-link type="primary" @click="$router.push('/auth/register')">
          立即注册
        </el-link>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

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

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const credentials = {
      username: loginForm.username,
      password: loginForm.password
    }
    
    const result = await authStore.login(credentials)
    console.log(result)
    // 登录成功后跳转
    const redirectPath = route.query.redirect || getDefaultRoute(result.role)
    console.log('route.query.redirect =', route.query.redirect)
    console.log('跳转路径 =', redirectPath)
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
}

.form-title {
  text-align: center;
  margin: 0 0 30px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
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
