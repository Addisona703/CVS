<template>
  <div class="service-records">
    <div class="page-header">
      <h1>服务记录</h1>
      <div class="header-actions">
        <el-button type="primary" plain @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.activityTitle" placeholder="请输入活动名称" clearable />
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input v-model="searchForm.userName" placeholder="请输入学生姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 记录列表 -->
    <el-card>
      <el-table :data="recordList" :loading="loading" stripe>
        <el-table-column prop="activityTitle" label="活动" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userName" label="学生" width="140" />
        <el-table-column label="时长" width="120">
          <template #default="{ row }">
            {{ Math.round((row.durationMinutes || 0) / 60 * 10) / 10 }} 小时
          </template>
        </el-table-column>
        <el-table-column prop="pointsEarned" label="积分" width="80" />
        <el-table-column prop="rating" label="评分" width="80">
          <template #default="{ row }">
            <span v-if="row.rating">{{ row.rating }}分</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="evaluation" label="评价" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">
              查看
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
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="服务记录详情"
      width="600px"
    >
      <div v-if="selectedRecord" class="record-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="活动名称">
            {{ selectedRecord.activityTitle }}
          </el-descriptions-item>
          <el-descriptions-item label="学生姓名">
            {{ selectedRecord.userName }}
          </el-descriptions-item>
          <el-descriptions-item label="服务时长">
            {{ Math.round((selectedRecord.durationMinutes || 0) / 60 * 10) / 10 }}小时
          </el-descriptions-item>
          <el-descriptions-item label="获得积分">
            {{ selectedRecord.pointsEarned || 0 }}分
          </el-descriptions-item>
          <el-descriptions-item label="评分">
            <span v-if="selectedRecord.rating">{{ selectedRecord.rating }}分</span>
            <span v-else class="text-muted">未评分</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(selectedRecord.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务内容" :span="2">
            {{ selectedRecord.description || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="评价" :span="2" v-if="selectedRecord.evaluation">
            {{ selectedRecord.evaluation }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { serviceRecordAPI } from '@/api'
import { formatDateTime, formatTime } from '@/utils/format'
import { usePagination } from '@/composables/usePagination'

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

// 定义数据
const recordList = ref([])
const showDetailDialog = ref(false)
const selectedRecord = ref(null)

const searchForm = reactive({
  activityTitle: '',
  userName: ''
})

const fetchRecordList = async () => {
  loading.value = true
  try {
    // 构建分页参数
    const pageRequest = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      params: {
        activityTitle: searchForm.activityTitle || undefined,
        userName: searchForm.userName || undefined
      }
    }

    // 调用新的API接口
    const resp = await serviceRecordAPI.getManagedRecords(pageRequest)
    if (resp.code === 200 && resp.data) {
      recordList.value = resp.data.records || []
      updatePagination({
        current: resp.data.current || 1,
        size: resp.data.size || pagination.size,
        total: resp.data.total || 0,
        pages: resp.data.pages || 0
      })
    } else {
      recordList.value = []
      updatePagination({ current: 1, size: pagination.size, total: 0, pages: 0 })
    }
  } catch (error) {
    console.error('获取服务记录失败:', error)
    recordList.value = []
    updatePagination({ current: 1, size: pagination.size, total: 0, pages: 0 })
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchRecordList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    activityTitle: '',
    userName: ''
  })
  handleSearch()
}

const handleRefresh = () => {
  fetchRecordList()
}

const viewDetail = (record) => {
  selectedRecord.value = record
  showDetailDialog.value = true
}

onMounted(() => {
  fetchRecordList()
})
</script>

<style lang="scss" scoped>
.service-records {
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
  
  .record-detail {
    :deep(.el-descriptions__body) {
      background: #fafafa;
    }
  }
  
  .text-muted {
    color: #909399;
  }
}
</style>