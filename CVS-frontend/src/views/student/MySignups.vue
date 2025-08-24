<template>
  <div class="my-signups">
    <div class="page-header">
      <h1>我的报名</h1>
    </div>
    
    <!-- 状态筛选 -->
    <el-card class="filter-card">
      <el-radio-group v-model="statusFilter" @change="handleFilterChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="PENDING">待审核</el-radio-button>
        <el-radio-button label="APPROVED">已通过</el-radio-button>
        <el-radio-button label="REJECTED">已拒绝</el-radio-button>
      </el-radio-group>
    </el-card>
    
    <!-- 报名列表 -->
    <div class="signup-list">
      <el-card 
        v-for="signup in signupList" 
        :key="signup.id" 
        class="signup-card"
        shadow="hover"
      >
        <div class="signup-header">
          <h3 class="activity-title">{{ signup.activityTitle }}</h3>
          <el-tag :type="getStatusType(signup.status)">
            {{ getStatusLabel(signup.status) }}
          </el-tag>
        </div>
        
        <div class="signup-content">
          <div class="activity-info">
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <span>{{ signup.location }}</span>
            </div>
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(signup.startTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span>报名时间：{{ formatDateTime(signup.signupTime) }}</span>
            </div>
            <div class="info-item" v-if="signup.points">
              <el-icon><Star /></el-icon>
              <span>{{ signup.points }}积分</span>
            </div>
          </div>
          
          <div class="signup-progress" v-if="signup.status === 'APPROVED'">
            <div class="progress-item" :class="{ active: signup.checkedIn }">
              <el-icon><Check /></el-icon>
              <span>已签到</span>
            </div>
            <div class="progress-item" :class="{ active: signup.checkedOut }">
              <el-icon><Check /></el-icon>
              <span>已签退</span>
            </div>
            <div class="progress-item" :class="{ active: signup.recordCreated }">
              <el-icon><Check /></el-icon>
              <span>记录已生成</span>
            </div>
          </div>
          
          <div class="rejection-reason" v-if="signup.status === 'REJECTED' && signup.rejectionReason">
            <el-alert
              :title="signup.rejectionReason"
              type="error"
              :closable="false"
              show-icon
            />
          </div>
        </div>
        
        <div class="signup-actions">
          <el-button type="primary" @click="viewActivity(signup.activityId)">
            查看活动
          </el-button>
          <el-button 
            v-if="canCancel(signup)"
            type="danger" 
            @click="cancelSignup(signup)"
          >
            取消报名
          </el-button>
          <el-button 
            v-if="signup.status === 'APPROVED' && signup.recordCreated"
            type="success" 
            @click="viewRecord(signup.recordId)"
          >
            查看记录
          </el-button>
        </div>
      </el-card>
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="signupList.length > 0">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 空状态 -->
    <el-empty v-if="!loading && signupList.length === 0" description="暂无报名记录" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Clock, Calendar, Star, Check } from '@element-plus/icons-vue'
import { signupAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import { usePagination } from '@/composables/usePagination'

const router = useRouter()
const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const signupList = ref([])
const statusFilter = ref('')

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

const fetchSignupList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      status: statusFilter.value || undefined
    }
    // 这里应该调用获取用户报名列表的API
    // const response = await signupAPI.getUserSignups(userId, params)
    
    // 使用模拟数据
    signupList.value = [
      {
        id: 1,
        activityId: 1,
        activityTitle: '社区环保志愿活动',
        location: '市中心公园',
        startTime: '2024-01-20T09:00:00',
        signupTime: '2024-01-15T10:30:00',
        status: 'APPROVED',
        points: 15,
        checkedIn: true,
        checkedOut: true,
        recordCreated: true,
        recordId: 1
      },
      {
        id: 2,
        activityId: 2,
        activityTitle: '敬老院志愿服务',
        location: '阳光敬老院',
        startTime: '2024-01-25T14:00:00',
        signupTime: '2024-01-18T15:20:00',
        status: 'PENDING',
        points: 20,
        checkedIn: false,
        checkedOut: false,
        recordCreated: false
      },
      {
        id: 3,
        activityId: 3,
        activityTitle: '图书馆志愿服务',
        location: '市图书馆',
        startTime: '2024-01-15T09:00:00',
        signupTime: '2024-01-10T14:00:00',
        status: 'REJECTED',
        rejectionReason: '报名人数已满，感谢您的参与热情',
        checkedIn: false,
        checkedOut: false,
        recordCreated: false
      }
    ]
    
    updatePagination({
      current: 1,
      size: 10,
      total: 3,
      pages: 1
    })
  } catch (error) {
    console.error('获取报名列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  pagination.current = 1
  fetchSignupList()
}

const viewActivity = (activityId) => {
  router.push(`/activities/${activityId}`)
}

const viewRecord = (recordId) => {
  router.push(`/student/records/${recordId}`)
}

const canCancel = (signup) => {
  // 只有待审核状态的报名可以取消
  return signup.status === 'PENDING'
}

const cancelSignup = async (signup) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消报名 "${signup.activityTitle}" 吗？`,
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await signupAPI.cancelSignup(signup.activityId)
    ElMessage.success('取消报名成功')
    fetchSignupList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消报名失败:', error)
    }
  }
}

onMounted(() => {
  fetchSignupList()
})
</script>

<style lang="scss" scoped>
.my-signups {
  .page-header {
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
  }
  
  .signup-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-bottom: 20px;
    
    .signup-card {
      .signup-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 16px;
        
        .activity-title {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #303133;
          flex: 1;
          margin-right: 12px;
        }
      }
      
      .signup-content {
        .activity-info {
          margin-bottom: 16px;
          
          .info-item {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 8px;
            font-size: 14px;
            color: #606266;
            
            .el-icon {
              color: #909399;
            }
          }
        }
        
        .signup-progress {
          display: flex;
          gap: 24px;
          margin-bottom: 16px;
          
          .progress-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            color: #c0c4cc;
            
            &.active {
              color: #67c23a;
            }
            
            .el-icon {
              font-size: 16px;
            }
          }
        }
        
        .rejection-reason {
          margin-bottom: 16px;
        }
      }
      
      .signup-actions {
        display: flex;
        gap: 12px;
        
        .el-button {
          flex: 1;
        }
      }
    }
  }
  
  .pagination {
    text-align: center;
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .my-signups {
    .signup-progress {
      flex-direction: column;
      gap: 12px !important;
    }
    
    .signup-actions {
      flex-direction: column;
      
      .el-button {
        width: 100%;
      }
    }
  }
}
</style>
