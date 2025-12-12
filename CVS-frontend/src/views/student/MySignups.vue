<template>
  <div class="my-signups">
    <div class="page-header">
      <h1>我的报名</h1>
    </div>

    <!-- 状态筛选 -->
    <el-card class="filter-card">
      <el-radio-group v-model="statusFilter" @change="handleFilterChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="PENDING">待审核</el-radio-button>
        <el-radio-button label="APPROVED">已通过</el-radio-button>
        <el-radio-button label="REJECTED">已拒绝</el-radio-button>
      </el-radio-group>
    </el-card>

    <!-- 报名列表 -->
    <div class="signup-list">
      <el-card v-for="signup in signupList" :key="signup.id" class="signup-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <h3 class="activity-title">{{ signup.activityTitle }}</h3>
              <span class="activity-subtitle">{{ signup.location }}</span>
            </div>
            <el-tag :type="getStatusType(signup.status)" size="large">
              {{ getStatusLabel(signup.status) }}
            </el-tag>
          </div>
        </template>

        <div class="card-content">
          <div class="info-row">
            <div class="info-item">
              <el-icon>
                <Clock />
              </el-icon>
              <span>活动时间：{{ formatDateTime(signup.startTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon>
                <Calendar />
              </el-icon>
              <span>报名时间：{{ formatDateTime(signup.createdAt) }}</span>
            </div>
            <div class="info-item" v-if="signup.points">
              <el-icon>
                <Star />
              </el-icon>
              <span>上限积分：{{ signup.points }}</span>
            </div>
          </div>

          <div class="signup-progress" v-if="signup.status === 'APPROVED'">
            <div class="progress-item" :class="{ active: signup.signedIn }">
              <el-icon>
                <Check />
              </el-icon>
              <span>已签到</span>
            </div>
            <div class="progress-item" :class="{ active: signup.signedOut }">
              <el-icon>
                <Check />
              </el-icon>
              <span>已签退</span>
            </div>
            <div class="progress-item" :class="{ active: signup.recordCreated }">
              <el-icon>
                <Check />
              </el-icon>
              <span>记录已生成</span>
            </div>
          </div>

          <div class="rejection-reason" v-if="signup.status === 'REJECTED' && signup.rejectReason">
            <el-alert :title="signup.rejectReason" type="error" :closable="false" show-icon />
          </div>
        </div>

        <template #footer>
          <div class="card-actions">
            <a href="javascript:void(0)" @click="viewActivity(signup.activityId)">查看活动</a>
            <a href="javascript:void(0)" @click="viewSignupDetail(signup)">查看详情</a>
            <a v-if="canCancel(signup)" href="javascript:void(0)" class="danger" @click="cancelSignup(signup)">
              取消报名
            </a>
            <a v-if="signup.status === 'APPROVED' && !signup.recordCreated" href="javascript:void(0)" class="primary"
              @click="openCheckDialog">
              签到/签退
            </a>
            <a v-if="signup.status === 'APPROVED' && signup.recordCreated" href="javascript:void(0)" class="success"
              @click="viewRecord(signup.recordId)">
              查看记录
            </a>
          </div>
        </template>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="signupList.length > 0">
      <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size"
        :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && signupList.length === 0" description="暂无报名记录" />

    <SignupDetailDialog v-model="detailDialogVisible" :signup="selectedSignup" />
    <CheckInOutDialog v-model="checkDialogVisible" @success="handleCheckSuccess" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Clock, Calendar, Star, Check } from '@element-plus/icons-vue'
import { signupAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import { usePagination } from '@/composables/usePagination'
import SignupDetailDialog from './components/SignupDetailDialog.vue'
import CheckInOutDialog from './components/CheckInOutDialog.vue'

const router = useRouter()
const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const signupList = ref([])
const statusFilter = ref('')
const detailDialogVisible = ref(false)
const selectedSignup = ref(null)
const checkDialogVisible = ref(false)

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

const fetchSignupList = async () => {
  loading.value = true
  try {
    // 使用POST请求和分页DTO结构
    const pageRequest = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      params: {
        // 添加筛选条件
        status: statusFilter.value || undefined
      }
    }

    const response = await signupAPI.getMySignups(pageRequest)
    if (response.code === 200) {
      signupList.value = response.data.records || []
      updatePagination(response.data)
    } else {
      ElMessage.error(response.message || '获取报名列表失败')
      signupList.value = []
    }
  } catch (error) {
    console.error('获取报名列表失败:', error)
    ElMessage.error('网络错误，请稍后重试')
    signupList.value = []
    updatePagination({
      current: 1,
      size: pagination.size,
      total: 0,
      pages: 0
    })
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  pagination.current = 1
  fetchSignupList()
}

const viewActivity = (activityId) => {
  router.push(`/activities/${activityId}`)
}

const viewRecord = (recordId) => {
  router.push(`/student/records/${recordId}`)
}

const canCancel = (signup) => {
  // 只有待审核的报名可以取消，已通过的报名不允许取消
  return signup.status === 'PENDING'
}

const viewSignupDetail = (signup) => {
  selectedSignup.value = { ...signup }
  detailDialogVisible.value = true
}

const cancelSignup = async (signup) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消报名 "${signup.activityTitle}" 吗？`,
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await signupAPI.cancelSignup(signup.id)
    ElMessage.success('取消报名成功')
    fetchSignupList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消报名失败:', error)
      ElMessage.error(error.response?.data?.message || '取消报名失败，请稍后重试')
    }
  }
}

const openCheckDialog = () => {
  checkDialogVisible.value = true
}

const handleCheckSuccess = (type) => {
  // 签到或签退成功后刷新列表
  setTimeout(() => {
    fetchSignupList()
  }, 1500)
}

onMounted(() => {
  fetchSignupList()
})
</script>

<style lang="scss" scoped>
.my-signups {
  .page-header {
    margin-bottom: 20px;

    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }

  .filter-card {
    margin-bottom: 20px;
  }

  .signup-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 20px;
    margin-bottom: 20px;

    .signup-card {
      display: flex;
      flex-direction: column;
      height: 100%;

      :deep(.el-card__header) {
        padding: 20px;
        border-bottom: 1px solid #f0f0f0;
      }

      :deep(.el-card__body) {
        flex: 1;
        display: flex;
        padding: 20px;
      }

      :deep(.el-card__footer) {
        padding: 16px 20px;
        border-top: 1px solid #f0f0f0;
        background-color: #fafafa;
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;

        .header-left {
          flex: 1;
          margin-right: 16px;

          .activity-title {
            margin: 0 0 8px 0;
            font-size: 18px;
            font-weight: 600;
            color: #303133;
            line-height: 1.4;
          }

          .activity-subtitle {
            font-size: 14px;
            color: #909399;
          }
        }
      }

      .card-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 16px;

        .info-row {
          display: flex;
          flex-direction: column;
          gap: 12px;

          .info-item {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            color: #606266;

            .el-icon {
              color: #909399;
              font-size: 16px;
            }
          }
        }

        .signup-progress {
          display: flex;
          gap: 24px;
          padding: 16px;
          background-color: #f5f7fa;
          border-radius: 4px;

          .progress-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            color: #c0c4cc;

            &.active {
              color: #67c23a;
              font-weight: 500;
            }

            .el-icon {
              font-size: 16px;
            }
          }
        }

        .rejection-reason {
          margin-top: 0;
        }
      }

      .card-actions {
        display: flex;
        gap: 16px;
        align-items: center;

        a {
          color: #409eff;
          text-decoration: none;
          font-size: 14px;
          transition: color 0.3s;

          &:hover {
            color: #66b1ff;
          }

          &.danger {
            color: #f56c6c;

            &:hover {
              color: #f78989;
            }
          }

          &.primary {
            color: #409eff;
            font-weight: 600;

            &:hover {
              color: #66b1ff;
            }
          }

          &.success {
            color: #67c23a;

            &:hover {
              color: #85ce61;
            }
          }
        }
      }
    }
  }

  .pagination {
    text-align: center;
    margin-top: 20px;
  }
}

@media (max-width: 1200px) {
  .my-signups .signup-list {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .my-signups {
    .signup-list {
      grid-template-columns: 1fr;

      .signup-card {
        .signup-progress {
          flex-direction: column;
          gap: 12px;
        }

        .card-actions {
          flex-wrap: wrap;
        }
      }
    }
  }
}
</style>
