<template>
  <div class="student-dashboard">
    <div class="dashboard-header">
      <h1>个人中心</h1>
      <p>欢迎回来，{{ userName }}同学！</p>
    </div>
    
    <!-- 个人统计 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon signups">
              <el-icon size="32"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ studentStats.mySignups }}</div>
              <div class="stat-label">我的报名</div>
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
              <div class="stat-number">{{ studentStats.serviceHours }}</div>
              <div class="stat-label">服务时长(小时)</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon points">
              <el-icon size="32"><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ studentStats.totalPoints }}</div>
              <div class="stat-label">积分总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon certificates">
              <el-icon size="32"><Medal /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ studentStats.certificates }}</div>
              <div class="stat-label">志愿证明</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 推荐活动和我的报名 -->
    <el-row :gutter="20" class="content-section">
      <el-col :xs="24" :lg="12">
        <el-card title="推荐活动">
          <div class="activity-list">
            <div class="activity-item" v-for="activity in recommendedActivities" :key="activity.id">
              <div class="activity-info">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-meta">
                  <span>{{ formatDate(activity.startTime) }}</span>
                  <span>{{ activity.maxParticipants }}人</span>
                </div>
                <div class="activity-desc">{{ activity.description }}</div>
              </div>
              <el-button type="primary" size="small" @click="signupActivity(activity.id)">
                报名
              </el-button>
            </div>
            <div v-if="recommendedActivities.length === 0" class="empty-list">
              <el-icon size="48" color="#ddd"><Calendar /></el-icon>
              <p>暂无推荐活动</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card title="我的报名">
          <div class="signup-list">
            <div class="signup-item" v-for="signup in mySignups" :key="signup.id">
              <div class="signup-info">
                <div class="signup-title">{{ signup.activityTitle }}</div>
                <div class="signup-meta">
                  <span>{{ formatDate(signup.signupTime) }}</span>
                  <el-tag :type="getStatusType(signup.status)" size="small">
                    {{ getStatusLabel(signup.status) }}
                  </el-tag>
                </div>
              </div>
              <el-button type="text" @click="viewSignup(signup.id)">
                查看
              </el-button>
            </div>
            <div v-if="mySignups.length === 0" class="empty-list">
              <el-icon size="48" color="#ddd"><UserFilled /></el-icon>
              <p>暂无报名记录</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 积分排行榜 -->
    <el-card title="积分排行榜" class="ranking-section">
      <div class="ranking-list">
        <div class="ranking-item" v-for="(item, index) in pointsRanking" :key="item.userId">
          <div class="rank-number" :class="{ 'top-three': index < 3 }">
            {{ index + 1 }}
          </div>
          <div class="user-info">
            <div class="user-name">{{ item.userName }}</div>
            <div class="user-points">{{ item.totalPoints }}分</div>
          </div>
          <div class="rank-badge" v-if="index < 3">
            <el-icon v-if="index === 0" color="#ffd700"><Trophy /></el-icon>
            <el-icon v-else-if="index === 1" color="#c0c0c0"><Trophy /></el-icon>
            <el-icon v-else color="#cd7f32"><Trophy /></el-icon>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 快速操作 -->
    <el-card title="快速操作" class="quick-actions">
      <div class="action-buttons">
        <el-button type="primary" @click="$router.push('/student/activities')">
          <el-icon><Calendar /></el-icon>
          浏览活动
        </el-button>
        <el-button type="success" @click="$router.push('/student/signups')">
          <el-icon><UserFilled /></el-icon>
          我的报名
        </el-button>
        <el-button type="warning" @click="$router.push('/student/points')">
          <el-icon><Star /></el-icon>
          积分中心
        </el-button>
        <el-button type="info" @click="$router.push('/student/certificates')">
          <el-icon><Medal /></el-icon>
          申请证明
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
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import { ElMessage } from 'element-plus'
import {
  UserFilled,
  Clock,
  Star,
  Medal,
  Calendar,
  Trophy
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const studentStats = ref({
  mySignups: 0,
  serviceHours: 0,
  totalPoints: 0,
  certificates: 0
})

const recommendedActivities = ref([
  {
    id: 1,
    title: '社区环保志愿活动',
    description: '参与社区环保活动，清理公园垃圾',
    startTime: '2024-01-20T09:00:00',
    maxParticipants: 30
  },
  {
    id: 2,
    title: '敬老院志愿服务',
    description: '为敬老院老人提供陪伴和帮助',
    startTime: '2024-01-25T14:00:00',
    maxParticipants: 20
  }
])

const mySignups = ref([
  {
    id: 1,
    activityTitle: '图书馆志愿服务',
    signupTime: '2024-01-10T10:00:00',
    status: 'APPROVED'
  },
  {
    id: 2,
    activityTitle: '校园清洁活动',
    signupTime: '2024-01-12T15:00:00',
    status: 'PENDING'
  }
])

const pointsRanking = ref([
  { userId: 1, userName: '张三', totalPoints: 280 },
  { userId: 2, userName: '李四', totalPoints: 260 },
  { userId: 3, userName: '王五', totalPoints: 240 },
  { userId: 4, userName: '赵六', totalPoints: 220 },
  { userId: 5, userName: '钱七', totalPoints: 200 }
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

const signupActivity = (activityId) => {
  ElMessage.success('报名成功！')
  // 这里应该调用报名API
}

const viewSignup = (signupId) => {
  router.push(`/student/signups`)
}

const fetchStudentStats = async () => {
  // 这里应该调用API获取学生统计数据
  // 暂时使用模拟数据
  studentStats.value = {
    mySignups: 5,
    serviceHours: 24,
    totalPoints: 180,
    certificates: 2
  }
}

onMounted(() => {
  fetchStudentStats()
})
</script>

<style lang="scss" scoped>
.student-dashboard {
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
          
          &.signups {
            background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
            color: white;
          }
          
          &.hours {
            background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
            color: white;
          }
          
          &.points {
            background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
            color: white;
          }
          
          &.certificates {
            background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
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
  
  .content-section {
    margin-bottom: 24px;
    
    .activity-list,
    .signup-list {
      .activity-item,
      .signup-item {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        padding: 16px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .activity-info,
        .signup-info {
          flex: 1;
          margin-right: 16px;
          
          .activity-title,
          .signup-title {
            font-size: 16px;
            color: #303133;
            margin-bottom: 8px;
          }
          
          .activity-meta,
          .signup-meta {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 14px;
            color: #909399;
            margin-bottom: 8px;
          }
          
          .activity-desc {
            font-size: 14px;
            color: #606266;
            line-height: 1.4;
          }
        }
      }
      
      .empty-list {
        text-align: center;
        padding: 40px 0;
        color: #909399;
        
        p {
          margin: 16px 0 0 0;
        }
      }
    }
  }
  
  .ranking-section {
    margin-bottom: 24px;
    
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
          
          .user-points {
            font-size: 14px;
            color: #909399;
          }
        }
        
        .rank-badge {
          margin-left: 16px;
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
  .student-dashboard {
    .action-buttons {
      .el-button {
        width: 100%;
        margin-bottom: 8px;
      }
    }
  }
}
</style>
