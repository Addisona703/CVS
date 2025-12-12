<template>
  <view class="profile-page">
    <!-- 头像区域 -->
    <view class="avatar-section">
      <view class="avatar-wrapper" @click="handleUploadAvatar">
        <view class="avatar">
          {{ userInitial }}
        </view>
      </view>
      <text class="username">{{ userInfo.username || 'admin' }}</text>
      <text class="role-badge">学工处</text>
    </view>

    <!-- 基本信息分组 -->
    <view class="list-group">
      <view class="group-title">基本信息</view>
      <view class="group-content">
        <view class="list-item" @click="handleEditField('name')">
          <text class="item-label">真实姓名</text>
          <view class="item-value-wrapper">
            <text class="item-value">{{ userInfo.name || '未设置' }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>

        <view class="list-item" @click="handleEditField('email')">
          <text class="item-label">邮箱</text>
          <view class="item-value-wrapper">
            <text class="item-value">{{ userInfo.email || '未设置' }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>

        <view class="list-item last" @click="handleEditField('phone')">
          <text class="item-label">手机号</text>
          <view class="item-value-wrapper">
            <text class="item-value">{{ userInfo.phone || '未设置' }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 账户安全分组 -->
    <view class="list-group">
      <view class="group-title">账户安全</view>
      <view class="group-content">
        <view class="list-item last" @click="handleChangePassword">
          <text class="item-label">修改密码</text>
          <view class="item-value-wrapper">
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 退出登录分组 -->
    <view class="list-group">
      <view class="group-content">
        <view class="list-item logout-item last" @click="handleLogout">
          <text class="logout-text">退出登录</text>
        </view>
      </view>
    </view>

    <!-- 退出登录确认对话框 -->
    <view v-if="showLogoutDialog" class="dialog-overlay" @click="showLogoutDialog = false">
      <view class="dialog-content" @click.stop>
        <view class="dialog-header">
          <text class="dialog-title">退出登录</text>
        </view>
        <view class="dialog-body">
          <text class="dialog-text">确定要退出登录吗？</text>
        </view>
        <view class="dialog-footer">
          <button class="dialog-btn cancel-btn" @click="showLogoutDialog = false">取消</button>
          <button class="dialog-btn confirm-btn" @click="confirmLogout">确定</button>
        </view>
      </view>
    </view>

    <!-- Custom Tab Bar -->
    <custom-tabbar :current="3" role="admin" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { getUserInfo, updateUserInfo, uploadAvatar, changePassword } from '@/api/user'
import { validateEmail, validatePhone } from '@/utils/validate'
import CustomTabbar from '@/components/common/custom-tabbar/custom-tabbar.vue'

const authStore = useAuthStore()

const userInfo = ref({
  username: '',
  name: '',
  email: '',
  phone: '',
  avatar: '',
  role: 'ADMIN'
})

const showLogoutDialog = ref(false)

const userInitial = computed(() => {
  if (!userInfo.value) return '管'
  const name = userInfo.value.name || userInfo.value.username || '管'
  return name.charAt(0).toUpperCase()
})

// 下拉刷新
onPullDownRefresh(async () => {
  try {
    await loadUserInfo()
  } finally {
    uni.stopPullDownRefresh()
  }
})

onMounted(() => {
  loadUserInfo()
})

const loadUserInfo = async () => {
  try {
    const data = await getUserInfo()
    userInfo.value = data
  } catch (error) {
    console.error('加载用户信息失败:', error)
    userInfo.value = { ...authStore.userInfo }
  }
}

const handleUploadAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]

      uni.showLoading({ title: '上传中...' })

      try {
        const result = await uploadAvatar({
          filePath: tempFilePath,
          name: 'avatar'
        })

        userInfo.value.avatar = result.url
        authStore.updateUserInfo({ avatar: result.url })

        uni.showToast({ title: '头像更新成功', icon: 'success' })
      } catch (error) {
        console.error('上传头像失败:', error)
        uni.showToast({ title: '上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

const handleEditField = (field) => {
  const fieldConfig = {
    name: { title: '真实姓名', placeholder: '请输入真实姓名' },
    email: { title: '邮箱', placeholder: '请输入邮箱' },
    phone: { title: '手机号', placeholder: '请输入手机号' }
  }

  const config = fieldConfig[field]

  uni.showModal({
    title: `修改${config.title}`,
    editable: true,
    placeholderText: config.placeholder,
    content: userInfo.value[field] || '',
    success: async (res) => {
      if (res.confirm && res.content) {
        if (field === 'email' && !validateEmail(res.content)) {
          uni.showToast({ title: '邮箱格式不正确', icon: 'none' })
          return
        }

        if (field === 'phone' && !validatePhone(res.content)) {
          uni.showToast({ title: '手机号格式不正确', icon: 'none' })
          return
        }

        try {
          await updateUserInfo({
            [field]: res.content
          })

          userInfo.value[field] = res.content
          authStore.updateUserInfo({ [field]: res.content })

          uni.showToast({ title: '修改成功', icon: 'success' })
        } catch (error) {
          console.error('修改失败:', error)
          uni.showToast({ title: '修改失败', icon: 'none' })
        }
      }
    }
  })
}

const handleChangePassword = () => {
  uni.showModal({
    title: '修改密码',
    content: '请输入旧密码',
    editable: true,
    placeholderText: '请输入旧密码',
    success: (res1) => {
      if (res1.confirm && res1.content) {
        const oldPassword = res1.content

        uni.showModal({
          title: '修改密码',
          content: '请输入新密码',
          editable: true,
          placeholderText: '请输入新密码（6-20位）',
          success: async (res2) => {
            if (res2.confirm && res2.content) {
              const newPassword = res2.content

              if (newPassword.length < 6 || newPassword.length > 20) {
                uni.showToast({ title: '密码长度应为6-20位', icon: 'none' })
                return
              }

              try {
                await changePassword({
                  oldPassword,
                  newPassword
                })

                uni.showToast({ title: '密码修改成功，请重新登录', icon: 'success' })

                setTimeout(() => {
                  authStore.logout()
                }, 1500)
              } catch (error) {
                console.error('修改密码失败:', error)
                uni.showToast({ title: '修改密码失败', icon: 'none' })
              }
            }
          }
        })
      }
    }
  })
}

const handleLogout = () => {
  showLogoutDialog.value = true
}

const confirmLogout = () => {
  showLogoutDialog.value = false
  authStore.logout()
}
</script>

<style scoped>
/* iOS风格 - 浅灰色背景 */
.profile-page {
  min-height: 100vh;
  background: #F2F2F7;
  padding-bottom: 120rpx;
}

/* 头像区域 - 顶部居中 */
.avatar-section {
  padding: 60rpx 0 40rpx;
  text-align: center;
  background: #F2F2F7;
}

.avatar-wrapper {
  display: inline-block;
  margin-bottom: 20rpx;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FA8C16 0%, #FF6B35 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56rpx;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 8rpx 24rpx rgba(250, 140, 22, 0.3);
}

.username {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #000;
  margin-bottom: 12rpx;
}

.role-badge {
  display: inline-block;
  padding: 6rpx 20rpx;
  background: linear-gradient(135deg, #FA8C16 0%, #FF6B35 100%);
  border-radius: 20rpx;
  font-size: 22rpx;
  color: #fff;
  font-weight: 500;
  box-shadow: 0 4rpx 12rpx rgba(250, 140, 22, 0.2);
}

/* iOS风格分组列表 */
.list-group {
  margin-bottom: 40rpx;
}

.group-title {
  padding: 16rpx 32rpx 12rpx;
  font-size: 26rpx;
  color: #8E8E93;
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.group-content {
  background: #FFFFFF;
  margin: 0 16rpx;
  border-radius: 20rpx;
  overflow: hidden;
}

/* 列表项 */
.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #E5E5EA;
  transition: background 0.2s;
  min-height: 88rpx;
}

.list-item.last {
  border-bottom: none;
}

.list-item:active {
  background: #F2F2F7;
}

.item-label {
  font-size: 32rpx;
  color: #000;
}

.item-value-wrapper {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.item-value {
  font-size: 32rpx;
  color: #8E8E93;
  max-width: 400rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-arrow {
  font-size: 40rpx;
  color: #C7C7CC;
  font-weight: 300;
}

/* 退出登录项 */
.logout-item {
  justify-content: center;
}

.logout-item:active {
  background: #FFE5E5;
}

.logout-text {
  font-size: 32rpx;
  color: #FF3B30;
  font-weight: 500;
}

/* 退出登录对话框 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.dialog-content {
  width: 560rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
}

.dialog-header {
  padding: 40rpx 32rpx 24rpx;
  text-align: center;
}

.dialog-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #000;
}

.dialog-body {
  padding: 0 32rpx 40rpx;
  text-align: center;
}

.dialog-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.dialog-footer {
  display: flex;
  border-top: 1rpx solid #E5E5EA;
}

.dialog-btn {
  flex: 1;
  height: 96rpx;
  line-height: 96rpx;
  text-align: center;
  font-size: 32rpx;
  border: none;
  background: transparent;
}

.cancel-btn {
  color: #666;
  border-right: 1rpx solid #E5E5EA;
}

.confirm-btn {
  color: #FF3B30;
  font-weight: 600;
}

.dialog-btn:active {
  background: #F2F2F7;
}
</style>
