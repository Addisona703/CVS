<template>
  <div class="teacher-dashboard">
    <div class="dashboard-header">
      <h1>教师工作台</h1>
      <p>欢迎回来，{{ userName }}老师！</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon activities">
              <el-icon size="32">
                <Calendar />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ teacherStats.myActivitiesCount }}</div>
              <div class="stat-label">我的活动</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon signups">
              <el-icon size="32">
                <UserFilled />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ teacherStats.totalSignupsCount }}</div>
              <div class="stat-label">参与学生</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon size="32">
                <Clock />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ teacherStats.pendingApprovalsCount }}</div>
              <div class="stat-label">待审核报名</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon records">
              <el-icon size="32">
                <Document />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ teacherStats.serviceRecordsCount }}</div>
              <div class="stat-label">待审核志愿证明</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 我的活动和正在参与的活动 -->
    <el-row :gutter="20" class="activities-section">
      <el-col :xs="24" :lg="12">
        <t-card title="我的活动" header-bordered>
          <template #actions>
            <a href="javascript:void(0)" @click="$router.push('/teacher/activities')">查看更多</a>
          </template>
          <t-list v-if="myActivities.length > 0" :split="true">
            <t-list-item v-for="activity in myActivities" :key="activity.id">
              <t-list-item-meta :title="activity.title">
                <template #description>
                  <div class="activity-meta-info">
                    <div class="meta-line">
                      <span>{{ formatDate(activity.startTime) }}</span>
                      <el-tag :type="getStatusType(activity.status)" size="small">
                        {{ getStatusLabel(activity.status) }}
                      </el-tag>
                    </div>
                    <div class="activity-stats">
                      报名人数: {{ activity.currentParticipants }} / {{ activity.maxParticipants }}
                    </div>
                  </div>
                </template>
              </t-list-item-meta>
              <template #action>
                <t-button theme="primary" @click="viewActivity(activity.id)">
                  查看
                </t-button>
              </template>
            </t-list-item>
          </t-list>
          <div v-else class="empty-list">
            <el-icon size="48" color="#ddd">
              <Calendar />
            </el-icon>
            <p>暂无活动</p>
          </div>
        </t-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <t-card title="正在进行的活动" header-bordered>
          <template #actions>
            <a href="javascript:void(0)" @click="$router.push('/teacher/activities')">查看更多</a>
          </template>
          <t-list v-if="ongoingActivities.length > 0" :split="true">
            <t-list-item v-for="activity in ongoingActivities" :key="activity.id">
              <t-list-item-meta :title="activity.title">
                <template #description>
                  <div class="activity-meta-info">
                    <div class="meta-line">
                      <span>{{ formatDate(activity.startTime) }} - {{ formatDate(activity.endTime) }}</span>
                      <el-tag :type="getActivityStatusType(activity)" size="small">
                        {{ getActivityStatusText(activity) }}
                      </el-tag>
                    </div>
                    <div class="activity-stats">
                      报名人数: {{ activity.currentParticipants }} / {{ activity.maxParticipants }}
                    </div>
                  </div>
                </template>
              </t-list-item-meta>
              <template #action>
                <t-button theme="primary" @click="viewActivity(activity.id)">
                  查看
                </t-button>
              </template>
            </t-list-item>
          </t-list>
          <div v-else class="empty-list">
            <el-icon size="48" color="#ddd">
              <Clock />
            </el-icon>
            <p>暂无正在进行的活动</p>
          </div>
        </t-card>
      </el-col>
    </el-row>

    <ActivityDetailDialog v-model="activityDetailVisible" :activity-id="selectedActivity?.id"
      :activity="selectedActivity" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { formatDate } from '@/utils/format'
import { statisticsAPI, activityAPI } from '@/api'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import ActivityDetailDialog from './components/ActivityDetailDialog.vue'
import {
  Calendar,
  UserFilled,
  Clock,
  Document
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const activityDetailVisible = ref(false)
const selectedActivity = ref(null)

const teacherStats = ref({
  myActivitiesCount: 0,
  totalSignupsCount: 0,
  pendingApprovalsCount: 0,
  serviceRecordsCount: 0
})

// 我的活动数据
const myActivities = ref([])

// 正在进行的活动列表
const ongoingActivities = ref([])

const userName = computed(() => authStore.userName)

const getStatusLabel = (status) => {
  return STATUS_LABELS[status] || status
}

const getStatusType = (status) => {
  const colorMap = {
    success: 'success',
    danger: 'danger',
    warning: 'warning',
    info: 'info'
  }
  return colorMap[STATUS_COLORS[status]] || 'info'
}

// 获取活动状态标签类型
const getActivityStatusType = (activity) => {
  const now = new Date()
  const startTime = new Date(activity.startTime)
  const endTime = new Date(activity.endTime)
  const timeDiff = startTime - now
  const thirtyMinutes = 30 * 60 * 1000

  if (now >= startTime && now <= endTime) {
    return 'success' // 进行中
  } else if (timeDiff > 0 && timeDiff <= thirtyMinutes) {
    return 'warning' // 即将开始
  }
  return 'info'
}

// 获取活动状态文本
const getActivityStatusText = (activity) => {
  const now = new Date()
  const startTime = new Date(activity.startTime)
  const endTime = new Date(activity.endTime)
  const timeDiff = startTime - now
  const thirtyMinutes = 30 * 60 * 1000

  if (now >= startTime && now <= endTime) {
    return '进行中'
  } else if (timeDiff > 0 && timeDiff <= thirtyMinutes) {
    const minutes = Math.ceil(timeDiff / (60 * 1000))
    return `${minutes}分钟后开始`
  }
  return '即将开始'
}

// 获取正在进行的活动
const fetchOngoingActivities = async () => {
  try {
    const response = await activityAPI.getMyActivities({
      pageNum: 1,
      pageSize: 100,
      params: {
        status: 'PUBLISHED'
      }
    })

    if (response.code === 200) {
      const now = new Date()
      const thirtyMinutes = 30 * 60 * 1000

      // 筛选出即将开始（半小时内）或正在进行的活动
      const activities = (response.data.records || []).filter(activity => {
        const startTime = new Date(activity.startTime)
        const endTime = new Date(activity.endTime)
        const timeDiff = startTime - now

        // 活动正在进行中
        if (now >= startTime && now <= endTime) {
          return true
        }

        // 活动即将开始（半小时内）
        if (timeDiff > 0 && timeDiff <= thirtyMinutes) {
          return true
        }

        return false
      })

      // 按开始时间排序，最近的在前面
      ongoingActivities.value = activities.sort((a, b) => {
        return new Date(a.startTime) - new Date(b.startTime)
      }).slice(0, 5) // 只显示前5个
    }
  } catch (error) {
    console.error('获取正在进行的活动失败:', error)
    ongoingActivities.value = []
  }
}

const fetchTeacherStats = async () => {
  try {
    const result = await statisticsAPI.getTeacherDashboardStats()
    teacherStats.value = result.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 获取我的活动列表
const fetchMyActivities = async () => {
  try {
    const response = await activityAPI.getMyActivities({
      pageNum: 1,
      pageSize: 3,
      params: {}
    })
    if (response.code === 200) {
      // 按照时间排序，并且优先展示草稿活动
      const activities = response.data.records || []
      myActivities.value = activities.sort((a, b) => {
        // 先按状态排序，草稿优先
        if (a.status === 'DRAFT' && b.status !== 'DRAFT') return -1
        if (a.status !== 'DRAFT' && b.status === 'DRAFT') return 1
        // 相同状态按时间排序
        return new Date(b.startTime) - new Date(a.startTime)
      })
    }
  } catch (error) {
    console.error('获取我的活动失败:', error)
    // 使用模拟数据作为备用
    myActivities.value = [
      {
        id: 1,
        title: '社区环保志愿活动',
        startTime: '2024-01-20T09:00:00',
        status: 'DRAFT',
        signupCount: 15,
        maxParticipants: 30
      },
      {
        id: 2,
        title: '敬老院志愿服务',
        startTime: '2024-01-25T14:00:00',
        status: 'PUBLISHED',
        signupCount: 20,
        maxParticipants: 25
      },
      {
        id: 3,
        title: '图书馆志愿服务',
        startTime: '2024-01-30T13:00:00',
        status: 'DRAFT',
        signupCount: 8,
        maxParticipants: 20
      },
      {
        id: 4,
        title: '校园清洁活动',
        startTime: '2024-02-05T08:00:00',
        status: 'PUBLISHED',
        signupCount: 12,
        maxParticipants: 15
      }
    ]
  }
}

// 查看活动详情
const viewActivity = (id) => {
  const activity = myActivities.value.find(item => item.id === id)
  selectedActivity.value = activity ? { ...activity } : { id }
  activityDetailVisible.value = true
}

onMounted(() => {
  fetchTeacherStats()
  fetchMyActivities()
  fetchOngoingActivities()
})
</script>

<style lang="scss" scoped>
.teacher-dashboard {
  .dashboard-header {
    margin-bottom: 24px;

    h1 {
      margin: 0 0 8px 0;
      font-size: 28px;
      color: #303133;
    }

    p {
      margin: 0;
      color: #606266;
      font-size: 16px;
    }
  }

  .stats-cards {
    margin-bottom: 24px;

    .stat-card {
      margin-bottom: 16px;
      box-shadow: var(--card-shadow);
      transition: all var(--transition-duration) var(--transition-timing);

      &:hover {
        box-shadow: var(--card-shadow-hover);
        transform: translateY(-4px);
      }

      :deep(.el-card__body) {
        padding: 20px;
      }

      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;

          &.activities {
            background: var(--stat-gradient-success, linear-gradient(135deg, #67c23a 0%, #85ce61 100%));
            color: #ffffff;
          }

          &.signups {
            background: var(--stat-gradient-primary, linear-gradient(135deg, #409eff 0%, #66b1ff 100%));
            color: #ffffff;
          }

          &.pending {
            background: var(--stat-gradient-warning, linear-gradient(135deg, #e6a23c 0%, #ebb563 100%));
            color: #ffffff;
          }

          &.records {
            background: var(--stat-gradient-info, linear-gradient(135deg, #909399 0%, #a6a9ad 100%));
            color: #ffffff;
          }
        }

        .stat-info {
          flex: 1;

          .stat-number {
            font-size: 24px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 4px;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }
  }

  .my-activities-card,
  .active-signups-card {
    box-shadow: var(--card-shadow);
    transition: all var(--transition-duration) var(--transition-timing);

    &:hover {
      box-shadow: var(--card-shadow-hover);
    }
  }

  :deep(.t-card) {
    .t-card__actions a {
      color: #67c23a;
      text-decoration: none;
      font-size: 14px;

      &:hover {
        color: #85ce61;
      }
    }
  }

  :deep(.t-list) {
    .t-list-item {
      padding: 16px 0;

      .t-list-item-meta__title {
        font-size: 16px;
        color: #303133;
        font-weight: 500;
        margin-bottom: 8px;
      }

      .activity-meta-info,
      .signup-meta-info {
        .meta-line {
          display: flex;
          align-items: center;
          gap: 12px;
          font-size: 14px;
          color: #909399;
          margin-bottom: 4px;

          &:last-child {
            margin-bottom: 0;
          }
        }

        .activity-stats {
          font-size: 14px;
          color: #67c23a;
          font-weight: 500;
          margin-top: 4px;
        }
      }

      .t-button--theme-primary {
        background-color: #67c23a;
        border-color: #67c23a;

        &:hover {
          background-color: #85ce61;
          border-color: #85ce61;
        }

        &:active {
          background-color: #5daf34;
          border-color: #5daf34;
        }
      }
    }
  }

  .empty-list {
    text-align: center;
    padding: 32px 0;
    color: #909399;

    p {
      margin-top: 12px;
    }
  }
}

@media (max-width: 768px) {
  .teacher-dashboard {
    :deep(.t-list) {
      .t-list-item {
        flex-direction: column;
        align-items: flex-start;

        .el-button {
          width: 100%;
        }
      }
    }
  }
}
</style>
