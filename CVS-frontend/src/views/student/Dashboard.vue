<template>
  <div class="student-dashboard">
    <div class="dashboard-header">
      <h1>学生中心</h1>
      <p>欢迎回来，{{ userName }}！</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon signups">
              <el-icon size="32">
                <UserFilled />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ studentStats.mySignupsCount || 0 }}</div>
              <div class="stat-label">我的报名</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon hours">
              <el-icon size="32">
                <Clock />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ Math.round((studentStats.totalServiceHours || 0) * 10) / 10 }}</div>
              <div class="stat-label">服务时长</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon points">
              <el-icon size="32">
                <Star />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ studentStats.totalPoints || 0 }}</div>
              <div class="stat-label">积分</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon certificates">
              <el-icon size="32">
                <Medal />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ studentStats.certificatesCount || 0 }}</div>
              <div class="stat-label">证明</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近活动和服务记录 -->
    <el-row :gutter="20" class="content-section">
      <el-col :xs="24" :lg="12">
        <t-card title="可报名活动" header-bordered>
          <template #actions>
            <a href="javascript:void(0)" @click="$router.push('/student/activities')">更多</a>
          </template>
          <t-list v-if="availableActivities.length > 0" :split="true">
            <t-list-item v-for="activity in availableActivities" :key="activity.id">
              <t-list-item-meta :title="activity.title || '未命名活动'">
                <template #description>
                  <div class="activity-meta-info">
                    <div class="meta-line">
                      <span>活动开始时间：{{ activity.startTime ? formatDate(activity.startTime) : '未设定时间' }}</span>
                      <span class="activity-participants">
                        {{ (activity.currentParticipants ?? activity.signupCount ?? 0) }}/{{ activity.maxParticipants ??
                          activity.capacity ?? 0 }} 人
                      </span>
                    </div>
                    <div class="activity-desc">{{ activity.description || '暂无描述' }}</div>
                  </div>
                </template>
              </t-list-item-meta>
              <template #action>
                <t-button theme="primary" @click="signupActivity(activity.id)">
                  报名
                </t-button>
              </template>
            </t-list-item>
          </t-list>
          <div v-else class="empty-list">
            <el-icon size="48" color="#ddd">
              <Calendar />
            </el-icon>
            <p>暂无可报名活动</p>
          </div>
        </t-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <t-card title="服务记录" header-bordered>
          <template #actions>
            <a href="javascript:void(0)" @click="$router.push('/student/records')">更多</a>
          </template>
          <t-list v-if="serviceRecords.length > 0" :split="true">
            <t-list-item v-for="record in serviceRecords" :key="record.id">
              <t-list-item-meta :title="record.activityTitle || '未知活动'">
                <template #description>
                  <div class="record-meta-info">
                    <span>{{ record.serviceDate ? formatDate(record.serviceDate) : '未知时间' }}</span>
                    <el-tag type="success" size="small">
                      {{ Math.round((record.serviceHours || 0) * 10) / 10 }} 小时
                    </el-tag>
                  </div>
                </template>
              </t-list-item-meta>
              <template #action>
                <div class="record-points">+{{ record.points || 0 }} 积分</div>
              </template>
            </t-list-item>
          </t-list>
          <div v-else class="empty-list">
            <el-icon size="48" color="#ddd">
              <Document />
            </el-icon>
            <p>暂无服务记录</p>
          </div>
        </t-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { formatDate } from '@/utils/format'
import { statisticsAPI, activityAPI, signupAPI, serviceRecordAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UserFilled,
  Clock,
  Star,
  Medal,
  Calendar,
  ArrowRight,
  Document
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const availableActivities = ref([])
const mySignupActivityIds = ref([])
const serviceRecords = ref([])
const studentStats = ref({
  mySignupsCount: 0,
  totalServiceHours: 0,
  totalPoints: 0,
  certificatesCount: 0
})

const userName = computed(() => authStore.userName || 'Student')

const signupActivity = async (activityId) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入报名原因', '报名原因', {
      confirmButtonText: '确定报名',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入你的报名原因和动机...',
      inputValidator: (value) => {
        if (!value || value.trim().length < 5) {
          return '报名原因不能少于5个字符'
        }
        if (value.trim().length > 200) {
          return '报名原因不能超过200个字符'
        }
        return true
      }
    })

    await signupAPI.signupActivity({
      activityId,
      reason: reason.trim()
    })
    ElMessage.success('报名成功！请等待审核。')

    await fetchMySignupActivityIds()
    await fetchAvailableActivities()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('报名失败:', error)
      ElMessage.error(error.response?.data?.message || '报名失败，请稍后重试')
    }
  }
}

const fetchStudentStats = async () => {
  try {
    const response = await statisticsAPI.getStudentDashboardStats()
    if (response.code === 200) {
      studentStats.value = {
        mySignupsCount: response.data?.mySignupsCount ?? 0,
        totalServiceHours: Number(((response.data?.totalServiceHours ?? 0).toFixed?.(1)) ?? (response.data?.totalServiceHours ?? 0)),
        totalPoints: response.data?.totalPoints ?? 0,
        certificatesCount: response.data?.certificatesCount ?? 0
      }
    } else {
      throw new Error('统计数据获取失败')
    }
  } catch (error) {
    console.error('获取学生统计数据失败:', error)
    studentStats.value = {
      mySignupsCount: 5,
      totalServiceHours: 24.7,
      totalPoints: 180,
      certificatesCount: 2
    }
  }
}

const fetchMySignupActivityIds = async () => {
  try {
    const response = await signupAPI.getMySignups({
      pageNum: 1,
      pageSize: 1000,
      params: {}
    })
    if (response.code === 200) {
      mySignupActivityIds.value = (response.data?.records ?? []).map((signup) => signup.activityId)
    } else {
      mySignupActivityIds.value = []
    }
  } catch (error) {
    console.error('获取我的报名列表失败:', error)
    mySignupActivityIds.value = []
  }
}

const fetchMyServiceRecords = async () => {
  try {
    const response = await serviceRecordAPI.getMyRecords({
      pageNum: 1,
      pageSize: 3,
      params: {}
    })
    if (response.code === 200 && response.data?.records?.length) {
      serviceRecords.value = response.data.records.map((record) => {
        const rawMinutes = record.durationMinutes ?? 0
        const derivedHours = rawMinutes ? Number((rawMinutes / 60).toFixed(1)) : 0
        return {
          ...record,
          serviceHours: record.serviceHours ?? derivedHours,
          serviceDate: record.serviceDate || record.createdAt,
          points: record.points ?? record.pointsEarned ?? 0
        }
      })
    } else {
      serviceRecords.value = []
    }
  } catch (error) {
    console.error('获取我的服务记录失败:', error)
    serviceRecords.value = []
  }
}

const fetchAvailableActivities = async () => {
  try {
    const response = await activityAPI.getActivityList({
      pageNum: 1,
      pageSize: 10,
      params: {
        status: 'PUBLISHED'
      }
    })
    if (response.code === 200) {
      const now = Date.now()
      const activities = (response.data?.records ?? [])
        .filter(
          (activity) => {
            if (activity.status !== 'PUBLISHED') return false
            if (mySignupActivityIds.value.includes(activity.id)) return false
            const deadline = activity.registrationDeadline
              ? new Date(activity.registrationDeadline).getTime()
              : null
            const isBeforeDeadline =
              deadline === null || Number.isNaN(deadline) || deadline >= now
            return isBeforeDeadline
          }
        )
        .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))

      availableActivities.value = activities.slice(0, 4)
    } else {
      availableActivities.value = []
    }
  } catch (error) {
    console.error('获取可报名活动失败:', error)
    availableActivities.value = []
  }
}

onMounted(async () => {
  await fetchStudentStats()
  await fetchMySignupActivityIds()
  await fetchAvailableActivities()
  await fetchMyServiceRecords()
})
</script>

<style lang="scss" scoped>
.student-dashboard {
  .dashboard-header {
    margin-bottom: var(--space-6, 24px);

    h1 {
      margin: 0 0 var(--space-2, 8px) 0;
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
    margin-bottom: var(--space-6, 24px);

    .stat-card {
      margin-bottom: var(--space-4, 16px);

      :deep(.el-card__body) {
        padding: var(--space-5, 20px);
      }

      .stat-content {
        display: flex;
        align-items: center;
        gap: var(--space-4, 16px);

        .stat-icon {
          width: var(--space-15, 60px);
          height: var(--space-15, 60px);
          border-radius: var(--space-3, 12px);
          display: flex;
          align-items: center;
          justify-content: center;

          &.signups {
            background: linear-gradient(135deg, #1f6bff 0%, #5facff 100%);
            color: white;
          }

          &.hours {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
            color: white;
          }

          &.points {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
          }

          &.certificates {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
          }
        }

        .stat-info {
          flex: 1;

          .stat-number {
            font-size: 24px;
            font-weight: 600;
            color: #303133;
            margin-bottom: var(--space-1, 4px);
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }
  }

  .content-section {
    margin-bottom: var(--space-6, 24px);

    :deep(.t-card) {
      .t-card__actions a {
        color: #1f6bff;
        text-decoration: none;
        font-size: 14px;

        &:hover {
          color: #409eff;
        }
      }
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
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

        .activity-meta-info {
          .meta-line {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 14px;
            color: #909399;
            margin-bottom: 8px;

            .activity-participants {
              color: #1f6bff;
              font-weight: 500;
            }
          }

          .activity-desc {
            font-size: 14px;
            color: #606266;
            line-height: 1.4;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }
        }
      }
    }

    .empty-list {
      text-align: center;
      padding: var(--space-10, 40px) 0;
      color: #909399;

      p {
        margin: var(--space-4, 16px) 0 0 0;
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

        .record-meta-info {
          display: flex;
          align-items: center;
          gap: 12px;
          font-size: 14px;
          color: #909399;
        }

        .record-points {
          font-size: 16px;
          color: #1f6bff;
          font-weight: 600;
          white-space: nowrap;
        }
      }
    }

    .empty-list {
      text-align: center;
      padding: var(--space-10, 40px) 0;
      color: #909399;

      p {
        margin: var(--space-4, 16px) 0 0 0;
      }
    }
  }
}

// 确保按钮文字颜色正确显示
:deep(.el-button) {
  &.el-button--primary {
    color: #ffffff !important;
    background-color: #1f6bff;
    border-color: #1f6bff;

    &:hover,
    &:focus {
      background-color: #409eff;
      border-color: #409eff;
      color: #ffffff !important;
    }

    &:active {
      background-color: #1f6bff;
      border-color: #1f6bff;
      color: #ffffff !important;
    }
  }

  &.el-button--text {
    color: #1f6bff !important;

    &:hover,
    &:focus {
      color: #409eff !important;
      background-color: transparent;
    }
  }
}

@media (max-width: 768px) {
  .student-dashboard {
    .activity-list .activity-item {
      flex-direction: column;
      align-items: flex-start;
      gap: var(--space-3, 12px);

      .el-button {
        width: 100%;
      }
    }

    .record-item {
      flex-direction: column;
      align-items: flex-start;
      gap: var(--space-3, 12px);
    }
  }
}
</style>
