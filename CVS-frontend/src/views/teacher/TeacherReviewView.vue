<template>
  <div class="teacher-review-page">
    <el-card class="review-card">
      <header class="card-header">
        <div class="title">
          <el-icon>
            <Memo />
          </el-icon>
          <div>
            <h2>学生签退审核</h2>
            <p>查看学生自评并调整最终评分与评语</p>
          </div>
        </div>
        <el-button type="primary" @click="fetchReviews" :loading="loading" text>
          刷新列表
        </el-button>
      </header>

      <el-form :model="filters" inline class="filter-form">
        <el-form-item label="活动编号">
          <el-input v-model="filters.activityId" placeholder="活动 ID" clearable maxlength="50" style="width: 180px" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="filters.reviewStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已审核" value="REVIEWED" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生信息">
          <el-input v-model="filters.keyword" placeholder="姓名或学号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-space>
            <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>

      <el-table :data="reviewList" v-loading="loading" class="review-table"
        :header-cell-style="{ background: 'var(--bg-color-hover, #f5f7fa)', color: 'var(--text-color-primary, #303133)' }"
        border>
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="activityTitle" label="所属活动" min-width="200" show-overflow-tooltip />
        <el-table-column label="签到时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.checkInTime) }}
          </template>
        </el-table-column>
        <el-table-column label="签退时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.checkOutTime) }}
          </template>
        </el-table-column>
        <el-table-column label="学生自评" min-width="180">
          <template #default="{ row }">
            <div class="rating-block">
              <el-rate :model-value="row.studentRating" disabled :colors="['#b3e19d', '#67c23a', '#5daf34']"
                size="small" />
              <p class="summary">{{ row.studentEvaluation || '—' }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="教师评语" min-width="180">
          <template #default="{ row }">
            <div class="rating-block">
              <el-rate :model-value="row.teacherRating" disabled :colors="['#b3e19d', '#67c23a', '#5daf34']"
                size="small" />
              <p class="summary">{{ row.teacherEvaluation || '—' }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reviewStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.reviewStatus === 'REVIEWED' ? 'success' : 'warning'">
              {{ row.reviewStatus === 'REVIEWED' ? '已审核' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.reviewStatus === 'PENDING'" type="primary" text @click="openReview(row)">
              调整评价
            </el-button>
            <el-tag v-else type="info" size="small">已完成</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination background layout="total, prev, pager, next, jumper" :current-page="pagination.current"
          :page-size="pagination.size" :total="pagination.total" @current-change="handlePageChange" />
      </div>
    </el-card>

    <review-dialog :visible="dialogVisible" :record="currentRecord" :loading="dialogLoading"
      @update:visible="dialogVisible = $event" @submit="submitReview" />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { Memo } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useNotificationNavigation } from '@/composables/useNotificationNavigation'
import { reviewAPI } from '@/api/review'
import ReviewDialog from '@/components/ReviewDialog.vue'

const filters = reactive({
  activityId: '',
  keyword: '',
  reviewStatus: 'PENDING'  // 默认显示待审核
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loading = ref(false)
const reviewList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const currentRecord = ref(null)
const { checkNotificationNavigation } = useNotificationNavigation()

const fetchReviews = async () => {
  loading.value = true
  try {
    const response = await reviewAPI.fetchReviewList({
      pageNum: pagination.current,
      pageSize: pagination.size,
      params: {
        activityId: filters.activityId ? Number(filters.activityId) : undefined,
        keyword: filters.keyword || undefined,
        reviewStatus: filters.reviewStatus || undefined
      }
    })

    if (response.code === 200 && response.data) {
      reviewList.value = response.data.records || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    console.error('获取审核列表失败', error)
    ElMessage.error('获取审核列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchReviews()
}

const handleReset = () => {
  filters.activityId = ''
  filters.keyword = ''
  filters.reviewStatus = ''
  handleSearch()
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchReviews()
}

const openReview = (record) => {
  currentRecord.value = { ...record }
  dialogVisible.value = true
}

const submitReview = async (payload) => {
  if (!currentRecord.value) return
  dialogLoading.value = true
  try {
    await reviewAPI.updateReview(currentRecord.value.signupId, payload)
    ElMessage.success('调整成功')
    dialogVisible.value = false
    fetchReviews()
  } catch (error) {
    console.error('调整评价失败', error)
    ElMessage.error('调整评价失败')
  } finally {
    dialogLoading.value = false
  }
}

const formatDateTime = (value) => {
  if (!value) return '—'
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchReviews()
  checkNotificationNavigation()
})
</script>

<style lang="scss" scoped>
.teacher-review-page {
  min-height: calc(100vh - 120px);
  padding: clamp(24px, 4vw, 40px);
  background: linear-gradient(150deg, rgba(231, 241, 255, 0.5), rgba(255, 255, 255, 0.85));
}

.review-card {
  border-radius: 26px;
  border: none;
  box-shadow: 0 36px 120px -60px rgba(31, 107, 255, 0.3);

  :deep(.el-card__body) {
    padding: clamp(22px, 3.5vw, 34px);
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 18px;
    margin-bottom: 20px;

    .title {
      display: flex;
      gap: 14px;
      align-items: flex-start;

      h2 {
        margin: 0;
        font-size: 24px;
        font-weight: 700;
        color: #1f2a44;
      }

      p {
        margin: 6px 0 0;
        color: rgba(31, 42, 68, 0.6);
        font-size: 14px;
      }

      :deep(svg) {
        font-size: 32px;
        color: #67c23a;
        padding: 12px;
        border-radius: 16px;
        background: linear-gradient(135deg, rgba(31, 107, 255, 0.18), rgba(31, 107, 255, 0.06));
        box-shadow: inset 0 0 0 1px rgba(31, 107, 255, 0.12);
      }
    }
  }

  .filter-form {
    background: rgba(31, 107, 255, 0.06);
    border-radius: 18px;
    padding: 16px clamp(18px, 3vw, 28px);
    margin-bottom: 20px;
  }

  .review-table {
    border-radius: 18px;
    overflow: hidden;

    .rating-block {
      display: flex;
      flex-direction: column;
      gap: 6px;

      .summary {
        margin: 0;
        font-size: 13px;
        color: rgba(31, 42, 68, 0.7);
        line-height: 1.4;
      }
    }
  }

  .table-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 18px;
  }
}

@media (max-width: 900px) {
  .review-card {
    .filter-form {
      display: grid;
      gap: 12px;
    }

    .card-header {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}
</style>
