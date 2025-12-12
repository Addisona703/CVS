<template>
  <el-dialog v-model="visible" width="720px" :close-on-click-modal="false" :show-close="false" class="profile-dialog">
    <template #header>
      <div class="dialog-header">
        <div class="header-content">
          <el-avatar :size="50" :src="userAvatar" class="user-avatar">
            <el-icon size="40">
              <User />
            </el-icon>
          </el-avatar>
          <div class="user-info"  >
            <div class="user-name">
              {{ userInfo.name || userInfo.username }}
              <el-tag :type="getRoleType(userInfo.role)" size="small">{{ getRoleLabel(userInfo.role) }}</el-tag>
            </div>
            <div class="user-username">@{{ userInfo.username }}</div>
          </div>
        </div>
        <el-button link type="info" @click="handleClose" class="close-btn">
          <el-icon size="20">
            <Close />
          </el-icon>
        </el-button>
      </div>
    </template>

    <div class="dialog-body">
      <t-tabs v-model="activeTab" class="profile-tabs" :class="`role-${userInfo.role?.toLowerCase()}`"
        :style="{ '--role-color': getRoleThemeColor(userInfo.role) }">
        <t-tab-panel value="profile" label="基本信息">
          <t-form ref="profileFormRef" :data="profileForm" :rules="profileRules" label-align="left" :label-width="80"
            class="profile-form">
            <t-form-item label="用户名" name="username">
              <t-input v-model="profileForm.username" disabled />
            </t-form-item>

            <t-form-item label="姓名" name="name">
              <t-input v-model="profileForm.name" placeholder="请输入姓名" />
            </t-form-item>

            <t-form-item label="邮箱" name="email">
              <t-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </t-form-item>

            <t-form-item label="手机号" name="phone">
              <t-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </t-form-item>

            <t-form-item style="margin-bottom: 0;">
              <t-space size="medium">
                <t-button theme="primary" :loading="saving" @click="handleSave"
                  :style="{ backgroundColor: getRoleThemeColor(userInfo.role), borderColor: getRoleThemeColor(userInfo.role) }">
                  保存修改
                </t-button>
                <t-button theme="default" variant="outline" @click="handleReset">重置</t-button>
              </t-space>
            </t-form-item>
          </t-form>
        </t-tab-panel>

        <t-tab-panel value="password" label="修改密码">
          <t-form ref="passwordFormRef" :data="passwordForm" :rules="passwordRules" label-align="left" :label-width="80"
            class="password-form">
            <t-form-item label="原密码" name="oldPassword">
              <t-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" clearable />
            </t-form-item>

            <t-form-item label="新密码" name="newPassword">
              <t-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" clearable />
            </t-form-item>

            <t-form-item label="确认密码" name="confirmPassword">
              <t-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" clearable />
            </t-form-item>

            <t-form-item style="margin-bottom: 0;">
              <t-space size="medium">
                <t-button theme="primary" :loading="resetLoading" @click="handlePasswordReset"
                  :style="{ backgroundColor: getRoleThemeColor(userInfo.role), borderColor: getRoleThemeColor(userInfo.role) }">
                  重置密码
                </t-button>
                <t-button theme="default" variant="outline" @click="resetPasswordForm">清空</t-button>
              </t-space>
            </t-form-item>
          </t-form>
        </t-tab-panel>
      </t-tabs>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Close } from '@element-plus/icons-vue'
import { ROLE_LABELS } from '@/utils/constants'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { authAPI, userAPI, serviceRecordAPI } from '@/api'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: undefined
  }
})

const controlled = computed(() => props.modelValue !== undefined)

const emit = defineEmits(['update:modelValue'])

const authStore = useAuthStore()
const router = useRouter()
const visible = ref(controlled.value ? props.modelValue : true)
const activeTab = ref('profile')
const profileFormRef = ref()
const profileForm = reactive({
  id: null,
  username: '',
  name: '',
  email: '',
  phone: ''
})
const saving = ref(false)

const userInfo = ref({})
const userStats = ref({})

const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const resetLoading = ref(false)

const userAvatar = computed(() => userInfo.value.avatar)

const profileRules = {
  name: [
    { required: true, message: '请输入姓名', type: 'error' }
  ],
  email: [
    { required: true, message: '请输入邮箱', type: 'error' },
    { email: true, message: '请输入正确的邮箱格式', type: 'error' }
  ],
  phone: [
    { required: true, message: '请输入手机号', type: 'error' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', type: 'error' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', type: 'error' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', type: 'error' },
    { min: 6, message: '新密码长度不能少于6位', type: 'error' },
    {
      validator: (val) => {
        if (val === passwordForm.oldPassword) {
          return { result: false, message: '新密码不能与原密码相同', type: 'error' }
        }
        return { result: true }
      }
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', type: 'error' },
    {
      validator: (val) => {
        if (val !== passwordForm.newPassword) {
          return { result: false, message: '两次输入的密码不一致', type: 'error' }
        }
        return { result: true }
      }
    }
  ]
}

const getRoleLabel = (role) => ROLE_LABELS[role] || role
const getRoleType = (role) => {
  const typeMap = {
    ADMIN: 'danger',
    TEACHER: 'warning',
    STUDENT: 'success'
  }
  return typeMap[role] || 'info'
}

const getRoleThemeColor = (role) => {
  const colorMap = {
    ADMIN: '#409eff',    // 蓝色 - 学工处
    TEACHER: '#67c23a',  // 绿色 - 教师
    STUDENT: '#1f6bff'   // 深蓝色 - 学生
  }
  return colorMap[role] || '#0052d9'
}

const syncForm = () => {
  profileForm.id = userInfo.value.id
  profileForm.username = userInfo.value.username
  profileForm.name = userInfo.value.name
  profileForm.email = userInfo.value.email
  profileForm.phone = userInfo.value.phone
}

const fetchProfile = async () => {
  const response = await userAPI.getUserById(authStore.user.id)
  if (response.code === 200) {
    userInfo.value = response.data
    syncForm()
  }

  if (response.code === 200 && response.data.role === 'STUDENT') {
    const statsResponse = await serviceRecordAPI.getMyStats()
    if (statsResponse.code === 200) {
      userStats.value = statsResponse.data
    }
  } else {
    userStats.value = {}
  }

  resetPasswordForm()
}

const handleSave = async () => {
  try {
    const result = await profileFormRef.value.validate()
    if (result !== true) return

    saving.value = true
    await userAPI.updateUser({ ...profileForm })
    await fetchProfile()
    ElMessage.success('个人信息更新成功')
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error(error.response?.data?.message || '更新失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  syncForm()
}

const handleClose = () => {
  visible.value = false
  resetPasswordForm()
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.reset()
}

const handlePasswordReset = async () => {
  try {
    const result = await passwordFormRef.value.validate()
    if (result !== true) return

    resetLoading.value = true
    await authAPI.resetPassword({
      userId: authStore.user.id,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码重置成功，请重新登录')
    resetPasswordForm()
    visible.value = false
    await authStore.logout()
    router.push('/auth/login')
  } catch (error) {
    console.error('重置密码失败:', error)
    ElMessage.error(error.response?.data?.message || '重置密码失败，请稍后重试')
  } finally {
    resetLoading.value = false
  }
}

watch(
  () => props.modelValue,
  (val) => {
    if (controlled.value) {
      visible.value = val
      if (val) {
        fetchProfile()
      }
    }
  }
)

watch(
  () => visible.value,
  (val) => {
    if (controlled.value) {
      emit('update:modelValue', val)
    }
  }
)

import { onMounted } from 'vue'

onMounted(() => {
  if (!controlled.value && visible.value) {
    fetchProfile()
  }
})
</script>

<style scoped lang="scss">
.profile-dialog {
  :deep(.el-dialog__header) {
    margin: 0;
    padding: 0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__footer) {
    display: none;
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  padding: 24px;
  background: #ffffff;
  border-bottom: none;
  align-items: center;

  .header-content {
    display: flex;
    gap: 16px;
    align-items: center;

    .user-avatar {
      flex-shrink: 0;
    }

    .user-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
      text-align: left !important;
      padding-top: 0;

      .user-name {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        display: flex;
        align-items: center;
        line-height: 1.4;
      }

      .user-username {
        font-size: 14px;
        color: #909399;  
        line-height: 1.4;
      }
    }
  }

  .close-btn {
    flex-shrink: 0; 
    width: 32px;
    height: 32px;
    border-radius: 50%;
    padding: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
    transition: background-color 0.3s, color 0.3s;

    &:hover {
      background-color: #f0f2f5;
      color: #606266;
    }
  }
}

.dialog-body {
  background: #ffffff;
  padding: 0;
}

.profile-tabs {
  :deep(.t-tabs__header) {
    padding: 0 24px;
    background: #ffffff;
  }

  :deep(.t-tabs__nav) {
    border-bottom: 1px solid #e5e7eb;
  }

  :deep(.t-tabs__nav-item) {
    font-size: 15px;
    font-weight: 500;
    color: #606266;
    padding: 0 20px;
    height: 48px;
    line-height: 48px;

    &:hover {
      color: var(--role-color, #0052d9);
    }

    &.t-is-active {
      color: var(--role-color, #0052d9);
    }
  }

  :deep(.t-tabs__nav-item-wrapper.t-is-active::after),
  :deep(.t-tabs__bar) {
    background-color: var(--role-color, #0052d9);
  }

  :deep(.t-tabs__content) {
    padding: 32px 24px;
  }
}

.profile-form {
  max-width: 100%;
  padding: 0;

  :deep(.t-form__item) {
    margin-bottom: 24px;
  }

  :deep(.t-form__label) {
    color: #606266;
    font-weight: 500;
    font-size: 14px;
    text-align: left !important;
  }

  :deep(.t-input) {
    width: 100%;
  }

  :deep(.t-input__inner) {
    height: 40px;
    font-size: 14px;
  }

  // 按钮样式优化
  :deep(.t-button) {
    height: 40px;
    padding: 0 24px;
    font-size: 14px;
    border-radius: 4px;

    &.t-button--theme-primary {
      &:hover {
        opacity: 0.85;
      }
    }

    &.t-button--variant-outline {
      color: #606266;
      border-color: #dcdfe6;

      &:hover {
        color: var(--role-color, #0052d9);
        border-color: var(--role-color, #0052d9);
        background-color: rgba(0, 82, 217, 0.04);
      }
    }
  }
}

.password-form {
  max-width: 100%;
  padding: 0;

  :deep(.t-form__item) {
    margin-bottom: 24px;
  }

  :deep(.t-form__label) {
    color: #606266;
    font-weight: 500;
    font-size: 14px;
    text-align: left !important;
  }

  :deep(.t-input) {
    width: 100%;
  }

  :deep(.t-input__inner) {
    height: 40px;
    font-size: 14px;
  }

  // 按钮样式优化
  :deep(.t-button) {
    height: 40px;
    padding: 0 24px;
    font-size: 14px;
    border-radius: 4px;

    &.t-button--theme-primary {
      &:hover {
        opacity: 0.85;
      }
    }

    &.t-button--variant-outline {
      color: #606266;
      border-color: #dcdfe6;

      &:hover {
        color: var(--role-color, #0052d9);
        border-color: var(--role-color, #0052d9);
        background-color: rgba(0, 82, 217, 0.04);
      }
    }
  }
}

@media (max-width: 768px) {
  .profile-tabs {
    :deep(.t-tabs__content) {
      padding: 16px;
    }
  }
}
</style>