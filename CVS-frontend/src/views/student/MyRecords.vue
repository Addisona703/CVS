<template>
  <div class="my-records">
    <div class="page-header">
      <h1>我的记录</h1>
    </div>
    
    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ totalRecords }}</div>
            <div class="stat-label">总记录数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ totalHours }}</div>
            <div class="stat-label">总服务时长(小时)</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ totalPoints }}</div>
            <div class="stat-label">总获得积分</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.activityTitle" placeholder="请输入活动名称" clearable />
        </el-form-item>
        <el-form-item label="记录时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 记录列表 -->
    <el-card>
      <el-table :data="recordList" :loading="loading" stripe>
        <el-table-column prop="activityTitle" label="活动名称" min-width="200" />
        <el-table-column prop="createdAt" label="记录日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="服务时长" width="100">
          <template #default="{ row }">
            {{ Math.round((row.durationMinutes || 0) / 60 * 10) / 10 }}小时
          </template>
        </el-table-column>
        <el-table-column prop="pointsEarned" label="获得积分" width="100" />
        <el-table-column prop="description" label="服务内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="rating" label="评分" width="80">
          <template #default="{ row }">
            <span v-if="row.rating">{{ row.rating }}分</span>
            <span v-else class="text-muted">未评分</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="记录时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">
              查看详情
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
          <el-descriptions-item label="记录日期">
            {{ formatDate(selectedRecord.createdAt) }}
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
          <el-descriptions-item label="记录时间">
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
import { ref, reactive, onMounted, computed } from 'vue'
import { serviceRecordAPI } from '@/api'
import { formatDate, formatDateTime, formatTime } from '@/utils/format'
import { usePagination } from '@/composables/usePagination'

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const recordList = ref([])
const showDetailDialog = ref(false)
const selectedRecord = ref(null)
const myStats = ref(null)

const searchForm = reactive({
  activityTitle: '',
  dateRange: []
})

// 计算统计数据
const totalRecords = computed(() => myStats.value?.totalRecords ?? 0)
const totalHours = computed(() => {
  const hours = myStats.value?.totalServiceHours ?? 0
  return Math.round(hours * 10) / 10
})
const totalPoints = computed(() => myStats.value?.totalPointsEarned ?? 0)

const fetchRecordList = async () => {
  loading.value = true
  try {
    // 构建分页参数
    const pageRequest = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      params: {
        activityTitle: searchForm.activityTitle || undefined,
        // 日期范围处理（如需要）
        startDate: searchForm.dateRange?.[0] || undefined,
        endDate: searchForm.dateRange?.[1] || undefined
      }
    }

    // 调用新的API接口
    const resp = await serviceRecordAPI.getMyRecords(pageRequest)
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

    // 获取统计数据
    try {
      const statsResp = await serviceRecordAPI.getMyStats()
      if (statsResp.code === 200) {
        myStats.value = statsResp.data
      }
    } catch (error) {
      console.warn('获取统计数据失败:', error)
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
    dateRange: []
  })
  handleSearch()
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
.my-records {
  .page-header {
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }
  
  .stats-overview {
    margin-bottom: 20px;
    
    .stat-card {
      text-align: center;
      
      .stat-content {
        .stat-number {
          font-size: 32px;
          font-weight: 600;
          color: #409eff;
          margin-bottom: 8px;
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
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
