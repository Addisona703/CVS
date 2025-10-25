<template>
  <div class="student-checkin-section">
    <div class="checkin-content">
      <!-- 签到区域 -->
      <div class="signin-section checkin-section" data-highlight="signin">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>活动签到</span>
              <el-tag v-if="signInStatus" :type="signInStatus === 'success' ? 'success' : 'info'">
                {{ signInStatus === 'success' ? '已签到' : '未签到' }}
              </el-tag>
            </div>
          </template>
          <div class="checkin-info">
            <!-- 已签到：只显示签到时间 -->
            <div v-if="signupInfo && signupInfo.signInTime" class="time-info">
              <el-icon>
                <Clock />
              </el-icon>
              <span>签到时间：{{ formatDateTime(signupInfo.signInTime) }}</span>
            </div>

            <!-- 未签到且活动未结束：显示提示文字和按钮 -->
            <template v-else-if="!isActivityEnded">
              <div v-if="showCountdown" class="countdown-info">
                <el-icon>
                  <Clock />
                </el-icon>
                <span>{{ countdownText }}</span>
              </div>
              <p v-if="isSignedUp && !isSignupApproved">报名审核通过后才可签到</p>
              <p v-else-if="!isSignedUp">报名后方可签到</p>
              <p v-else-if="!isActivityStarted">活动尚未开始，签到将在开始后开放</p>
              <p v-else>请在活动开始后输入教师口令完成签到</p>
              <el-button v-if="canSignIn" type="primary" @click="$emit('sign-in')">
                立即签到
              </el-button>
              <el-button v-else disabled>
                {{ getSignInButtonText() }}
              </el-button>
            </template>

            <!-- 未签到且活动已结束：显示未签到提示 -->
            <div v-else class="no-time-info">
              <span>未签到</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 签退区域 -->
      <div class="signout-section checkout-section" data-highlight="signout">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>活动签退</span>
              <el-tag v-if="signOutStatus" :type="getSignOutTagType()">
                {{ getSignOutStatusText() }}
              </el-tag>
            </div>
          </template>
          <div class="checkout-info">
            <!-- 已签退：只显示签退时间 -->
            <div v-if="signupInfo && signupInfo.signOutTime" class="time-info">
              <el-icon>
                <Clock />
              </el-icon>
              <span>签退时间：{{ formatDateTime(signupInfo.signOutTime) }}</span>
            </div>

            <!-- 未签退且活动未结束：显示提示文字、状态和按钮 -->
            <template v-else-if="!isActivityEnded">
              <div v-if="showCountdown" class="countdown-info">
                <el-icon>
                  <Clock />
                </el-icon>
                <span>{{ countdownText }}</span>
              </div>
              <p v-if="!isActivityStarted">活动尚未开始，签退将在开始后开放</p>
              <p v-else>活动结束后请输入教师口令完成签退</p>

              <!-- 签退被拒绝状态 -->
              <div v-if="signOutStatus === 'rejected'" class="checkout-rejected"
                data-highlight="checkout-rejected">
                <el-alert title="签退被拒绝" type="error" :description="signOutReason || '签退申请未通过审核'" show-icon
                  :closable="false" />
              </div>

              <!-- 签退按钮 -->
              <el-button v-if="canSignOut" type="warning" @click="$emit('sign-out')">
                申请签退
              </el-button>
              <el-button v-else disabled>
                {{ getSignOutButtonText() }}
              </el-button>
            </template>

            <!-- 未签退且活动已结束：显示未签退提示 -->
            <div v-else class="no-time-info">
              <span>未签退</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { Clock } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/format'

const props = defineProps({
  signupInfo: {
    type: Object,
    default: null
  },
  isActivityEnded: {
    type: Boolean,
    default: false
  },
  isSignedUp: {
    type: Boolean,
    default: false
  },
  activityStartTime: {
    type: [String, Number, Date],
    default: null
  }
})

defineEmits(['sign-in', 'sign-out'])

const signInStatus = computed(() => {
  return props.signupInfo?.signedIn ? 'success' : null
})

const signOutStatus = computed(() => {
  if (props.signupInfo?.signedOut) {
    return 'approved'
  } else if (props.signupInfo?.checkoutPending) {
    return 'pending'
  }
  return null
})

const signOutReason = computed(() => {
  return props.signupInfo?.checkoutRejectReason || ''
})

const signupStatus = computed(() => props.signupInfo?.status || null)
const isSignedUp = computed(() => props.isSignedUp)
const isSignupApproved = computed(() => signupStatus.value === 'APPROVED')

const normalizeDate = (value) => {
  if (!value) return null

  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : value
  }

  if (typeof value === 'number') {
    const fromNumber = new Date(value)
    return Number.isNaN(fromNumber.getTime()) ? null : fromNumber
  }

  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (!trimmed) return null

    const candidate = trimmed.includes('T') ? trimmed : trimmed.replace(' ', 'T')
    const parsed = new Date(candidate)
    if (!Number.isNaN(parsed.getTime())) {
      return parsed
    }

    const [datePart, timePart = ''] = trimmed.split(/[\sT]/)
    const [year, month = 1, day = 1] = (datePart || '').split('-').map(Number)
    const [hour = 0, minute = 0, second = 0] = timePart.split(':').map(Number)

    if ([year, month, day].some((v) => Number.isNaN(v))) return null

    const fallback = new Date(year, month - 1, day, hour, minute, second || 0)
    return Number.isNaN(fallback.getTime()) ? null : fallback
  }

  return null
}

const now = ref(Date.now())
let countdownTimer = null

onMounted(() => {
  if (!countdownTimer) {
    countdownTimer = setInterval(() => {
      now.value = Date.now()
    }, 1000)
  }
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})

const activityStart = computed(() => {
  if (!props.activityStartTime) return null
  return normalizeDate(props.activityStartTime)
})

const isActivityStarted = computed(() => {
  if (!activityStart.value) return true
  return now.value >= activityStart.value.getTime()
})

const showCountdown = computed(() => {
  if (!activityStart.value) return false
  const diff = activityStart.value.getTime() - now.value
  return diff > 0 && diff <= 30 * 60 * 1000
})

const countdownText = computed(() => {
  if (!showCountdown.value || !activityStart.value) return ''
  const diffSeconds = Math.max(0, Math.floor((activityStart.value.getTime() - now.value) / 1000))
  const hours = Math.floor(diffSeconds / 3600)
  const minutes = Math.floor((diffSeconds % 3600) / 60)
  const seconds = diffSeconds % 60
  const pad = (num) => num.toString().padStart(2, '0')
  return `距离活动开始还有 ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`
})

const canSignIn = computed(() => {
  return isSignedUp.value && isSignupApproved.value && signInStatus.value !== 'success' && isActivityStarted.value
})

const canSignOut = computed(() => {
  return signInStatus.value === 'success' && !signOutStatus.value
})

const getSignInButtonText = () => {
  if (signInStatus.value === 'success') return '已签到'
  if (!isSignedUp.value) return '未报名'
  if (!isSignupApproved.value) {
    if (signupStatus.value === 'PENDING') return '待审核'
    if (signupStatus.value === 'REJECTED') return '报名未通过'
    return '不可签到'
  }
  if (!isActivityStarted.value) return '活动未开始'
  return '输入口令签到'
}

const getSignOutButtonText = () => {
  if (signInStatus.value !== 'success') return '请先签到'
  if (signOutStatus.value === 'pending') return '审核中'
  if (signOutStatus.value === 'approved') return '已签退'
  if (signOutStatus.value === 'rejected') return '签退被拒'
  return '输入口令签退'
}

const getSignOutTagType = () => {
  switch (signOutStatus.value) {
    case 'approved': return 'success'
    case 'rejected': return 'danger'
    case 'pending': return 'warning'
    default: return 'info'
  }
}

const getSignOutStatusText = () => {
  switch (signOutStatus.value) {
    case 'approved': return '已签退'
    case 'rejected': return '签退被拒'
    case 'pending': return '审核中'
    default: return '未签退'
  }
}
</script>

<style lang="scss" scoped>
.student-checkin-section {
  .checkin-content {
    display: grid;
    gap: 24px;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-weight: 600;
      color: #1f2532;

      :deep(.el-tag) {
        border-radius: 999px;
      }
    }

    .checkin-info,
    .checkout-info {
      text-align: center;
      padding: 4px;

      p {
        margin-bottom: 16px;
        color: #4e5969;
      }

      .time-info {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        margin-bottom: 16px;
        padding: 12px;
        background: rgba(22, 93, 255, 0.06);
        border-radius: 8px;
        color: #165dff;
        font-weight: 500;

        .el-icon {
          font-size: 16px;
        }
      }

      .countdown-info {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        margin-bottom: 16px;
        padding: 12px;
        background: rgba(250, 173, 20, 0.1);
        border-radius: 8px;
        color: #faad14;
        font-weight: 500;

        .el-icon {
          font-size: 16px;
        }
      }

      .no-time-info {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 12px;
        color: #909399;
        font-size: 14px;
      }
    }

    .checkout-success,
    .checkout-rejected {
      margin-bottom: 16px;
    }
  }

  .section-card {
    border-radius: 14px;
    border: 1px solid #e5e6eb;
    background: #fff;
    transition: box-shadow 0.2s ease, transform 0.2s ease;

    &:hover {
      box-shadow: 0 14px 32px rgba(15, 31, 64, 0.08);
      transform: translateY(-2px);
    }

    :deep(.el-card__header) {
      padding: 18px 24px;
      border-bottom: 1px solid #eef0f6;
      background: linear-gradient(180deg, #fafbff 0%, #ffffff 100%);
    }

    :deep(.el-card__body) {
      padding: 24px;
    }
  }
}

@media (max-width: 768px) {
  .student-checkin-section {
    .checkin-content {
      grid-template-columns: 1fr;
    }
  }
}
</style>
