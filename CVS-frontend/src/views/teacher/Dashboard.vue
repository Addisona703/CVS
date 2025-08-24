<template>
  <div class="teacher-dashboard">
    <div class="dashboard-header">
      <h1>教师工作台</h1>
      <p>欢迎回来，{{ userName }}老师！</p>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon activities">
              <el-icon size="32"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ teacherStats.myActivitiesCount }}</div>
              <div class="stat-label">我的活动</div>
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
              <div class="stat-number">{{ teacherStats.totalSignupsCount }}</div>
              <div class="stat-label">参与学生</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon size="32"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ teacherStats.pendingApprovalsCount }}</div>
              <div class="stat-label">待审核报名</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon records">
              <el-icon size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ teacherStats.serviceRecordsCount }}</div>
              <div class="stat-label">待审核志愿证明</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 待办事项 -->
    <el-row :gutter="20" class="todo-section">
      <el-col :xs="24" :lg="12">
        <el-card title="待办事项">
          <div class="todo-list">
            <div class="todo-item" v-for="item in todoList" :key="item.id">
              <div class="todo-content">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-desc">{{ item.description }}</div>
              </div>
              <el-button type="primary" size="small" @click="handleTodo(item)">
                处理
              </el-button>
            </div>
            <div v-if="todoList.length === 0" class="empty-todo">
              <el-icon size="48" color="#ddd"><Check /></el-icon>
              <p>暂无待办事项</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card title="最近活动">
          <div class="recent-activities">
            <div class="activity-item" v-for="activity in recentActivities" :key="activity.id">
              <div class="activity-info">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-meta">
                  <span>{{ formatDate(activity.startTime) }}</span>
                  <el-tag :type="getStatusType(activity.status)" size="small">
                    {{ getStatusLabel(activity.status) }}
                  </el-tag>
                </div>
              </div>
              <el-button type="text" @click="viewActivity(activity.id)">
                查看
              </el-button>
            </div>
            <div v-if="recentActivities.length === 0" class="empty-activities">
              <el-icon size="48" color="#ddd"><Calendar /></el-icon>
              <p>暂无活动</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 快速操作 -->
    <el-card title="快速操作" class="quick-actions">
      <div class="action-buttons">
        <el-button type="primary" @click="$router.push('/teacher/activities/create')">
          <el-icon><Plus /></el-icon>
          创建活动
        </el-button>
        <el-button type="success" @click="$router.push('/teacher/signups')">
          <el-icon><UserFilled /></el-icon>
          审核报名
        </el-button>
        <el-button type="warning" @click="$router.push('/teacher/records')">
          <el-icon><Document /></el-icon>
          记录服务
        </el-button>
        <el-button type="info" @click="$router.push('/teacher/activities')">
          <el-icon><Calendar /></el-icon>
          管理活动
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { formatDate } from '@/utils/format'
import { statisticsAPI } from '@/api'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import {
  Calendar,
  UserFilled,
  Clock,
  Document,
  Check,
  Plus
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const teacherStats = ref({
  myActivitiesCount: 0,
  totalSignupsCount: 0,
  pendingApprovalsCount: 0,
  serviceRecordsCount: 0
})

const todoList = ref([
  {
    id: 1,
    title: '审核志愿活动报名',
    description: '有5个学生报名参加环保活动，需要审核'
  },
  {
    id: 2,
    title: '填写服务记录',
    description: '上周的社区服务活动需要填写服务记录'
  }
])

const recentActivities = ref([
  {
    id: 1,
    title: '社区环保志愿活动',
    startTime: '2024-01-15T09:00:00',
    status: 'PUBLISHED'
  },
  {
    id: 2,
    title: '敬老院志愿服务',
    startTime: '2024-01-20T14:00:00',
    status: 'DRAFT'
  }
])

const userName = computed(() => authStore.userName)

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

const handleTodo = (item) => {
  if (item.id === 1) {
    router.push('/teacher/signups')
  } else if (item.id === 2) {
    router.push('/teacher/records')
  }
}

const viewActivity = (id) => {
  router.push(`/activities/${id}`)
}

const fetchTeacherStats = async () => {
  try {
    const result = await statisticsAPI.getTeacherDashboardStats()
    teacherStats.value = result.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

onMounted(() => {
  fetchTeacherStats()
})
</script>

<style lang="scss" scoped>
.teacher-dashboard {
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
          
          &.activities {
            background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
            color: white;
          }
          
          &.signups {
            background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
            color: white;
          }
          
          &.pending {
            background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
            color: white;
          }
          
          &.records {
            background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
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
  
  .todo-section {
    margin-bottom: 24px;
    
    .todo-list {
      .todo-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .todo-content {
          flex: 1;
          
          .todo-title {
            font-size: 16px;
            color: #303133;
            margin-bottom: 4px;
          }
          
          .todo-desc {
            font-size: 14px;
            color: #909399;
          }
        }
      }
      
      .empty-todo {
        text-align: center;
        padding: 40px 0;
        color: #909399;
        
        p {
          margin: 16px 0 0 0;
        }
      }
    }
    
    .recent-activities {
      .activity-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .activity-info {
          flex: 1;
          
          .activity-title {
            font-size: 16px;
            color: #303133;
            margin-bottom: 8px;
          }
          
          .activity-meta {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 14px;
            color: #909399;
          }
        }
      }
      
      .empty-activities {
        text-align: center;
        padding: 40px 0;
        color: #909399;
        
        p {
          margin: 16px 0 0 0;
        }
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
        min-width: 140px;
      }
    }
  }
}

@media (max-width: 768px) {
  .teacher-dashboard {
    .action-buttons {
      .el-button {
        width: 100%;
        margin-bottom: 8px;
      }
    }
  }
}
</style>
