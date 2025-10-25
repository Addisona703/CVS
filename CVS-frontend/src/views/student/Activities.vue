<template>
  <div class="activities">
    <div class="page-header">
      <h1>活动报名</h1>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="活动标题">
          <el-input v-model="searchForm.title" placeholder="请输入活动标题" clearable />
        </el-form-item>
        <el-form-item label="活动时间">
          <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期"
            end-placeholder="结束日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 活动列表 -->
    <div class="activity-list">
      <el-card v-for="activity in activityList" :key="activity.id" class="activity-item" shadow="hover">
        <div class="details">
          <div class="activity-header">
            <h3 class="activity-title">{{ activity.title }}</h3>
          </div>

          <p class="activity-description">{{ activity.description }}</p>

          <div class="activity-info">
            <div class="info-item">
              <el-icon>
                <Location />
              </el-icon>
              <span>{{ activity.location }}</span>
            </div>
            <div class="info-item">
              <el-icon>
                <Clock />
              </el-icon>
              <span>{{ formatDateTime(activity.startTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon>
                <User />
              </el-icon>
              <span>{{ activity.currentParticipants }}/{{ activity.maxParticipants }}人</span>
            </div>
            <div class="info-item">
              <el-icon>
                <Star />
              </el-icon>
              <span>{{ activity.points }}积分</span>
            </div>
          </div>

          <div class="activity-meta">
            <span class="creator">创建者：{{ activity.organizerName }}</span>
            <span class="deadline">报名截止：{{ formatDate(activity.registrationDeadline) }}</span>
          </div>
        </div>

        <div class="actions">
          <el-button type="primary" @click="viewActivity(activity.id)">
            查看详情
          </el-button>
          <el-button v-if="canSignup(activity)" type="success" @click="signupActivity(activity)">
            立即报名
          </el-button>
          <el-button v-else-if="isSignedUp(activity)" type="info" disabled>
            已报名
          </el-button>
          <el-button v-else-if="isFullOrExpired(activity)" type="info" disabled>
            {{ getDisabledReason(activity) }}
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="activityList.length > 0">
      <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size"
        :total="pagination.total" :page-sizes="[12, 24, 48]" layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && activityList.length === 0" description="暂无活动" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Clock, User, Star } from '@element-plus/icons-vue'
import { activityAPI, signupAPI } from '@/api'
import { formatDate, formatDateTime } from '@/utils/format'
import { usePagination } from '@/composables/usePagination'

const router = useRouter()
const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination(12)

const activityList = ref([])
const mySignups = ref([])

const searchForm = reactive({
  title: '',
  dateRange: []
})

const typeLabels = {
  ENVIRONMENTAL: '环保公益',
  COMMUNITY: '社区服务',
  EDUCATION: '教育支教',
  ELDERLY_CARE: '敬老助残',
  OTHER: '其他'
}

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

const fetchActivityList = async () => {
  loading.value = true
  try {
    // 构建查询参数，符合后端PageDTO<ActivitySearchDTO>结构
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      params: {
        status: 'PUBLISHED', // 只显示已发布的活动
        title: searchForm.title || undefined,
        startTimeFrom: searchForm.dateRange?.[0] ? `${searchForm.dateRange[0]}T00:00:00` : undefined,
        startTimeTo: searchForm.dateRange?.[1] ? `${searchForm.dateRange[1]}T23:59:59` : undefined
      }
    }

    const response = await activityAPI.getActivityList(params)
    if (response.code === 200) {
      // 过滤掉已截止报名的活动
      const now = new Date()
      activityList.value = response.data.records.filter(activity => {
        const deadline = new Date(activity.registrationDeadline)
        return now < deadline
      })
      // 更新分页信息，使用过滤后的数量
      updatePagination({
        ...response.data,
        total: activityList.value.length
      })
    } else {
      ElMessage.error(response.message || '获取活动列表失败')
      activityList.value = []
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
    ElMessage.error('网络错误，请稍后重试')
    activityList.value = []
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

const fetchMySignups = async () => {
  try {
    const response = await signupAPI.getMySignups({
      pageNum: 1,
      pageSize: 1000,
      params: {}
    })
    if (response.code === 200) {
      mySignups.value = response.data.records?.map(signup => signup.activityId) || []
    }
  } catch (error) {
    console.error('获取我的报名失败:', error)
    mySignups.value = []
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchActivityList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    dateRange: []
  })
  handleSearch()
}

const viewActivity = (id) => {
  router.push(`/activities/${id}`)
}

const canSignup = (activity) => {
  const now = new Date()
  const deadline = new Date(activity.registrationDeadline)
  return !isSignedUp(activity) &&
    activity.currentParticipants < activity.maxParticipants &&
    now < deadline
}

const isSignedUp = (activity) => {
  return mySignups.value.includes(activity.id)
}

const isFullOrExpired = (activity) => {
  const now = new Date()
  const deadline = new Date(activity.registrationDeadline)
  return activity.currentParticipants >= activity.maxParticipants || now >= deadline
}

const getDisabledReason = (activity) => {
  const now = new Date()
  const deadline = new Date(activity.registrationDeadline)

  if (activity.currentParticipants >= activity.maxParticipants) {
    return '名额已满'
  }
  if (now >= deadline) {
    return '报名已截止'
  }
  return '无法报名'
}

const signupActivity = async (activity) => {
  try {
    // 先显示报名原因输入框
    const { value: reason } = await ElMessageBox.prompt(
      `请输入报名 "${activity.title}" 的原因：`,
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

    // 使用SignupCreateDTO结构调用报名接口
    await signupAPI.signupActivity({
      activityId: activity.id,
      reason: reason.trim()
    })
    ElMessage.success('报名成功！请等待审核。')

    // 更新本地状态
    mySignups.value.push(activity.id)
    fetchActivityList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('报名失败:', error)
      ElMessage.error(error.response?.data?.message || '报名失败，请稍后重试')
    }
  }
}

onMounted(() => {
  fetchMySignups()
  fetchActivityList()
})
</script>

<style lang="scss" scoped>
.activities {
  .page-header {
    margin-bottom: 20px;

    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }

  .search-card {
    margin-bottom: 20px;
  }

  .activity-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-bottom: 20px;

    .activity-item {
      :deep(.el-card__body) {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 24px;
      }

      .details {
        flex: 1;
        min-width: 0;
        margin-right: 16px;

        .activity-header {
          margin-bottom: 12px;

          .activity-title {
            margin: 0;
            font-size: 18px;
            font-weight: 600;
            color: #303133;
          }
        }

        .activity-description {
          color: #606266;
          line-height: 1.6;
          margin-bottom: 12px;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .activity-info {
          display: flex;
          flex-wrap: wrap;
          gap: 16px;
          margin-bottom: 12px;

          .info-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            color: #606266;

            .el-icon {
              color: #909399;
            }
          }
        }

        .activity-meta {
          display: flex;
          gap: 24px;
          font-size: 12px;
          color: #909399;
        }
      }

      .actions {
        display: flex;
        flex-direction: column;
        gap: 8px;
        flex-shrink: 0;
        width: 140px;

        .el-button {
          // --- 这是唯一的修改 ---
          // 强制重置 Element Plus 按钮之间默认的 margin
          margin: 0 !important;
          // --- 修改结束 ---

          width: 140px !important;
          min-width: 140px !important;
          max-width: 140px !important;
          padding: 12px 20px !important;
          box-sizing: border-box;
        }
      }
    }
  }

  .pagination {
    text-align: center;
    margin-top: 20px;
  }
}
</style>