<template>
  <div class="teacher-checkin-section">
    <div class="teacher-check-grid">
      <el-card class="section-card teacher-action-card" shadow="never" data-highlight="signin">
        <template #header>
          <div class="card-header">
            <span>签到管理</span>
            <el-tag type="success">
              教师端
            </el-tag>
          </div>
        </template>
        <div class="teacher-card-body">
          <div v-if="!isActivityStarted" class="pre-activity-message">
            <div v-if="showCountdown" class="countdown-info">
              <el-icon>
                <Clock />
              </el-icon>
              <span>{{ countdownText }}</span>
            </div>
            <p>活动尚未开始，请在开始后再生成签到二维码。</p>
          </div>
          <div v-else>
            <p>生成签到二维码，让学生扫码完成签到并同步到系统。</p>
            <div class="teacher-stats">
              <div class="stat">
                <span class="label">已签到</span>
                <span class="value">{{ stats.checkinCompleted }}</span>
              </div>
              <div class="stat">
                <span class="label">报名总数</span>
                <span class="value">{{ stats.total }}</span>
              </div>
              <div class="stat">
                <span class="label">签到率</span>
                <span class="value">{{ stats.checkinRate }}%</span>
              </div>
            </div>

            <!-- 活动结束后显示签到学生列表 -->
            <div v-if="isActivityEnded && checkedInStudents.length > 0" class="student-list">
              <h4>已签到学生</h4>
              <div class="student-items">
                <div v-for="student in checkedInStudents" :key="student.id" class="student-item">
                  <span class="student-name">{{ student.name }}</span>
                  <span class="student-time">{{ formatDateTime(student.signInTime) }}</span>
                </div>
              </div>
            </div>

            <el-button v-if="!isActivityEnded" type="primary" @click="$emit('go-to-qr', 'checkin')"
              class="qr-link-button">
              生成签到二维码
            </el-button>
          </div>
        </div>
      </el-card>

      <el-card class="section-card teacher-action-card" shadow="never" data-highlight="signout">
        <template #header>
          <div class="card-header">
            <span>签退管理</span>
            <el-tag type="danger">
              教师端
            </el-tag>
          </div>
        </template>
        <div class="teacher-card-body">
          <div v-if="!isActivityStarted" class="pre-activity-message">
            <div v-if="showCountdown" class="countdown-info">
              <el-icon>
                <Clock />
              </el-icon>
              <span>{{ countdownText }}</span>
            </div>
            <p>活动尚未开始，请在开始后再生成签退二维码。</p>
          </div>
          <div v-else>
            <p>生成签退二维码，学生扫码后可提交签退申请与自评信息。</p>
            <div class="teacher-stats">
              <div class="stat">
                <span class="label">已签退</span>
                <span class="value">{{ stats.checkoutCompleted }}</span>
              </div>
              <div class="stat">
                <span class="label">待签退</span>
                <span class="value">{{ stats.checkoutPending }}</span>
              </div>
              <div class="stat">
                <span class="label">签退率</span>
                <span class="value">{{ stats.checkoutRate }}%</span>
              </div>
            </div>

            <!-- 活动结束后显示签退学生列表 -->
            <div v-if="isActivityEnded && checkedOutStudents.length > 0" class="student-list">
              <h4>已签退学生</h4>
              <div class="student-items">
                <div v-for="student in checkedOutStudents" :key="student.id" class="student-item">
                  <span class="student-name">{{ student.name }}</span>
                  <span class="student-time">{{ formatDateTime(student.signOutTime) }}</span>
                </div>
              </div>
            </div>

            <el-button v-if="!isActivityEnded" type="warning" plain @click="$emit('go-to-qr', 'checkout')"
              class="qr-link-button">
              生成签退二维码
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { Clock } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/format'

const props = defineProps({
  checkedInStudents: {
    type: Array,
    default: () => []
  },
  checkedOutStudents: {
    type: Array,
    default: () => []
  },
  isActivityEnded: {
    type: Boolean,
    default: false
  },
  totalParticipants: {
    type: Number,
    default: 0
  },
  activityStartTime: {
    type: [String, Number, Date],
    default: null
  },
  activityStatus: {
    type: String,
    default: ''
  }
})

defineEmits(['go-to-qr'])

const now = ref(Date.now())
let countdownTimer = null

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
  if (!activityStart.value || props.activityStatus !== 'PUBLISHED') return false
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

const stats = computed(() => {
  const total = Number(props.totalParticipants) || 0
  const checkinCompleted = props.checkedInStudents?.filter((item) => item && item.signInTime).length || 0
  const checkoutCompleted = props.checkedOutStudents?.filter((item) => item && item.signOutTime).length || 0
  const checkoutPending = Math.max(checkinCompleted - checkoutCompleted, 0)
  const checkinRate = total > 0 ? Math.round((checkinCompleted / total) * 100) : 0
  const checkoutRate = checkinCompleted > 0 ? Math.round((checkoutCompleted / checkinCompleted) * 100) : 0

  return {
    total,
    checkinCompleted,
    checkoutCompleted,
    checkoutPending,
    checkinRate,
    checkoutRate
  }
})
</script>

<style lang="scss" scoped>
.teacher-checkin-section {
  .teacher-check-grid {
    display: grid;
    gap: 24px;
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));

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

    .teacher-card-body {
      display: flex;
      flex-direction: column;
      gap: 18px;
      color: #4e5969;

      .pre-activity-message {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 12px;
        text-align: center;

        p {
          margin: 0;
        }
      }

      .countdown-info {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        padding: 12px 16px;
        border-radius: 8px;
        background: rgba(250, 173, 20, 0.12);
        color: #faad14;
        font-weight: 600;

        .el-icon {
          font-size: 16px;
        }
      }

      p {
        margin: 0;
        line-height: 1.6;
      }
    }

    .teacher-stats {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;

      .stat {
        flex: 1;
        min-width: 100px;
        background: rgba(22, 93, 255, 0.06);
        border-radius: 12px;
        padding: 12px 16px;
        text-align: center;

        .label {
          display: block;
          font-size: 13px;
          color: #8a919f;
          margin-bottom: 6px;
        }

        .value {
          font-size: 20px;
          font-weight: 700;
          color: #165dff;
        }
      }
    }

    .student-list {
      margin-top: 20px;

      h4 {
        margin: 0 0 12px 0;
        font-size: 15px;
        font-weight: 600;
        color: #1f2532;
      }

      .student-items {
        max-height: 300px;
        overflow-y: auto;
        border: 1px solid #e5e6eb;
        border-radius: 8px;

        .student-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 10px 14px;
          border-bottom: 1px solid #f0f1f5;

          &:last-child {
            border-bottom: none;
          }

          &:hover {
            background: rgba(22, 93, 255, 0.04);
          }

          .student-name {
            font-weight: 500;
            color: #1f2532;
          }

          .student-time {
            font-size: 13px;
            color: #8a919f;
          }
        }
      }
    }

    .qr-link-button {
      align-self: flex-start;
      border-radius: 999px;
      padding: 10px 24px;
      font-weight: 600;

      &.el-button--warning.is-plain {
        --el-button-bg-color: #f29f3f;
        --el-button-text-color: #ffffff;
        --el-button-border-color: #f29f3f;
        --el-button-hover-bg-color: #dc851c;
        --el-button-hover-text-color: #ffffff;
        --el-button-hover-border-color: #dc851c;
        --el-button-active-bg-color: #c96a00;
        --el-button-active-border-color: #c96a00;
        --el-button-disabled-text-color: rgba(255, 255, 255, 0.6);
        --el-button-disabled-bg-color: #f29f3f;
        --el-button-disabled-border-color: #f29f3f;
      }
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
  .teacher-checkin-section {
    .teacher-check-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
