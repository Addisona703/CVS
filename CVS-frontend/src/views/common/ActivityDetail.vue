<template>
  <div class="activity-detail">
    <div class="page-header">
      <div class="header-left">
        <el-button class="back-button" text @click="goBackToList">
          <el-icon>
            <ArrowLeft />
          </el-icon>
          返回
        </el-button>
        <div class="header-text">
          <h1 class="page-title">活动详情</h1>
          <p class="page-subtitle">查看活动安排、报名状态与签到签退进度</p>
        </div>
      </div>
    </div>

    <el-card v-if="activity" class="activity-card" shadow="hover">
      <div class="activity-header">
        <h1 class="activity-title">{{ activity.title }}</h1>
        <div class="activity-meta">
          <!-- <el-tag :type="getTypeColor(activity.type)">
            {{ getTypeLabel(activity.type) }}
          </el-tag> -->
          <el-tag :type="getStatusType(activity.status)">
            {{ getStatusLabel(activity.status) }}
          </el-tag>
        </div>
      </div>

      <!-- 标签页导航 -->
      <el-tabs v-model="activeTab" class="activity-tabs">
        <el-tab-pane label="活动信息" name="info" data-tab="info">
          <div class="activity-content">
            <el-row :gutter="20">
              <el-col :xs="24" :lg="16">
                <div class="activity-description" data-highlight="approved">
                  <h3>活动描述</h3>
                  <p>{{ activity.description }}</p>
                </div>

                <div class="activity-requirements" v-if="activity.requirements">
                  <h3>参与要求</h3>
                  <p>{{ activity.requirements }}</p>
                </div>

                <div class="activity-contact">
                  <h3>联系方式</h3>
                  <p>{{ activity.contactInfo }}</p>
                </div>

                <!-- 审核状态显示区域 -->
                <div v-if="showApprovalStatus" class="approval-status" :class="approvalStatusClass"
                  data-highlight="rejected">
                  <el-alert :title="approvalStatusTitle" :type="approvalStatusType"
                    :description="approvalStatusDescription" show-icon :closable="false" />
                </div>
              </el-col>

              <el-col :xs="24" :lg="8">
                <div class="activity-info">
                  <h3>活动信息</h3>
                  <div class="info-list">
                    <div class="info-item">
                      <el-icon>
                        <Location />
                      </el-icon>
                      <span class="label">活动地点：</span>
                      <span>{{ activity.location }}</span>
                    </div>
                    <div class="info-item">
                      <el-icon>
                        <Clock />
                      </el-icon>
                      <span class="label">开始时间：</span>
                      <span>{{ formatDateTime(activity.startTime) }}</span>
                    </div>
                    <div class="info-item">
                      <el-icon>
                        <Clock />
                      </el-icon>
                      <span class="label">结束时间：</span>
                      <span>{{ formatDateTime(activity.endTime) }}</span>
                    </div>
                    <div class="info-item">
                      <el-icon>
                        <Calendar />
                      </el-icon>
                      <span class="label">报名截止：</span>
                      <span>{{ formatDateTime(activity.registrationDeadline) }}</span>
                    </div>
                    <div class="info-item">
                      <el-icon>
                        <User />
                      </el-icon>
                      <span class="label">参与人数：</span>
                      <span>{{ activity.currentParticipants }}/{{ activity.maxParticipants }}人</span>
                    </div>
                    <div class="info-item">
                      <el-icon>
                        <Star />
                      </el-icon>
                      <span class="label">积分奖励：</span>
                      <span>{{ activity.points }}分</span>
                    </div>
                    <div class="info-item">
                      <el-icon>
                        <Avatar />
                      </el-icon>
                      <span class="label">创建者：</span>
                      <span>{{ activity.organizerName }}</span>
                    </div>
                  </div>
                </div>

                <div class="activity-actions" v-if="showActions">
                  <el-button v-if="canSignup" type="primary" size="large" class="action-button" @click="signupActivity">
                    立即报名
                  </el-button>
                  <el-button v-else-if="isSignedUp" type="info" size="large" class="action-button" disabled>
                    已报名
                  </el-button>
                  <el-button v-else-if="isFullOrExpired" type="info" size="large" class="action-button" disabled>
                    {{ getDisabledReason() }}
                  </el-button>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <el-tab-pane label="签到签退" name="checkin" data-tab="checkin">
          <StudentCheckinSection v-if="isStudent" :signup-info="mySignupInfo" :is-activity-ended="isActivityEnded"
            :is-signed-up="isSignedUp" :activity-start-time="activity?.startTime" @sign-in="handleSignIn"
            @sign-out="handleSignOut" />

          <TeacherCheckinSection v-else-if="isTeacher" :checked-in-students="checkedInStudents"
            :checked-out-students="checkedOutStudents" :is-activity-ended="isActivityEnded"
            :total-participants="activity?.currentParticipants || 0" :activity-start-time="activity?.startTime"
            :activity-status="activity?.status" @go-to-qr="goToQrPage" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 参与者列表 -->
    <ParticipantsList v-if="isTeacher && activity" :activity-id="route.params.id" />

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 错误状态 -->
    <el-result v-if="error" icon="error" title="加载失败" :sub-title="error">
      <template #extra>
        <el-button type="primary" @click="fetchActivityDetail">重试</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Location, Clock, Calendar, User, Star, Avatar } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useNotificationNavigation } from '@/composables/useNotificationNavigation'
import { activityAPI, signupAPI } from '@/api'
import { checkAPI } from '@/api/check'
import { formatDateTime } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import StudentCheckinSection from './components/StudentCheckinSection.vue'
import TeacherCheckinSection from './components/TeacherCheckinSection.vue'
import ParticipantsList from './components/ParticipantsList.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { checkNotificationNavigation } = useNotificationNavigation()

const activity = ref(null)
const loading = ref(false)
const error = ref('')
const mySignups = ref([])
const activeTab = ref('info')
const mySignupInfo = ref(null)
const checkedInStudents = ref([])
const checkedOutStudents = ref([])

const typeLabels = {
  ENVIRONMENTAL: '环保公益',
  COMMUNITY: '社区服务',
  EDUCATION: '教育支教',
  ELDERLY_CARE: '敬老助残',
  OTHER: '其他'
}

const isStudent = computed(() => authStore.user?.role === 'STUDENT')
const isTeacher = computed(() => authStore.user?.role === 'TEACHER' || authStore.user?.role === 'ADMIN')
const isActivityEnded = computed(() => {
  if (!activity.value) return false
  return new Date() > new Date(activity.value.endTime)
})

const showActions = computed(() => {
  return isStudent.value && activity.value?.status === 'PUBLISHED'
})



const canSignup = computed(() => {
  if (!activity.value) return false
  const now = new Date()
  const deadline = new Date(activity.value.registrationDeadline)
  return !isSignedUp.value &&
    activity.value.currentParticipants < activity.value.maxParticipants &&
    now < deadline
})

const isSignedUp = computed(() => {
  return mySignups.value.includes(activity.value?.id)
})

const isFullOrExpired = computed(() => {
  if (!activity.value) return false
  const now = new Date()
  const deadline = new Date(activity.value.registrationDeadline)
  return activity.value.currentParticipants >= activity.value.maxParticipants || now >= deadline
})

const getTypeLabel = (type) => {
  return typeLabels[type] || type
}

const getTypeColor = (type) => {
  const colorMap = {
    ENVIRONMENTAL: 'success',
    COMMUNITY: 'primary',
    EDUCATION: 'warning',
    ELDERLY_CARE: 'danger',
    OTHER: 'info'
  }
  return colorMap[type] || 'info'
}

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

const getDisabledReason = () => {
  if (!activity.value) return ''
  const now = new Date()
  const deadline = new Date(activity.value.registrationDeadline)

  if (activity.value.currentParticipants >= activity.value.maxParticipants) {
    return '名额已满'
  }
  if (now >= deadline) {
    return '报名已截止'
  }
  return '无法报名'
}

// 审核状态相关计算属性
const showApprovalStatus = computed(() => {
  const highlight = route.query.highlight
  return highlight === 'approved' || highlight === 'rejected'
})

const approvalStatusClass = computed(() => {
  const highlight = route.query.highlight
  return highlight === 'approved' ? 'status-approved' : 'status-rejected'
})

const approvalStatusType = computed(() => {
  const highlight = route.query.highlight
  return highlight === 'approved' ? 'success' : 'error'
})

const approvalStatusTitle = computed(() => {
  const highlight = route.query.highlight
  return highlight === 'approved' ? '报名审核通过' : '报名审核未通过'
})

const approvalStatusDescription = computed(() => {
  const highlight = route.query.highlight
  if (highlight === 'approved') {
    return '恭喜！您的报名申请已通过审核，请按时参加活动'
  } else {
    return '很抱歉，您的报名申请未通过审核，请查看拒绝原因'
  }
})



const fetchActivityDetail = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await activityAPI.getActivityById(route.params.id)
    if (response.code === 200) {
      activity.value = response.data
    }
  } catch (err) {
    console.error('获取活动详情失败:', err)
    error.value = '获取活动详情失败'
    // 使用模拟数据
    activity.value = {
      id: 1,
      title: '社区环保志愿活动',
      type: 'ENVIRONMENTAL',
      status: 'PUBLISHED',
      description: '参与社区环保活动，清理公园垃圾，保护环境从我做起。活动将在市中心公园进行，需要参与者自备垃圾袋和手套。我们将提供必要的工具和指导，希望大家能够积极参与，为环保事业贡献自己的力量。',
      requirements: '1. 身体健康，能够进行户外活动\n2. 自备垃圾袋和手套\n3. 穿着适合户外活动的服装\n4. 准时参加，不得无故缺席',
      location: '市中心公园',
      startTime: '2024-01-20T09:00:00',
      endTime: '2024-01-20T17:00:00',
      registrationDeadline: '2024-01-18T23:59:59',
      maxParticipants: 30,
      currentParticipants: 25,
      points: 15,
      organizerName: '张老师',
      contactInfo: '联系电话：138-0013-8001\n邮箱：zhang@example.com'
    }
  } finally {
    loading.value = false
  }
}



const fetchMySignups = async () => {
  if (isStudent.value) {
    try {
      const response = await signupAPI.getMySignups({
        pageNum: 1,
        pageSize: 100,
        params: {}
      })
      if (response.code === 200) {
        const signups = response.data.records || []
        mySignups.value = signups.map(s => s.activityId)

        // 查找当前活动的报名信息
        const currentSignup = signups.find(s => s.activityId === parseInt(route.params.id))
        if (currentSignup) {
          mySignupInfo.value = currentSignup
        }
      }
    } catch (err) {
      console.error('获取我的报名失败:', err)
    }
  }
}

const fetchCheckinCheckoutData = async () => {
  if (!isTeacher.value) return

  try {
    // 获取已签到的学生
    const checkinResponse = await signupAPI.getActivitySignups(route.params.id, {
      pageNum: 1,
      pageSize: 1000,
      params: {
        signedIn: true
      }
    })
    if (checkinResponse.code === 200) {
      checkedInStudents.value = checkinResponse.data.records || []
    }

    // 获取已签退的学生
    const checkoutResponse = await signupAPI.getActivitySignups(route.params.id, {
      pageNum: 1,
      pageSize: 1000,
      params: {
        signedOut: true
      }
    })
    if (checkoutResponse.code === 200) {
      checkedOutStudents.value = checkoutResponse.data.records || []
    }
  } catch (err) {
    console.error('获取签到签退数据失败:', err)
  }
}



const signupActivity = async () => {
  try {
    // 先显示报名原因输入框
    const { value: reason } = await ElMessageBox.prompt(
      `请输入报名 "${activity.value.title}" 的原因：`,
      '报名原因',
      {
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
      }
    )

    await signupAPI.signupActivity({
      activityId: activity.value.id,
      reason: reason.trim()
    })
    ElMessage.success('报名成功！请等待审核。')

    // 更新本地状态
    mySignups.value.push(activity.value.id)
    activity.value.currentParticipants++
  } catch (err) {
    if (err !== 'cancel') {
      console.error('报名失败:', err)
      ElMessage.error(err.response?.data?.message || '报名失败，请稍后重试')
    }
  }
}

const handleSignIn = async () => {
  try {
    const { value: token } = await ElMessageBox.prompt(
      '请输入教师提供的签到口令：',
      '签到验证',
      {
        confirmButtonText: '立即签到',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入签到口令',
        inputValidator: (value) => {
          if (!value || !value.trim()) {
            return '签到口令不能为空'
          }
          return true
        }
      }
    )

    await checkAPI.studentCheckin({ token: token.trim() })
    ElMessage.success('签到成功！')
    await fetchMySignups()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('签到失败:', error)
      ElMessage.error(error.response?.data?.message || '签到失败，请稍后重试')
    }
  }
}

const handleSignOut = async () => {
  try {
    const { value: token } = await ElMessageBox.prompt(
      '请输入教师提供的签退口令：',
      '签退验证',
      {
        confirmButtonText: '下一步',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入签退口令',
        inputValidator: (value) => {
          if (!value || !value.trim()) {
            return '签退口令不能为空'
          }
          return true
        }
      }
    )

    const { value: ratingValue } = await ElMessageBox.prompt(
      '请为本次服务打分（1-5分）：',
      '自我评分',
      {
        confirmButtonText: '下一步',
        cancelButtonText: '取消',
        inputType: 'number',
        inputValue: '5',
        inputValidator: (value) => {
          const num = Number(value)
          if (!Number.isInteger(num) || num < 1 || num > 5) {
            return '评分需为 1-5 的整数'
          }
          return true
        }
      }
    )

    const studentRating = Number(ratingValue)

    const { value: reason } = await ElMessageBox.prompt(
      '请输入本次服务的签退自评（不少于5个字，可填写主要收获或感想）：',
      '提交签退自评',
      {
        confirmButtonText: '提交签退',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入签退自评...',
        inputValidator: (value) => {
          if (!value || value.trim().length < 5) {
            return '签退自评不能少于5个字符'
          }
          return true
        }
      }
    )

    await checkAPI.studentCheckout({
      token: token.trim(),
      studentEvaluation: reason.trim(),
      studentRating
    })

    ElMessage.success('签退申请已提交，请等待审核')
    await fetchMySignups()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('签退申请失败:', error)
      ElMessage.error(error.response?.data?.message || '签退申请失败，请稍后重试')
    }
  }
}

const goToQrPage = (type = 'checkin') => {
  if (!activity.value) return
  const query = type === 'checkout' ? { type } : {}
  router.push({
    name: 'ActivityQrCode',
    params: { id: activity.value.id },
    query
  })
}

// 返回活动列表
const goBackToList = () => {
  if (isStudent.value) {
    // 学生返回到"我的报名"页面
    router.push({ name: 'MySignups' })
  } else if (isTeacher.value) {
    // 教师端智能返回
    // 检查历史记录中上一页是否是二维码页面
    const historyState = window.history.state
    const previousRoute = historyState?.back
    
    // 如果上一页是二维码页面，返回到"我的活动"
    if (previousRoute && previousRoute.includes('/qrcode')) {
      router.push({ name: 'MyActivities' })
    } else {
      // 否则返回上一页
      router.back()
    }
  } else {
    // 其他情况返回上一页
    router.back()
  }
}

// 初始化标签页
const initializeTab = () => {
  const tab = route.query.tab
  if (tab && ['info', 'checkin'].includes(tab)) {
    activeTab.value = tab
  }
}

onMounted(async () => {
  await fetchActivityDetail()
  await fetchMySignups()
  await fetchCheckinCheckoutData()
  initializeTab()
  checkNotificationNavigation()
})
</script>

<style lang="scss" scoped>
.activity-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .back-button {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      font-size: 14px;
      font-weight: 500;
      border-radius: 999px;
      color: var(--primary-color, #165dff);
      transition: background-color 0.2s ease, color 0.2s ease;

      :deep(.el-icon) {
        font-size: 16px;
      }

      &:hover {
        background-color: var(--primary-color-light-bg, rgba(22, 93, 255, 0.08));
        color: var(--primary-color-dark, #0c42b4);
      }
    }

    .header-text {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .page-title {
      margin: 0;
      font-size: 26px;
      font-weight: 600;
      color: #1f2532;
      letter-spacing: 0.2px;
    }

    .page-subtitle {
      margin: 0;
      color: #606266;
      font-size: 14px;
    }
  }

  .activity-card {
    border: none;
    border-radius: 16px;
    background: #fff;
    box-shadow: 0 18px 40px rgba(15, 31, 64, 0.08);

    :deep(.el-card__body) {
      padding: 32px;
    }
  }

  .activity-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 20px;
    margin-bottom: 24px;

    .activity-title {
      margin: 0;
      font-size: 28px;
      font-weight: 600;
      color: #1f2532;
      flex: 1;
    }

    .activity-meta {
      display: flex;
      align-items: center;
      gap: 12px;

      :deep(.el-tag) {
        border-radius: 999px;
        padding: 0 14px;
        font-size: 13px;
        font-weight: 500;
        line-height: 28px;
      }
    }
  }

  .activity-tabs {
    margin-top: 16px;

    :deep(.el-tabs__header) {
      margin-bottom: 24px;
    }

    :deep(.el-tabs__nav-wrap)::after {
      background: transparent;
    }

    :deep(.el-tabs__item) {
      padding: 12px 20px;
      font-weight: 500;
      color: #4e5969;
      transition: color 0.2s ease;
    }

    :deep(.el-tabs__item.is-active) {
      color: var(--primary-color, #165dff);
    }

    :deep(.el-tabs__active-bar) {
      height: 3px;
      border-radius: 2px;
      background-color: var(--primary-color, #165dff);
    }
  }

  .activity-content {

    .activity-description,
    .activity-requirements,
    .activity-contact {
      margin-bottom: 28px;

      h3 {
        margin: 0 0 12px 0;
        font-size: 18px;
        font-weight: 600;
        color: #1f2532;
      }

      p {
        margin: 0;
        color: #4e5969;
        line-height: 1.75;
        white-space: pre-line;
      }
    }

    .activity-info {
      background: var(--info-bg-gradient, linear-gradient(180deg, #f7f9ff 0%, #ffffff 100%));
      padding: 24px;
      border-radius: 12px;
      border: 1px solid var(--primary-border, rgba(22, 93, 255, 0.08));
      margin-bottom: 24px;

      h3 {
        margin: 0 0 18px 0;
        font-size: 18px;
        font-weight: 600;
        color: #1f2532;
      }

      .info-list {
        display: flex;
        flex-direction: column;
        gap: 14px;

        .info-item {
          display: flex;
          align-items: center;
          font-size: 14px;
          color: #4e5969;

          .el-icon {
            color: var(--primary-color, #165dff);
            margin-right: 10px;
            font-size: 16px;
          }

          .label {
            color: #8a919f;
            min-width: 96px;
            display: inline-flex;
            align-items: center;
          }
        }
      }
    }

    .activity-actions {
      margin-top: 16px;

      .action-button {
        width: 100%;
        height: 44px;
        border-radius: 10px;
        font-weight: 600;
        letter-spacing: 0.2px;
      }
    }
  }

  .approval-status {
    margin-top: 16px;
    padding: 16px;
    border-radius: 12px;
    background: var(--primary-color-light-bg, rgba(22, 93, 255, 0.04));
  }





  .loading {
    padding: 24px;
  }
}

@media (max-width: 768px) {
  .activity-detail {
    padding: 16px;
    gap: 20px;

    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }

    .activity-card :deep(.el-card__body) {
      padding: 24px 20px;
    }

    .activity-header {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}
</style>
