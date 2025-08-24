<template>
  <div class="activity-management">
    <div class="page-header">
      <h1>活动管理</h1>
      <el-button type="primary" @click="$router.push('/teacher/activities/create')">
        <el-icon><Plus /></el-icon>
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
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建者">
          <el-input v-model="searchForm.creatorName" placeholder="请输入创建者姓名" clearable />
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
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="活动标题" min-width="200" />
        <el-table-column prop="creatorName" label="创建者" width="120" />
        <el-table-column prop="maxParticipants" label="最大人数" width="100" />
        <el-table-column prop="currentParticipants" label="当前人数" width="100" />
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
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewActivity(row.id)">
              查看
            </el-button>
            <el-button 
              v-if="row.status === 'DRAFT'" 
              type="success" 
              size="small" 
              @click="publishActivity(row)"
            >
              发布
            </el-button>
            <el-button 
              v-if="row.status === 'PUBLISHED'" 
              type="warning" 
              size="small" 
              @click="cancelActivity(row)"
            >
              取消
            </el-button>
            <el-button type="danger" size="small" @click="deleteActivity(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
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
  status: '',
  creatorName: ''
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
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const response = await activityAPI.getActivityList(params)
    if (response.code === 200) {
      activityList.value = response.data.records
      updatePagination(response.data)
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchActivityList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    status: '',
    creatorName: ''
  })
  handleSearch()
}

const viewActivity = (id) => {
  router.push(`/activities/${id}`)
}

const publishActivity = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要发布活动 "${row.title}" 吗？`,
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
      `确定要取消活动 "${row.title}" 吗？`,
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

onMounted(() => {
  fetchActivityList()
})
</script>

<style lang="scss" scoped>
.activity-management {
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
