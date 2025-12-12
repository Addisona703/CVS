<template>
  <view class="activity-detail-page">
    <scroll-view 
      class="detail-content"
      scroll-y
      enable-back-to-top
      scroll-with-animation
      :lower-threshold="100"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="refresh"
    >
      <view v-if="loading" class="loading-state">
        <text>加载中...</text>
      </view>
      
      <view v-else-if="activity" class="activity-info">
        <!-- 活动头部 -->
        <view class="activity-header">
          <text class="activity-title">{{ activity.title }}</text>
          <!-- <view :class="['status-badge', `status-${activity.status}`]">
            {{ statusText }}
          </view> -->
        </view>
        
        <!-- 活动基本信息 -->
        <view class="info-section">
          <view class="info-item">
            <ph-icon name="calendar" :size="44" color="#0052d9" />
            <view class="info-content">
              <text class="info-label">活动时间</text>
              <text class="info-value">
                {{ formatDateTime(activity.startTime) }} 至
                {{ formatDateTime(activity.endTime) }}
              </text>
            </view>
          </view>
          
          <view class="info-item">
            <ph-icon name="map-pin" :size="44" color="#FF8C42" />
            <view class="info-content">
              <text class="info-label">活动地点</text>
              <text class="info-value">{{ activity.location }}</text>
            </view>
          </view>
          
          <view class="info-item">
            <ph-icon name="users" :size="44" color="#7BC96F" />
            <view class="info-content">
              <text class="info-label">报名人数</text>
              <text class="info-value">
                {{ activity.currentParticipants }} / {{ activity.maxParticipants }}
              </text>
            </view>
          </view>
          
          <view class="info-item">
            <ph-icon name="clock" :size="44" color="#9B59B6" />
            <view class="info-content">
              <text class="info-label">报名截止</text>
              <text class="info-value">
                {{ formatDateTime(activity.registrationDeadline) }}
              </text>
            </view>
          </view>
          
          <view class="info-item">
            <ph-icon name="star" :size="44" color="#FFB800" weight="fill" />
            <view class="info-content">
              <text class="info-label">积分奖励</text>
              <text class="info-value points">{{ activity.points }} 积分</text>
            </view>
          </view>
        </view>
        
        <!-- 签到签退信息（活动进行中或已结束） -->
        <view v-if="isStudent && signupInfo && signupInfo.status === 'APPROVED' && (activityStatus === 'ongoing' || activityStatus === 'ended')" class="checkin-info-section">
          <view class="checkin-status-card">
            <view class="status-row">
              <view class="status-item" :class="{ 'completed': signupInfo.signInTime }">
                <ph-icon :name="signupInfo.signInTime ? 'check-circle' : 'clock'" :size="56" :color="signupInfo.signInTime ? '#2ba471' : '#ccc'" :weight="signupInfo.signInTime ? 'fill' : 'regular'" />
                <view class="status-content">
                  <text class="status-label">签到时间</text>
                  <text class="status-value">
                    {{ signupInfo.signInTime ? formatDateTime(signupInfo.signInTime) : '未签到' }}
                  </text>
                </view>
              </view>
              
              <view class="status-divider"></view>
              
              <view class="status-item" :class="{ 'completed': signupInfo.signOutTime }">
                <ph-icon :name="signupInfo.signOutTime ? 'check-circle' : 'clock'" :size="56" :color="signupInfo.signOutTime ? '#2ba471' : '#ccc'" :weight="signupInfo.signOutTime ? 'fill' : 'regular'" />
                <view class="status-content">
                  <text class="status-label">签退时间</text>
                  <text class="status-value">
                    {{ signupInfo.signOutTime ? formatDateTime(signupInfo.signOutTime) : '未签退' }}
                  </text>
                </view>
              </view>
            </view>
            
            <!-- 服务时长 -->
            <view v-if="signupInfo.signInTime && signupInfo.signOutTime" class="service-duration">
              <ph-icon name="timer" :size="40" color="#0052d9" />
              <text class="duration-text">服务时长：{{ calculateDuration(signupInfo.signInTime, signupInfo.signOutTime) }}</text>
            </view>
            
            <!-- 签到签退按钮（仅活动进行中显示） -->
            <button 
              v-if="activityStatus === 'ongoing' && (!signupInfo.signInTime || !signupInfo.signOutTime)"
              class="checkin-action-btn"
              @click="handleCheckIn"
            >
              <ph-icon name="qr-code" :size="44" color="#fff" />
              <text class="btn-text">{{ signupInfo.signInTime ? '扫码签退' : '扫码签到' }}</text>
            </button>
          </view>
        </view>
        
        <!-- 活动倒计时（活动未开始时显示） -->
        <view class="countdown-section" v-if="activityStatus === 'upcoming'">
          <view class="countdown-title">距离活动开始</view>
          <view class="countdown-time">
            <view class="time-unit" v-if="countdown.days > 0">
              <text class="time-number">{{ countdown.days }}</text>
              <text class="time-label">天</text>
            </view>
            <view class="time-unit">
              <text class="time-number">{{ countdown.hours }}</text>
              <text class="time-label">时</text>
            </view>
            <view class="time-unit">
              <text class="time-number">{{ countdown.minutes }}</text>
              <text class="time-label">分</text>
            </view>
            <view class="time-unit">
              <text class="time-number">{{ countdown.seconds }}</text>
              <text class="time-label">秒</text>
            </view>
          </view>
        </view>
        
        <!-- 活动描述 -->
        <view class="section">
          <text class="section-title">活动描述</text>
          <text class="section-content">{{ activity.description }}</text>
        </view>
        
        <!-- 参与要求 -->
        <view class="section" v-if="activity.requirements">
          <text class="section-title">参与要求</text>
          <text class="section-content">{{ activity.requirements }}</text>
        </view>
        
        <!-- 联系方式 -->
        <view class="section" v-if="activity.contactInfo">
          <text class="section-title">联系方式</text>
          <text class="section-content">{{ activity.contactInfo }}</text>
        </view>
        
        <!-- 组织者信息 -->
        <view class="section">
          <text class="section-title">组织者</text>
          <text class="section-content">{{ activity.organizerName }}</text>
        </view>
      </view>
    </scroll-view>
    
    <!-- 报名弹窗 -->
    <view v-if="showSignupDialog" class="signup-dialog-mask" @click="showSignupDialog = false">
      <view class="signup-dialog" @click.stop>
        <view class="dialog-header">
          <text class="dialog-title">报名活动</text>
          <text class="dialog-close" @click="showSignupDialog = false">✕</text>
        </view>
        
        <view class="dialog-body">
          <view class="form-label">报名理由（选填）</view>
          <textarea
            v-model="signupReason"
            class="reason-input"
            placeholder="请输入报名理由，让组织者更了解你..."
            placeholder-style="color: #999;"
            maxlength="200"
            :auto-height="true"
          />
          <view class="char-count">{{ signupReason.length }}/200</view>
        </view>
        
        <view class="dialog-footer">
          <button class="dialog-btn cancel-btn" @click="showSignupDialog = false">取消</button>
          <button class="dialog-btn confirm-btn" @click="confirmSignup">确认报名</button>
        </view>
      </view>
    </view>
    
    <!-- 底部操作栏 -->
    <view class="action-bar" v-if="activity">
      <!-- 学生：报名/取消报名 -->
      <template v-if="isStudent">
        <button 
          v-if="!signupInfo"
          class="action-btn primary"
          :disabled="!canSignup"
          @click="handleSignup"
        >
          {{ signupBtnText }}
        </button>
        
        <button 
          v-else-if="signupInfo.status === 'PENDING'"
          class="action-btn"
          @click="handleCancelSignup"
        >
          取消报名
        </button>
        
        <view v-else class="signup-status">
          <text :class="['status-text', `status-${signupInfo.status}`]">
            {{ signupStatusText }}
          </text>
        </view>
      </template>
      
      <!-- 教师：编辑活动（仅草稿状态） -->
      <template v-else-if="canEdit && authStore.isTeacher">
        <button class="action-btn" @click="handleEdit">
          编辑活动
        </button>
        <button class="action-btn danger" @click="handleDelete">
          删除活动
        </button>
      </template>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getActivityById, deleteActivity } from '@/api/activity'
import { createSignup, cancelSignup, getMySignups } from '@/api/signup'
import { formatDateTime } from '@/utils/format'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'

const authStore = useAuthStore()

const activity = ref(null)
const signupInfo = ref(null)
const loading = ref(false)
const refreshing = ref(false)
const activityId = ref(null)
const countdown = ref({ days: 0, hours: 0, minutes: 0, seconds: 0 })
const countdownTimer = ref(null)
const showSignupDialog = ref(false)
const signupReason = ref('')

const isStudent = computed(() => authStore.isStudent)
const canEdit = computed(() => {
  if (!activity.value) return false
  
  // 学工处可以编辑任何状态的活动
  if (authStore.isAdmin) return true
  
  // 教师只能编辑自己创建的草稿状态的活动
  if (authStore.isTeacher && activity.value.organizerId === authStore.userId) {
    return activity.value.status === 'DRAFT'
  }
  
  return false
})

const statusText = computed(() => {
  const statusMap = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'ONGOING': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[activity.value?.status] || '未知'
})

// 计算活动状态（用于倒计时，仅已发布/进行中显示倒计时）
const activityStatus = computed(() => {
  if (!activity.value) return null

  // 仅对已发布或进行中计算倒计时，其余状态不显示
  if (activity.value.status !== 'PUBLISHED' && activity.value.status !== 'ONGOING') {
    return null
  }

  if (activity.value.status === 'ONGOING') {
    return 'ongoing'
  }
  if (activity.value.status === 'COMPLETED') {
    return 'ended'
  }

  // 后端状态 PUBLISHED，根据时间判断
  const now = new Date()
  const startTime = new Date(activity.value.startTime)
  const endTime = new Date(activity.value.endTime)

  if (now < startTime) {
    return 'upcoming'
  } else if (now >= startTime && now <= endTime) {
    return 'ongoing'
  } else {
    return 'ended'
  }
})

// 是否显示倒计时
const showCountdown = computed(() => {
  return activityStatus.value === 'upcoming' || activityStatus.value === 'ongoing'
})

// 倒计时标题
const countdownTitle = computed(() => {
  if (activityStatus.value === 'upcoming') {
    return '距离活动开始'
  } else if (activityStatus.value === 'ongoing') {
    return '距离活动结束'
  }
  return ''
})

const canSignup = computed(() => {
  if (!activity.value) return false
  
  const now = new Date()
  const deadline = new Date(activity.value.registrationDeadline)
  
  return activity.value.status === 'PUBLISHED' &&
    now < deadline &&
    activity.value.currentParticipants < activity.value.maxParticipants
})

const signupBtnText = computed(() => {
  if (!activity.value) return '报名'
  
  // 根据活动状态显示不同文字
  const statusTextMap = {
    'DRAFT': '活动未发布',
    'COMPLETED': '活动已结束',
    'CANCELLED': '活动已取消',
    'ONGOING': '活动进行中'
  }
  
  if (statusTextMap[activity.value.status]) {
    return statusTextMap[activity.value.status]
  }
  
  if (activity.value.status !== 'PUBLISHED') {
    return '活动未开放报名'
  }
  
  const now = new Date()
  const deadline = new Date(activity.value.registrationDeadline)
  
  if (now >= deadline) {
    return '报名已截止'
  }
  
  if (activity.value.currentParticipants >= activity.value.maxParticipants) {
    return '报名已满'
  }
  
  return '立即报名'
})

const signupStatusText = computed(() => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return statusMap[signupInfo.value?.status] || ''
})

// 更新倒计时
const updateCountdown = () => {
  if (!activity.value) return
  
  const now = new Date()
  let targetTime
  
  if (activityStatus.value === 'upcoming') {
    targetTime = new Date(activity.value.startTime)
  } else if (activityStatus.value === 'ongoing') {
    targetTime = new Date(activity.value.endTime)
  } else {
    return
  }
  
  const diff = targetTime - now
  
  if (diff <= 0) {
    countdown.value = { days: 0, hours: 0, minutes: 0, seconds: 0 }
    if (countdownTimer.value) {
      clearInterval(countdownTimer.value)
      countdownTimer.value = null
    }
    return
  }
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  
  countdown.value = { days, hours, minutes, seconds }
}

// 启动倒计时
const startCountdown = () => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
  
  updateCountdown()
  countdownTimer.value = setInterval(updateCountdown, 1000)
}

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || currentPage.$page.options
  
  if (options.id) {
    activityId.value = parseInt(options.id)
    loadActivityDetail()
    if (isStudent.value) {
      loadSignupInfo()
    }
  }
})

// 加载活动详情
const loadActivityDetail = async () => {
  loading.value = true
  
  try {
    activity.value = await getActivityById(activityId.value)
    
    console.log('活动数据:', activity.value)
    console.log('开始时间:', activity.value.startTime)
    console.log('结束时间:', activity.value.endTime)
    
    // 启动倒计时
    startCountdown()
  } catch (error) {
    console.error('加载活动详情失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 加载报名信息
const loadSignupInfo = async () => {
  try {
    const data = await getMySignups({
      pageNum: 1,
      pageSize: 1,
      params: {
        activityId: activityId.value
      }
    })
    
    if (data.records && data.records.length > 0) {
      signupInfo.value = data.records[0]
    }
  } catch (error) {
    console.error('加载报名信息失败:', error)
  }
}

// 下拉刷新
const refresh = () => {
  refreshing.value = true
  loadActivityDetail()
  if (isStudent.value) {
    loadSignupInfo()
  }
}

// 报名
const handleSignup = () => {
  signupReason.value = ''
  showSignupDialog.value = true
}

// 确认报名
const confirmSignup = async () => {
  try {
    const signup = await createSignup({
      activityId: activityId.value,
      reason: signupReason.value || ''
    })
    
    signupInfo.value = signup
    activity.value.currentParticipants++
    showSignupDialog.value = false
    
    uni.showToast({ title: '报名成功', icon: 'success' })
  } catch (error) {
    console.error('报名失败:', error)
    uni.showToast({ 
      title: error.message || '报名失败', 
      icon: 'none' 
    })
  }
}

// 取消报名
const handleCancelSignup = () => {
  uni.showModal({
    title: '提示',
    content: '确定要取消报名吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelSignup(signupInfo.value.id)
          
          signupInfo.value = null
          activity.value.currentParticipants--
          
          uni.showToast({ title: '已取消报名', icon: 'success' })
        } catch (error) {
          console.error('取消报名失败:', error)
        }
      }
    }
  })
}

// 编辑活动
const handleEdit = () => {
  uni.navigateTo({
    url: `/pages/teacher/create-activity/create-activity?id=${activityId.value}`
  })
}

// 删除活动
const handleDelete = () => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这个活动吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteActivity(activityId.value)
          
          uni.showToast({ title: '删除成功', icon: 'success' })
          
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          console.error('删除活动失败:', error)
        }
      }
    }
  })
}

// 计算服务时长
const calculateDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return '0小时0分钟'
  
  const start = new Date(startTime)
  const end = new Date(endTime)
  const diffMs = end - start
  
  const hours = Math.floor(diffMs / (1000 * 60 * 60))
  const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  return `${hours}小时${minutes}分钟`
}

// 签到签退
const handleCheckIn = () => {
  uni.navigateTo({
    url: '/pages/student-sub/check-in/check-in'
  })
}

onUnmounted(() => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
})
</script>

<style scoped>
.activity-detail-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  overflow: hidden;
}

.detail-content {
  flex: 1;
  height: 0;
  -webkit-overflow-scrolling: touch;
}

.loading-state {
  padding: 200rpx 32rpx;
  text-align: center;
  font-size: 28rpx;
  color: #999;
}

.activity-info {
  padding-bottom: 120rpx;
}

.activity-header {
  background: #fff;
  padding: 32rpx;
  margin-bottom: 16rpx;
  transform: translateZ(0);
  will-change: transform;
}

.activity-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #333;
  margin-bottom: 16rpx;
}

.status-badge {
  display: inline-block;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.status-badge.status-DRAFT {
  background: #f0f0f0;
  color: #666;
}

.status-badge.status-PUBLISHED {
  background: #e8f4fd;
  color: #0052d9;
}

.status-badge.status-ONGOING {
  background: #e8f8f2;
  color: #2ba471;
}

.status-badge.status-COMPLETED {
  background: #f0f0f0;
  color: #999;
}

.status-badge.status-CANCELLED {
  background: #fdebec;
  color: #e34d59;
}

.info-section {
  background: #fff;
  padding: 16rpx 32rpx;
  margin-bottom: 16rpx;
  transform: translateZ(0);
  will-change: transform;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}



.info-content {
  flex: 1;
}

.info-label {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.info-value {
  display: block;
  font-size: 28rpx;
  color: #333;
}

.info-value.points {
  color: #ed7b2f;
  font-weight: 700;
}

.section {
  background: #fff;
  padding: 32rpx;
  margin-bottom: 16rpx;
  transform: translateZ(0);
  will-change: transform;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #333;
  margin-bottom: 16rpx;
}

.section-content {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.action-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  background: #fff;
  color: #333;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.action-btn.primary {
  background: #0052d9;
  color: #fff;
  border-color: #0052d9;
}

.action-btn.danger {
  color: #e34d59;
  border-color: #e34d59;
}

.action-btn[disabled] {
  opacity: 0.6;
}

.signup-status {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-text {
  font-size: 28rpx;
  font-weight: 700;
}

.status-text.status-PENDING {
  color: #ed7b2f;
}

.status-text.status-APPROVED {
  color: #2ba471;
}

.status-text.status-REJECTED {
  color: #e34d59;
}

/* 倒计时样式 */
.countdown-section {
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
  border-radius: 20rpx;
  padding: 32rpx;
  margin: 16rpx;
  text-align: center;
  transform: translateZ(0);
  will-change: transform;
}

.countdown-title {
  color: #fff;
  font-size: 28rpx;
  margin-bottom: 20rpx;
  opacity: 0.9;
}

.countdown-time {
  display: flex;
  justify-content: center;
  gap: 24rpx;
}

.time-unit {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80rpx;
}

.time-number {
  font-size: 48rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1;
  margin-bottom: 8rpx;
}

.time-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 签到签退信息卡片 */
.checkin-info-section {
  margin: 16rpx;
}

.checkin-status-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  transform: translateZ(0);
  will-change: transform;
}

.checkin-title {
  text-align: center;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 24rpx;
  font-weight: 600;
}

.status-row {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.status-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.status-item.completed .status-icon {
  filter: none;
}



.status-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.status-label {
  font-size: 24rpx;
  color: #999;
}

.status-value {
  font-size: 26rpx;
  color: #333;
  font-weight: 600;
}

.status-item.completed .status-value {
  color: #2ba471;
}

.status-divider {
  width: 1rpx;
  height: 60rpx;
  background: #e0e0e0;
  margin: 0 24rpx;
}

.service-duration {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 12rpx;
  margin-bottom: 24rpx;
}



.duration-text {
  font-size: 28rpx;
  color: #0052d9;
  font-weight: 600;
}

.checkin-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
  border-radius: 12rpx;
  border: none;
  transition: all 0.3s ease;
}

.checkin-action-btn::after {
  border: none;
}

.checkin-action-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}



.btn-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: 600;
}

/* 报名弹窗样式 */
.signup-dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
}

.signup-dialog {
  width: 100%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.dialog-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #333;
}

.dialog-close {
  font-size: 40rpx;
  color: #999;
  line-height: 1;
}

.dialog-body {
  padding: 32rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
  font-weight: 600;
}

.reason-input {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  box-sizing: border-box;
}

.char-count {
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 12rpx;
}

.dialog-footer {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx 32rpx;
}

.dialog-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
}

.dialog-btn::after {
  border: none;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.confirm-btn {
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
  color: #fff;
}
</style>
