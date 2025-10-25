<template>
  <div class="my-activities">
    <div class="page-header">
      <h1>我的活动</h1>
      <el-button type="primary" @click="$router.push('/teacher/activities/create')">
        <el-icon>
          <Plus />
        </el-icon>
        创建活动
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="活动标题">
          <el-input v-model="searchForm.title" placeholder="请输入活动标题" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审核" value="PENDING_APPROVAL" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="进行中" value="ONGOING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="审核拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 活动列表 -->
    <el-card>
      <el-table :data="activityList" :loading="loading" stripe>
        <el-table-column prop="title" label="活动标题" min-width="150" />
        <el-table-column prop="maxParticipants" label="最大人数" width="90" />
        <el-table-column label="报名人数" width="90">
          <template #default="{ row }">
            <span>{{ row.currentParticipants || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="签到人数" width="90">
          <template #default="{ row }">
            <el-tag :type="getCheckinTagType(row)" size="small">
              {{ row.checkinCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签退人数" width="90">
          <template #default="{ row }">
            <el-tag :type="getCheckoutTagType(row)" size="small">
              {{ row.checkoutCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewActivity(row.id)">
              查看
            </el-button>
            <!-- 草稿、被拒绝、已取消的活动可以编辑 -->
            <el-button 
              v-if="row.status === 'DRAFT' || row.status === 'REJECTED' || row.status === 'CANCELLED'" 
              type="info" 
              size="small" 
              @click="editActivity(row.id)"
            >
              编辑
            </el-button>
            <el-tooltip v-if="row.status === 'REJECTED' && row.rejectReason" :content="'拒绝原因：' + row.rejectReason" placement="top">
              <el-button type="warning" size="small" icon="Warning">
                查看原因
              </el-button>
            </el-tooltip>
            <!-- 草稿、待审核、被拒绝、已取消的活动可以删除 -->
            <el-button 
              v-if="row.status === 'DRAFT' || row.status === 'PENDING_APPROVAL' || row.status === 'REJECTED' || row.status === 'CANCELLED'" 
              type="danger" 
              size="small" 
              @click="deleteActivity(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size"
          :total="pagination.total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper"
          @size-change="onPageSizeChange" @current-change="onPageChange" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { activityAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import { usePagination } from '@/composables/usePagination'

const router = useRouter()
const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const activityList = ref([])

const searchForm = reactive({
  title: '',
  status: ''
})

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

const fetchActivityList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      params: {
        ...searchForm
      }
    }
    const response = await activityAPI.getMyActivities(params)
    if (response.code === 200) {
      activityList.value = response.data.records
      updatePagination(response.data)
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
    // 使用模拟数据
    activityList.value = [
      {
        id: 1,
        title: '社区环保志愿活动',
        maxParticipants: 30,
        currentParticipants: 25,
        status: 'PUBLISHED',
        startTime: '2024-01-20T09:00:00',
        endTime: '2024-01-20T17:00:00',
        createdAt: '2024-01-15T10:00:00'
      },
      {
        id: 2,
        title: '敬老院志愿服务',
        maxParticipants: 20,
        currentParticipants: 0,
        status: 'DRAFT',
        startTime: '2024-01-25T14:00:00',
        endTime: '2024-01-25T18:00:00',
        createdAt: '2024-01-18T15:30:00'
      }
    ]
    updatePagination({
      current: 1,
      size: 10,
      total: 2,
      pages: 1
    })
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  console.log('handleSearch', searchForm)
  fetchActivityList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    status: ''
  })
  handleSearch()
}

const viewActivity = (id) => {
  router.push(`/activities/${id}`)
}

const editActivity = (id) => {
  router.push(`/teacher/activities/create/${id}`)
}

const publishActivity = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要发布活动 "${row.title}" 吗？发布后学生即可报名参加。`,
      '确认发布',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await activityAPI.publishActivity(row.id)
    ElMessage.success('发布成功')
    fetchActivityList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布活动失败:', error)
    }
  }
}

const cancelActivity = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消活动 "${row.title}" 吗？取消后已报名的学生将收到通知。`,
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await activityAPI.cancelActivity(row.id)
    ElMessage.success('取消成功')
    fetchActivityList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消活动失败:', error)
    }
  }
}

const deleteActivity = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除活动 "${row.title}" 吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await activityAPI.deleteActivity(row.id)
    ElMessage.success('删除成功')
    fetchActivityList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除活动失败:', error)
    }
  }
}

// 获取签到标签颜色
const getCheckinTagType = (row) => {
  const checkinCount = row.checkinCount || 0
  const totalCount = row.currentParticipants || 0
  if (totalCount === 0) return 'info'
  if (checkinCount === 0) return 'info'
  if (checkinCount === totalCount) return 'success'
  return 'warning'
}

// 获取签退标签颜色
const getCheckoutTagType = (row) => {
  const checkoutCount = row.checkoutCount || 0
  const checkinCount = row.checkinCount || 0
  if (checkinCount === 0) return 'info'
  if (checkoutCount === 0) return 'info'
  if (checkoutCount === checkinCount) return 'success'
  return 'warning'
}

const onPageSizeChange = (size) => {
  handleSizeChange(size)
  fetchActivityList()
}

const onPageChange = (page) => {
  handleCurrentChange(page)
  fetchActivityList()
}

onMounted(() => {
  fetchActivityList()
})
</script>

<style lang="scss" scoped>
.my-activities {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
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

  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
