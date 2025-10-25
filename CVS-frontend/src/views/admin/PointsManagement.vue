<template>
  <div class="points-management">
    <div class="page-header">
      <h1>积分管理</h1>
    </div>
    
    <!-- 积分排行榜 -->
    <el-card class="ranking-card">
      <template #header>
        <span>积分排行榜</span>
      </template>
      
      <el-table :data="rankingList" :loading="rankingLoading" stripe>
        <el-table-column prop="ranking" label="排名" width="80">
          <template #default="{ row, $index }">
            <div class="rank-number" :class="{ 'top-three': $index < 3 }">
              {{ $index + 1 }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="用户姓名" min-width="120">
          <template #default="{ row }">
            {{ row.name || row.userName || '未知用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="学号/工号" min-width="120">
          <template #default="{ row }">
            {{ row.username || '未设置' }}
          </template>
        </el-table-column>
        <!-- <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column> -->
        <el-table-column prop="totalPoints" label="总积分" width="100" sortable />
      </el-table>
      
      <!-- 排行榜分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="rankingPagination.current"
          v-model:page-size="rankingPagination.size"
          :total="rankingPagination.total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleRankingSizeChange"
          @current-change="handleRankingCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 积分记录管理 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>积分记录管理</span>
      </template>
      
      <!-- 搜索筛选 -->
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="用户姓名">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入用户姓名"
            size="small"
            clearable
          />
        </el-form-item>
        <el-form-item label="学号/工号">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入学号/工号"
            size="small"
            clearable
          />
        </el-form-item>
        <el-form-item label="积分范围">
          <el-input
            v-model="searchForm.minPoints"
            placeholder="最小积分"
            size="small"
            type="number"
            style="width: 100px;"
          />
          <span style="margin: 0 8px;">至</span>
          <el-input
            v-model="searchForm.maxPoints"
            placeholder="最大积分"
            size="small"
            type="number"
            style="width: 100px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
          <el-button size="small" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="pointsRecords" :loading="recordsLoading" stripe>
        <el-table-column prop="name" label="用户姓名" min-width="120">
          <template #default="{ row }">
            {{ row.name || row.userName || '未知用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="activityTitle" label="活动名称" min-width="150">
          <template #default="{ row }">
           <span 
              :title="row.activityTitle || formatPointsSource(row.pointsSource)" 
              style="cursor: help; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block;"
            >
              {{ row.activityTitle || formatPointsSource(row.pointsSource) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="80">
          <template #default="{ row }">
            <span :class="row.points > 0 ? 'points-positive' : 'points-negative'">
              {{ row.points > 0 ? '+' : '' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="pointsSource" label="积分来源" width="100">
          <template #default="{ row }">
            <el-tag :type="getPointsSourceType(row.pointsSource)" size="small">
              {{ formatPointsSource(row.pointsSource) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationMinutes" label="服务时长" width="100">
          <template #default="{ row }">
            <span v-if="row.durationMinutes">
              {{ Math.round(row.durationMinutes / 60 * 10) / 10 }}小时
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="80">
          <template #default="{ row }">
            <el-rate v-if="row.rating" v-model="row.rating" disabled size="small" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="记录时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 记录分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="recordsPagination.current"
          v-model:page-size="recordsPagination.size"
          :total="recordsPagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleRecordsSizeChange"
          @current-change="handleRecordsCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pointsAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { ROLE_LABELS } from '@/utils/constants'
import { usePagination } from '@/composables/usePagination'

// 排行榜相关
const rankingList = ref([])
const rankingLoading = ref(false)
const rankingPagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 积分记录相关
const pointsRecords = ref([])
const recordsLoading = ref(false)
const recordsPagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const searchForm = reactive({
  name: '',
  username: '',
  minPoints: null,
  maxPoints: null
})

const getRoleLabel = (role) => {
  return ROLE_LABELS[role] || role
}

const getRoleType = (role) => {
  const typeMap = {
    'ADMIN': 'danger',
    'TEACHER': 'warning',
    'STUDENT': 'success'
  }
  return typeMap[role] || 'info'
}

// 获取积分排行榜
const fetchRanking = async () => {
  rankingLoading.value = true
  try {
    const response = await pointsAPI.getPointsRankingPage({
      page: rankingPagination.current,
      size: rankingPagination.size
    })
    if (response.code === 200) {
      // 确保数据字段映射正确
      rankingList.value = (response.data.records || []).map(item => ({
        ...item,
        name: item.name || item.userName || '未知用户',
        username: item.username || '未设置',
        totalPoints: item.totalPoints || 0
      }))
      rankingPagination.total = response.data.total || 0
    }
  } catch (error) {
    console.error('获取积分排行榜失败:', error)
    rankingList.value = []
    rankingPagination.total = 0
  } finally {
    rankingLoading.value = false
  }
}

// 获取积分记录
const fetchPointsRecords = async () => {
  recordsLoading.value = true
  try {
    const params = {
      pageNum: recordsPagination.current,
      pageSize: recordsPagination.size,
      params: {
        // 使用新的搜索字段
        name: searchForm.name || undefined,
        username: searchForm.username || undefined,
        minPoints: searchForm.minPoints || undefined,
        maxPoints: searchForm.maxPoints || undefined
      }
    }
    
    const response = await pointsAPI.getAllPointsRecordsWithSearch(params)
    if (response.code === 200) {
      // 确保数据字段映射正确
      pointsRecords.value = (response.data.records || []).map(item => ({
        ...item,
        name: item.name || item.userName || '未知用户',
        username: item.username || '未设置',
        points: item.points || 0,
        activityTitle: item.activityTitle || null,
        pointsSource: item.pointsSource || 'ADJUSTMENT',
        durationMinutes: item.durationMinutes || 0,
        rating: item.rating || null,
        createdAt: item.createdAt || new Date().toISOString()
      }))
      recordsPagination.total = response.data.total || 0
    }
  } catch (error) {
    console.error('获取积分记录失败:', error)
    pointsRecords.value = []
    recordsPagination.total = 0
  } finally {
    recordsLoading.value = false
  }
}

// 排行榜分页处理
const handleRankingSizeChange = (size) => {
  rankingPagination.size = size
  rankingPagination.current = 1
  fetchRanking()
}

const handleRankingCurrentChange = (page) => {
  rankingPagination.current = page
  fetchRanking()
}

// 记录分页处理
const handleRecordsSizeChange = (size) => {
  recordsPagination.size = size
  recordsPagination.current = 1
  fetchPointsRecords()
}

const handleRecordsCurrentChange = (page) => {
  recordsPagination.current = page
  fetchPointsRecords()
}

const handleSearch = () => {
  recordsPagination.current = 1
  fetchPointsRecords()
}

const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    username: '',
    minPoints: null,
    maxPoints: null
  })
  handleSearch()
}

const getPointsSourceType = (source) => source === 'SERVICE' ? 'success' : 'warning'
const formatPointsSource = (source) => (source === 'SERVICE' ? '服务记录' : '手动调整')

onMounted(() => {
  fetchRanking()
  fetchPointsRecords()
})
</script>

<style lang="scss" scoped>
.points-management {
  .page-header {
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }
  
  .search-form {
    margin-bottom: 16px;
  }
  
  .pagination {
    margin-top: 16px;
    text-align: right;
  }
  
  .rank-number {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    margin: 0 auto;
    
    &.top-three {
      background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
      color: white;
    }
  }
  
  .points-positive {
    color: #67c23a;
    font-weight: 600;
  }
  
  .points-negative {
    color: #f56c6c;
    font-weight: 600;
  }
}
</style>
