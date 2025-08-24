<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h1>管理员仪表板</h1>
      <p>欢迎回来，{{ userName }}！</p>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon users">
              <el-icon size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
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
              <div class="stat-number">{{ stats.totalActivities }}</div>
              <div class="stat-label">总活动数</div>
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
              <div class="stat-number">{{ stats.totalSignups }}</div>
              <div class="stat-label">总报名数</div>
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
              <div class="stat-number">{{ stats.totalServiceHours }}</div>
              <div class="stat-label">服务时长(小时)</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <!-- <el-row :gutter="20" class="charts-section">
      <el-col :xs="24" :lg="12">
        <el-card title="用户增长趋势">
          <div class="chart-placeholder">
            <el-icon size="64" color="#ddd"><TrendCharts /></el-icon>
            <p>用户增长图表</p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card title="活动参与统计">
          <div class="chart-placeholder">
            <el-icon size="64" color="#ddd"><PieChart /></el-icon>
            <p>活动参与饼图</p>
          </div>
        </el-card>
      </el-col>
    </el-row> -->
    
    <!-- 快速操作 -->
    <el-row :gutter="20" class="quick-actions">
      <el-col :xs="24" :lg="12">
        <el-card title="快速操作">
          <div class="action-buttons">
            <el-button type="primary" @click="$router.push('/admin/users')">
              <el-icon><User /></el-icon>
              用户管理
            </el-button>
            <el-button type="success" @click="$router.push('/admin/activities')">
              <el-icon><Calendar /></el-icon>
              活动管理
            </el-button>
            <el-button type="warning" @click="$router.push('/admin/statistics')">
              <el-icon><DataAnalysis /></el-icon>
              统计分析
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card title="系统状态">
          <div class="system-status">
            <div class="status-item">
              <span class="status-label">系统运行时间:</span>
              <span class="status-value">7天 12小时</span>
            </div>
            <div class="status-item">
              <span class="status-label">在线用户:</span>
              <span class="status-value">{{ stats.onlineUsers || 0 }}</span>
            </div>
            <div class="status-item">
              <span class="status-label">系统版本:</span>
              <span class="status-value">v1.0.0</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { statisticsAPI } from '@/api'
import {
  User,
  Calendar,
  UserFilled,
  Clock,
  TrendCharts,
  PieChart,
  DataAnalysis
} from '@element-plus/icons-vue'

const authStore = useAuthStore()

const stats = ref({
  totalUsers: 0,
  totalActivities: 0,
  totalSignups: 0,
  totalServiceHours: 0,
  onlineUsers: 0
})

const userName = computed(() => authStore.userName)

const fetchStats = async () => {
  try {
    const response = await statisticsAPI.getOverallStats()
    if (response.code === 200) {
      Object.assign(stats.value, response.data)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style lang="scss" scoped>
.admin-dashboard {
  .dashboard-header {
    margin-bottom: 24px;
    
    h1 {
      margin: 0 0 8px 0;
      font-size: 28px;
      color: #303133;
    }
    
    p {
      margin: 0;
      color: #606266;
      font-size: 16px;
    }
  }
  
  .stats-cards {
    margin-bottom: 24px;
    
    .stat-card {
      margin-bottom: 16px;
      
      :deep(.el-card__body) {
        padding: 20px;
      }
      
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
          }
        }
      }
    }
  }
  
  .charts-section {
    margin-bottom: 24px;
    
    .chart-placeholder {
      height: 300px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #909399;
      
      p {
        margin: 16px 0 0 0;
        font-size: 16px;
      }
    }
  }
  
  .quick-actions {
    .action-buttons {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      
      .el-button {
        flex: 1;
        min-width: 120px;
      }
    }
    
    .system-status {
      .status-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .status-label {
          color: #606266;
          font-size: 14px;
        }
        
        .status-value {
          color: #303133;
          font-weight: 500;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .admin-dashboard {
    .action-buttons {
      .el-button {
        width: 100%;
        margin-bottom: 8px;
      }
    }
  }
}
</style>
