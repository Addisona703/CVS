<template>
  <div class="service-record-management">
    <div class="page-header">
      <h1>服务记录管理</h1>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.activityTitle" placeholder="请输入活动名称" clearable />
        </el-form-item>
        <el-form-item label="用户姓名">
          <el-input v-model="searchForm.userName" placeholder="请输入用户姓名" clearable />
        </el-form-item>
        <el-form-item label="服务时长">
          <el-input-number v-model="searchForm.minDurationMinutes" placeholder="最小时长(分钟)" :min="0" />
          <span style="margin: 0 8px;">-</span>
          <el-input-number v-model="searchForm.maxDurationMinutes" placeholder="最大时长(分钟)" :min="0" />
        </el-form-item>
        <el-form-item label="积分范围">
          <el-input-number v-model="searchForm.minPointsEarned" placeholder="最小积分" :min="0" />
          <span style="margin: 0 8px;">-</span>
          <el-input-number v-model="searchForm.maxPointsEarned" placeholder="最大积分" :min="0" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 服务记录列表 -->
    <el-card>
      <el-table :data="recordList" :loading="loading" stripe>
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="userName" label="用户姓名" />
        <el-table-column prop="activityTitle" label="活动名称" show-overflow-tooltip />
        <el-table-column prop="durationMinutes" label="服务时长">
          <template #default="{ row }">
            {{ Math.round((row.durationMinutes || 0) / 60 * 10) / 10 }}小时
          </template>
        </el-table-column>
        <el-table-column prop="pointsEarned" label="获得积分" />
        <el-table-column prop="rating" label="评分">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="evaluation" label="评价" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
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
          <el-descriptions-item label="记录ID">{{ selectedRecord.id }}</el-descriptions-item>
          <el-descriptions-item label="用户姓名">{{ selectedRecord.userName }}</el-descriptions-item>
          <el-descriptions-item label="活动名称">{{ selectedRecord.activityTitle }}</el-descriptions-item>
          <el-descriptions-item label="服务时长">
            {{ Math.round((selectedRecord.durationMinutes || 0) / 60 * 10) / 10 }}小时
          </el-descriptions-item>
          <el-descriptions-item label="获得积分">{{ selectedRecord.pointsEarned }}</el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate v-model="selectedRecord.rating" disabled />
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(selectedRecord.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="评价内容" :span="2">
            {{ selectedRecord.evaluation || '暂无评价' }}
          </el-descriptions-item>
          <el-descriptions-item label="服务描述" :span="2">
            {{ selectedRecord.description || '暂无描述' }}
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
import { ElMessage } from 'element-plus'
import { serviceRecordAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { usePagination } from '@/composables/usePagination'

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const recordList = ref([])
const showDetailDialog = ref(false)
const selectedRecord = ref(null)

const searchForm = reactive({
  activityTitle: '',
  userName: '',
  minDurationMinutes: null,
  maxDurationMinutes: null,
  minPointsEarned: null,
  maxPointsEarned: null
})

const fetchRecordList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      params: {
        ...searchForm
      }
    }
    const response = await serviceRecordAPI.getAllRecords(params)
    if (response.code === 200) {
      recordList.value = response.data.records
      updatePagination(response.data)
    }
  } catch (error) {
    console.error('获取服务记录列表失败:', error)
    ElMessage.error('获取服务记录列表失败')
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
    userName: '',
    minDurationMinutes: null,
    maxDurationMinutes: null,
    minPointsEarned: null,
    maxPointsEarned: null
  })
  handleSearch()
}

const handleViewDetail = (row) => {
  selectedRecord.value = row
  showDetailDialog.value = true
}

onMounted(() => {
  fetchRecordList()
})
</script>

<style lang="scss" scoped>
.service-record-management {
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
  
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
  
  .record-detail {
    .el-descriptions {
      margin-top: 20px;
    }
  }
}
</style>