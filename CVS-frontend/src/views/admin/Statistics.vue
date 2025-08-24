<template>
  <div class="statistics">
    <div class="page-header">
      <h1>统计分析</h1>
      <el-button-group>
        <el-button :type="timeRange === 'week' ? 'primary' : ''" @click="timeRange = 'week'">
          本周
        </el-button>
        <el-button :type="timeRange === 'month' ? 'primary' : ''" @click="timeRange = 'month'">
          本月
        </el-button>
        <el-button :type="timeRange === 'year' ? 'primary' : ''" @click="timeRange = 'year'">
          本年
        </el-button>
      </el-button-group>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon users">
              <el-icon size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overviewStats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
              <div class="stat-change positive">+{{ overviewStats.newUsers }} 新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon activities">
              <el-icon size="32"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overviewStats.totalActivities }}</div>
              <div class="stat-label">总活动数</div>
              <div class="stat-change positive">+{{ overviewStats.newActivities }} 新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon signups">
              <el-icon size="32"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overviewStats.totalSignups }}</div>
              <div class="stat-label">总报名数</div>
              <div class="stat-change positive">+{{ overviewStats.newSignups }} 新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon hours">
              <el-icon size="32"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overviewStats.totalServiceHours }}</div>
              <div class="stat-label">服务时长(小时)</div>
              <div class="stat-change positive">+{{ overviewStats.newServiceHours }} 新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :xs="24" :lg="12">
        <el-card title="用户增长趋势">
          <div class="chart-container">
            <div class="chart-placeholder">
              <el-icon size="64" color="#ddd"><TrendCharts /></el-icon>
              <p>用户增长趋势图</p>
              <p class="chart-desc">显示用户注册数量的时间趋势</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card title="活动参与分布">
          <div class="chart-container">
            <div class="chart-placeholder">
              <el-icon size="64" color="#ddd"><PieChart /></el-icon>
              <p>活动参与分布图</p>
              <p class="chart-desc">显示不同类型活动的参与情况</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="charts-section">
      <el-col :xs="24" :lg="12">
        <el-card title="服务时长统计">
          <div class="chart-container">
            <div class="chart-placeholder">
              <el-icon size="64" color="#ddd"><DataAnalysis /></el-icon>
              <p>服务时长统计图</p>
              <p class="chart-desc">显示各时间段的服务时长分布</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card title="积分排行榜">
          <div class="ranking-list">
            <div class="ranking-item" v-for="(item, index) in topUsers" :key="item.id">
              <div class="rank-number" :class="{ 'top-three': index < 3 }">
                {{ index + 1 }}
              </div>
              <div class="user-info">
                <div class="user-name">{{ item.name }}</div>
                <div class="user-role">{{ getRoleLabel(item.role) }}</div>
              </div>
              <div class="user-points">{{ item.totalPoints }}分</div>
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
    
    <!-- 详细数据表格 -->
    <el-card title="活动统计详情">
      <el-table :data="activityStats" stripe>
        <el-table-column prop="title" label="活动名称" />
        <el-table-column prop="creatorName" label="创建者" />
        <el-table-column prop="signupCount" label="报名人数" />
        <el-table-column prop="participantCount" label="参与人数" />
        <el-table-column prop="serviceHours" label="服务时长" />
        <el-table-column prop="participationRate" label="参与率">
          <template #default="{ row }">
            {{ formatPercent(row.participationRate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { statisticsAPI } from '@/api'
import { formatPercent } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS, ROLE_LABELS } from '@/utils/constants'
import {
  User,
  Calendar,
  UserFilled,
  Clock,
  TrendCharts,
  PieChart,
  DataAnalysis,
  Trophy
} from '@element-plus/icons-vue'

const timeRange = ref('month')

const overviewStats = reactive({
  totalUsers: 0,
  newUsers: 0,
  totalActivities: 0,
  newActivities: 0,
  totalSignups: 0,
  newSignups: 0,
  totalServiceHours: 0,
  newServiceHours: 0
})

const topUsers = ref([
  { id: 1, name: '张三', role: 'STUDENT', totalPoints: 280 },
  { id: 2, name: '李四', role: 'STUDENT', totalPoints: 260 },
  { id: 3, name: '王五', role: 'TEACHER', totalPoints: 240 },
  { id: 4, name: '赵六', role: 'STUDENT', totalPoints: 220 },
  { id: 5, name: '钱七', role: 'STUDENT', totalPoints: 200 }
])

const activityStats = ref([
  {
    title: '社区环保志愿活动',
    creatorName: '张老师',
    signupCount: 30,
    participantCount: 28,
    serviceHours: 84,
    participationRate: 0.93,
    status: 'PUBLISHED'
  },
  {
    title: '敬老院志愿服务',
    creatorName: '李老师',
    signupCount: 25,
    participantCount: 23,
    serviceHours: 69,
    participationRate: 0.92,
    status: 'PUBLISHED'
  }
])

const getRoleLabel = (role) => {
  return ROLE_LABELS[role] || role
}

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

const fetchOverviewStats = async () => {
  try {
    const response = await statisticsAPI.getOverallStats()
    if (response.code === 200) {
      Object.assign(overviewStats, response.data)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 使用模拟数据
    Object.assign(overviewStats, {
      totalUsers: 1250,
      newUsers: 45,
      totalActivities: 89,
      newActivities: 8,
      totalSignups: 567,
      newSignups: 23,
      totalServiceHours: 1680,
      newServiceHours: 120
    })
  }
}

watch(timeRange, () => {
  fetchOverviewStats()
})

onMounted(() => {
  fetchOverviewStats()
})
</script>

<style lang="scss" scoped>
.statistics {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }
  
  .stats-overview {
    margin-bottom: 24px;
    
    .stat-card {
      margin-bottom: 16px;
      
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          
          &.users {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
          }
          
          &.activities {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
          }
          
          &.signups {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
            color: white;
          }
          
          &.hours {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
            color: white;
          }
        }
        
        .stat-info {
          flex: 1;
          
          .stat-number {
            font-size: 24px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 4px;
          }
          
          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-bottom: 4px;
          }
          
          .stat-change {
            font-size: 12px;
            
            &.positive {
              color: #67c23a;
            }
            
            &.negative {
              color: #f56c6c;
            }
          }
        }
      }
    }
  }
  
  .charts-section {
    margin-bottom: 24px;
    
    .chart-container {
      height: 300px;
      
      .chart-placeholder {
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #909399;
        
        p {
          margin: 16px 0 8px 0;
          font-size: 16px;
        }
        
        .chart-desc {
          margin: 0;
          font-size: 14px;
          color: #c0c4cc;
        }
      }
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
        }
        
        .user-info {
          flex: 1;
          
          .user-name {
            font-size: 16px;
            color: #303133;
            margin-bottom: 4px;
          }
          
          .user-role {
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
}
</style>
