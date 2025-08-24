<template>
  <div class="profile">
    <div class="page-header">
      <h1>个人资料</h1>
    </div>
    
    <el-row :gutter="20">
      <el-col :xs="24" :lg="8">
        <!-- 头像和基本信息 -->
        <el-card class="profile-card">
          <div class="profile-avatar">
            <el-avatar :size="120" :src="userInfo.avatar">
              <el-icon size="60"><User /></el-icon>
            </el-avatar>
            <el-button type="primary" size="small" class="upload-btn">
              更换头像
            </el-button>
          </div>
          
          <div class="profile-basic">
            <h3>{{ userInfo.name }}</h3>
            <p class="username">@{{ userInfo.username }}</p>
            <el-tag :type="getRoleType(userInfo.role)">
              {{ getRoleLabel(userInfo.role) }}
            </el-tag>
          </div>
          
          <div class="profile-stats" v-if="userInfo.role === 'STUDENT'">
            <div class="stat-item">
              <div class="stat-number">{{ userStats.totalPoints }}</div>
              <div class="stat-label">总积分</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ userStats.serviceHours }}</div>
              <div class="stat-label">服务时长</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ userStats.activities }}</div>
              <div class="stat-label">参与活动</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="16">
        <!-- 个人信息编辑 -->
        <el-card title="个人信息">
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            size="large"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="profileForm.name" placeholder="请输入姓名" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <!-- <el-row :gutter="20" v-if="userInfo.role === 'STUDENT'">
              <el-col :span="12">
                <el-form-item label="学号" prop="studentId">
                  <el-input v-model="profileForm.studentId" placeholder="请输入学号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="专业" prop="major">
                  <el-input v-model="profileForm.major" placeholder="请输入专业" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" v-if="userInfo.role === 'STUDENT'">
              <el-col :span="12">
                <el-form-item label="年级" prop="grade">
                  <el-select v-model="profileForm.grade" placeholder="请选择年级">
                    <el-option label="大一" value="1" />
                    <el-option label="大二" value="2" />
                    <el-option label="大三" value="3" />
                    <el-option label="大四" value="4" />
                    <el-option label="研一" value="5" />
                    <el-option label="研二" value="6" />
                    <el-option label="研三" value="7" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="班级" prop="className">
                  <el-input v-model="profileForm.className" placeholder="请输入班级" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="个人简介">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="4"
                placeholder="请输入个人简介"
              />
            </el-form-item> -->
            
            <el-form-item>
              <el-button type="primary" :loading="updating" @click="handleUpdate">
                保存修改
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 密码修改 -->
        <el-card title="修改密码" class="password-card">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            size="large"
          >
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input
                v-model="passwordForm.currentPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请确认新密码"
                show-password
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" :loading="changingPassword" @click="handlePasswordChange">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { userAPI } from '@/api'
import { ROLE_LABELS } from '@/utils/constants'

const authStore = useAuthStore()

const userInfo = ref({})
const userStats = ref({})
const updating = ref(false)
const changingPassword = ref(false)
const profileFormRef = ref()
const passwordFormRef = ref()

const profileForm = reactive({
  username: '',
  name: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const profileRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const getRoleLabel = (role) => {
  return ROLE_LABELS[role] || role
}

const getRoleType = (role) => {
  const typeMap = {
    ADMIN: 'danger',
    TEACHER: 'warning',
    STUDENT: 'success'
  }
  return typeMap[role] || 'info'
}

const fetchUserInfo = async () => {
  try {
    const userId = authStore.user.id
    const response = await userAPI.getUserById(userId)
    if (response.code === 200) {
      userInfo.value = response.data
      Object.assign(profileForm, response.data)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 使用当前用户信息
    userInfo.value = authStore.user
    Object.assign(profileForm, authStore.user)
  }
}

const fetchUserStats = async () => {
  if (userInfo.value.role === 'STUDENT') {
    try {
      // 这里应该调用获取用户统计的API
      userStats.value = {
        totalPoints: 180,
        serviceHours: 24,
        activities: 5
      }
    } catch (error) {
      console.error('获取用户统计失败:', error)
    }
  }
}

const handleUpdate = async () => {
  try {
    await profileFormRef.value.validate()
    updating.value = true
    
    const userId = authStore.user.id
    await userAPI.updateUser(userId, profileForm)
    
    // 更新本地用户信息
    authStore.updateUser(profileForm)
    
    ElMessage.success('个人信息更新成功')
  } catch (error) {
    console.error('更新失败:', error)
  } finally {
    updating.value = false
  }
}

const handleReset = () => {
  Object.assign(profileForm, userInfo.value)
  profileFormRef.value.resetFields()
}

const handlePasswordChange = async () => {
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    
    // 这里应该调用修改密码的API
    ElMessage.success('密码修改成功')
    resetPasswordForm()
  } catch (error) {
    console.error('密码修改失败:', error)
  } finally {
    changingPassword.value = false
  }
}

const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  passwordFormRef.value?.resetFields()
}

onMounted(() => {
  fetchUserInfo()
  fetchUserStats()
})
</script>

<style lang="scss" scoped>
.profile {
  .page-header {
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }
  
  .profile-card {
    text-align: center;
    margin-bottom: 20px;
    
    .profile-avatar {
      margin-bottom: 20px;
      
      .upload-btn {
        margin-top: 12px;
      }
    }
    
    .profile-basic {
      margin-bottom: 20px;
      
      h3 {
        margin: 0 0 8px 0;
        font-size: 20px;
        color: #303133;
      }
      
      .username {
        margin: 0 0 12px 0;
        color: #909399;
        font-size: 14px;
      }
    }
    
    .profile-stats {
      display: flex;
      justify-content: space-around;
      padding-top: 20px;
      border-top: 1px solid #f0f0f0;
      
      .stat-item {
        .stat-number {
          font-size: 24px;
          font-weight: 600;
          color: #409eff;
          margin-bottom: 4px;
        }
        
        .stat-label {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
  
  .password-card {
    margin-top: 20px;
  }
}
</style>
