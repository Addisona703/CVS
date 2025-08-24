<template>
  <div class="register-page">
    <el-form
      ref="registerFormRef"
      :model="registerForm"
      :rules="registerRules"
      label-width="0"
      size="large"
    >
      <h2 class="form-title">用户注册</h2>
      
      <el-form-item prop="username">
        <el-input
          v-model="registerForm.username"
          placeholder="请输入用户名/学号/工号"
          prefix-icon="User"
          clearable
        />
      </el-form-item>
      
      <el-form-item prop="name">
        <el-input
          v-model="registerForm.name"
          placeholder="请输入真实姓名"
          prefix-icon="Avatar"
          clearable
        />
      </el-form-item>
      
      <el-form-item prop="role">
        <el-select
          v-model="registerForm.role"
          placeholder="请选择角色"
          style="width: 100%"
        >
          <el-option label="学生" value="STUDENT" />
          <el-option label="教师" value="TEACHER" />
        </el-select>
      </el-form-item>
      
      <el-form-item prop="email">
        <el-input
          v-model="registerForm.email"
          placeholder="请输入邮箱地址"
          prefix-icon="Message"
          clearable
        />
      </el-form-item>
      
      <el-form-item prop="phone">
        <el-input
          v-model="registerForm.phone"
          placeholder="请输入手机号码"
          prefix-icon="Phone"
          clearable
        />
      </el-form-item>
      
      <el-form-item prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          placeholder="请输入密码"
          prefix-icon="Lock"
          show-password
          clearable
        />
      </el-form-item>
      
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          placeholder="请确认密码"
          prefix-icon="Lock"
          show-password
          clearable
        />
      </el-form-item>
      
      <el-form-item>
        <el-checkbox v-model="registerForm.agreement">
          我已阅读并同意
          <el-link type="primary" :underline="false">用户协议</el-link>
          和
          <el-link type="primary" :underline="false">隐私政策</el-link>
        </el-checkbox>
      </el-form-item>
      
      <el-form-item>
        <el-button
          type="primary"
          size="large"
          style="width: 100%"
          :loading="loading"
          @click="handleRegister"
        >
          注册
        </el-button>
      </el-form-item>
      
      <div class="form-footer">
        <span>已有账号？</span>
        <el-link type="primary" @click="$router.push('/auth/login')">
          立即登录
        </el-link>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()

const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  name: '',
  role: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreement: [
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意用户协议'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ]
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    const registerData = {
      username: registerForm.username,
      name: registerForm.name,
      role: registerForm.role,
      email: registerForm.email,
      phone: registerForm.phone,
      password: registerForm.password
    }
    
    const response = await authAPI.register(registerData)
    
    if (response.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/auth/login')
    }
    
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  width: 100%;
}

.form-title {
  text-align: center;
  margin: 0 0 30px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
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
  margin-bottom: 18px;
}

:deep(.el-input__wrapper) {
  padding: 12px 16px;
}

:deep(.el-checkbox__label) {
  font-size: 14px;
  line-height: 1.5;
}
</style>
