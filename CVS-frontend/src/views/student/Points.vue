<template>
  <div class="points">
    <div class="page-header">
      <h1>积分中心</h1>
    </div>
    
    <!-- 积分概览 -->
    <el-row :gutter="20" class="points-overview">
      <el-col :xs="24" :md="8">
        <el-card class="points-card">
          <div class="points-content">
            <div class="points-icon">
              <el-icon size="48" color="#e6a23c"><Star /></el-icon>
            </div>
            <div class="points-info">
              <div class="points-number">{{ userPoints.totalPoints || 0 }}</div>
              <div class="points-label">总积分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="8">
        <el-card class="rank-card">
          <div class="rank-content">
            <div class="rank-icon">
              <el-icon size="48" color="#f56c6c"><Trophy /></el-icon>
            </div>
            <div class="rank-info">
              <div class="rank-number">{{ userPoints.ranking || '-' }}</div>
              <div class="rank-label">当前排名</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="8">
        <el-card class="level-card">
          <div class="level-content">
            <div class="level-icon">
              <el-icon size="48" color="#67c23a"><Medal /></el-icon>
            </div>
            <div class="level-info">
              <div class="level-name">{{ userPoints.level || '新手志愿者' }}</div>
              <div class="level-label">当前等级</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 等级进度 -->
    <el-card class="level-progress-card">
      <template #header>
        <span>等级进度</span>
      </template>
      <div class="level-progress">
        <div class="current-level">
          <span class="level-name">{{ userPoints.level || '新手志愿者' }}</span>
          <span class="level-points">{{ userPoints.totalPoints || 0 }}分</span>
        </div>
        <div class="progress-bar">
          <el-progress 
            :percentage="levelProgress" 
            :stroke-width="20"
            :show-text="false"
          />
        </div>
        <div class="next-level">
          <span class="level-name">{{ nextLevel.name || '志愿达人' }}</span>
          <span class="level-points">{{ nextLevel.requiredPoints || 1000 }}分</span>
        </div>
      </div>
      <div class="progress-text" v-if="(nextLevel.requiredPoints || 1000) > (userPoints.totalPoints || 0)">
        还需 {{ (nextLevel.requiredPoints || 1000) - (userPoints.totalPoints || 0) }} 分升级到 {{ nextLevel.name || '志愿达人' }}
      </div>
      <div class="progress-text" v-else>
        已达到最高等级
      </div>
    </el-card>
    
    <!-- 积分记录和排行榜 -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="14">
        <el-card>
          <template #header>
            <span>积分记录</span>
          </template>
          
          <!-- 搜索筛选 -->
          <el-form :model="searchForm" inline class="search-form">
            <el-form-item label="记录时间">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                size="small"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
              <el-button size="small" @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="pointsRecords" :loading="loading" stripe>
            <el-table-column prop="activityTitle" label="活动名称" min-width="150">
              <template #default="{ row }">
                {{ row.activityTitle || '志愿服务' }}
              </template>
            </el-table-column>
            <el-table-column prop="points" label="积分" width="80">
              <template #default="{ row }">
                <span class="points-change positive">+{{ row.points || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="获得原因" min-width="120">
              <template #default="{ row }">
                {{ row.reason || '完成志愿服务' }}
              </template>
            </el-table-column>
            <el-table-column prop="recordTime" label="获得时间" width="180">
              <template #default="{ row }">
                {{ row.recordTime ? formatDateTime(row.recordTime) : '-' }}
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.current"
              v-model:page-size="pagination.size"
              :total="pagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              small
            />
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="10">
        <el-card>
          <template #header>
            <span>积分排行榜</span>
          </template>
          
          <div class="ranking-list">
            <div class="ranking-item" v-for="(item, index) in pointsRanking" :key="item.userId">
              <div class="rank-number" :class="{ 'top-three': index < 3, 'current-user': item.isCurrentUser }">
                {{ index + 1 }}
              </div>
              <div class="user-info">
                <div class="user-name" :class="{ 'current-user': item.isCurrentUser }">
                  {{ item.name || '未知用户' }}
                  <el-tag v-if="item.isCurrentUser" type="primary" size="small">我</el-tag>
                </div>
                <div class="user-level">{{ item.level || '新手志愿者' }}</div>
              </div>
              <div class="user-points">{{ item.totalPoints || 0 }}分</div>
              <div class="rank-badge" v-if="index < 3">
                <el-icon v-if="index === 0" color="#ffd700"><Trophy /></el-icon>
                <el-icon v-else-if="index === 1" color="#c0c0c0"><Trophy /></el-icon>
                <el-icon v-else color="#cd7f32"><Trophy /></el-icon>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Star, Trophy, Medal } from '@element-plus/icons-vue'
import { pointsAPI, serviceRecordAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { usePagination } from '@/composables/usePagination'

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const userPoints = ref({
  totalPoints: 0,
  ranking: 0,
  level: ''
})

const pointsRecords = ref([])
const pointsRanking = ref([])

const searchForm = reactive({
  dateRange: []
})

// 等级配置
const levels = [
  { name: '新手志愿者', requiredPoints: 0, color: '#909399' },
  { name: '初级志愿者', requiredPoints: 50, color: '#67c23a' },
  { name: '中级志愿者', requiredPoints: 150, color: '#e6a23c' },
  { name: '高级志愿者', requiredPoints: 300, color: '#f56c6c' },
  { name: '资深志愿者', requiredPoints: 500, color: '#722ed1' },
  { name: '志愿达人', requiredPoints: 1000, color: '#eb2f96' }
]

// 根据积分获取等级
const getLevelByPoints = (points) => {
  const levelInfo = levels.slice().reverse().find(level => points >= level.requiredPoints)
  return levelInfo ? levelInfo.name : '新手志愿者'
}

const nextLevel = computed(() => {
  const currentPoints = userPoints.value.totalPoints
  const nextLevelInfo = levels.find(level => level.requiredPoints > currentPoints)
  return nextLevelInfo || levels[levels.length - 1]
})

const levelProgress = computed(() => {
  const currentPoints = userPoints.value.totalPoints
  const currentLevelInfo = levels.slice().reverse().find(level => level.requiredPoints <= currentPoints)
  const nextLevelInfo = nextLevel.value
  
  if (!currentLevelInfo || currentLevelInfo === nextLevelInfo) {
    return 100
  }
  
  const progressPoints = currentPoints - currentLevelInfo.requiredPoints
  const totalPoints = nextLevelInfo.requiredPoints - currentLevelInfo.requiredPoints
  
  return Math.round((progressPoints / totalPoints) * 100)
})

const fetchUserPoints = async () => {
  try {
    // 调用积分统计接口
    const response = await pointsAPI.getCurrentUserPointsStats()
    if (response.code === 200) {
      const stats = response.data
      userPoints.value = {
        totalPoints: stats.totalPoints || 0,
        ranking: stats.currentRanking || 0,
        level: getLevelByPoints(stats.totalPoints || 0)
      }
    }
  } catch (error) {
    console.error('获取用户积分失败:', error)
    // 使用模拟数据作为备用
    userPoints.value = {
      totalPoints: 180,
      ranking: 5,
      level: '中级志愿者'
    }
  }
}

const fetchPointsRecords = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      params: {
        // 可以添加搜索条件
        startDate: searchForm.dateRange?.[0],
        endDate: searchForm.dateRange?.[1]
      }
    }
    
    // 调用获取我的服务记录API来获取积分记录
    const response = await serviceRecordAPI.getMyRecords(params)
    if (response.code === 200) {
      pointsRecords.value = (response.data.records || []).map(record => ({
        ...record,
        activityTitle: record.activityTitle || '志愿服务',
        points: record.pointsEarned || 0,
        reason: '完成志愿服务',
        recordTime: record.createdAt
      }))
      
      updatePagination(response.data)
    }
  } catch (error) {
    console.error('获取积分记录失败:', error)
    // 使用模拟数据作为备用
    pointsRecords.value = [
      {
        id: 1,
        activityTitle: '社区环保志愿活动',
        points: 15,
        reason: '完成志愿服务',
        recordTime: '2024-01-20T18:00:00'
      },
      {
        id: 2,
        activityTitle: '敬老院志愿服务',
        points: 20,
        reason: '完成志愿服务',
        recordTime: '2024-01-15T19:30:00'
      },
      {
        id: 3,
        activityTitle: '图书馆志愿服务',
        points: 10,
        reason: '完成志愿服务',
        recordTime: '2024-01-10T17:00:00'
      }
    ]
    
    updatePagination({
      current: 1,
      size: 10,
      total: 3,
      pages: 1
    })
  } finally {
    loading.value = false
  }
}

const fetchPointsRanking = async () => {
  try {
    // 调用分页排行榜接口，只获取前10名
    const response = await pointsAPI.getPointsRankingPage({ page: 1, size: 10 })
    if (response.code === 200) {
      pointsRanking.value = (response.data.records || []).map(item => ({
        ...item,
        level: getLevelByPoints(item.totalPoints),
        isCurrentUser: item.isCurrentUser || false
      }))
    }
  } catch (error) {
    console.error('获取积分排行榜失败:', error)
    // 使用模拟数据作为备用
    pointsRanking.value = [
      { userId: 1, userName: '张三', totalPoints: 280, level: '高级志愿者', isCurrentUser: false },
      { userId: 2, userName: '李四', totalPoints: 260, level: '高级志愿者', isCurrentUser: false },
      { userId: 3, userName: '王五', totalPoints: 240, level: '中级志愿者', isCurrentUser: false },
      { userId: 4, userName: '赵六', totalPoints: 220, level: '中级志愿者', isCurrentUser: false },
      { userId: 5, userName: '当前用户', totalPoints: 180, level: '中级志愿者', isCurrentUser: true },
      { userId: 6, userName: '钱七', totalPoints: 160, level: '中级志愿者', isCurrentUser: false }
    ]
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchPointsRecords()
}

const handleReset = () => {
  Object.assign(searchForm, {
    dateRange: []
  })
  handleSearch()
}

onMounted(() => {
  fetchUserPoints()
  fetchPointsRecords()
  fetchPointsRanking()
})
</script>

<style lang="scss" scoped>
.points {
  .page-header {
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }
  
  .points-overview {
    margin-bottom: 20px;
    
    .points-card,
    .rank-card,
    .level-card {
      .points-content,
      .rank-content,
      .level-content {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .points-info,
        .rank-info,
        .level-info {
          .points-number,
          .rank-number {
            font-size: 32px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 4px;
          }
          
          .level-name {
            font-size: 18px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 4px;
          }
          
          .points-label,
          .rank-label,
          .level-label {
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }
  }
  
  .level-progress-card {
    margin-bottom: 20px;
    
    .level-progress {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 12px;
      
      .current-level,
      .next-level {
        display: flex;
        flex-direction: column;
        align-items: center;
        min-width: 80px;
        
        .level-name {
          font-size: 14px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }
        
        .level-points {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .progress-bar {
        flex: 1;
      }
    }
    
    .progress-text {
      text-align: center;
      font-size: 14px;
      color: #606266;
    }
  }
  
  .search-form {
    margin-bottom: 16px;
  }
  
  .points-change {
    font-weight: 600;
    
    &.positive {
      color: #67c23a;
    }
    
    &.negative {
      color: #f56c6c;
    }
  }
  
  .pagination {
    margin-top: 16px;
    text-align: right;
  }
  
  .ranking-list {
    .ranking-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
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
        margin-right: 16px;
        
        &.top-three {
          background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
          color: white;
        }
        
        &.current-user {
          background: #409eff;
          color: white;
        }
      }
      
      .user-info {
        flex: 1;
        
        .user-name {
          font-size: 16px;
          color: #303133;
          margin-bottom: 4px;
          display: flex;
          align-items: center;
          gap: 8px;
          
          &.current-user {
            font-weight: 600;
            color: #409eff;
          }
        }
        
        .user-level {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .user-points {
        font-size: 16px;
        font-weight: 600;
        color: #e6a23c;
        margin-right: 16px;
      }
      
      .rank-badge {
        margin-left: 8px;
      }
    }
  }
}
</style>
