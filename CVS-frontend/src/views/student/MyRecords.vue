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
        <el-table-column prop="serviceDate" label="服务日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.serviceDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="serviceHours" label="服务时长" width="100">
          <template #default="{ row }">
            {{ row.serviceHours }}小时
          </template>
        </el-table-column>
        <el-table-column prop="points" label="获得积分" width="100" />
        <el-table-column prop="description" label="服务内容" min-width="200" />
        <el-table-column prop="evaluator" label="评价人" width="120" />
        <el-table-column prop="recordTime" label="记录时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.recordTime) }}
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
          <el-descriptions-item label="服务日期">
            {{ formatDate(selectedRecord.serviceDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务时长">
            {{ selectedRecord.serviceHours }}小时
          </el-descriptions-item>
          <el-descriptions-item label="获得积分">
            {{ selectedRecord.points }}分
          </el-descriptions-item>
          <el-descriptions-item label="评价人">
            {{ selectedRecord.evaluator }}
          </el-descriptions-item>
          <el-descriptions-item label="记录时间">
            {{ formatDateTime(selectedRecord.recordTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务内容" :span="2">
            {{ selectedRecord.description }}
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
import { formatDate, formatDateTime } from '@/utils/format'
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
const totalRecords = computed(() => myStats.value?.totalActivities ?? recordList.value.length)
const totalHours = computed(() => myStats.value?.totalHours ?? recordList.value.reduce((sum, record) => sum + (record.durationHours || record.serviceHours || 0), 0))
const totalPoints = computed(() => myStats.value?.totalPoints ?? 0)

const fetchRecordList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }

    // 获取我的服务记录
    const resp = await serviceRecordAPI.getMyRecords(params)
    recordList.value = resp.data?.records || []
    updatePagination(resp.data || { current: 1, size: params.size, total: 0, pages: 0 })

    // 获取我的服务统计（可选）
    try {
      const statsResp = await serviceRecordAPI.getMyStats()
      myStats.value = statsResp.data || null
    } catch {}

  } catch (error) {
    console.error('获取服务记录失败:', error)
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
}
</style>
