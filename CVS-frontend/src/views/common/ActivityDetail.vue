<template>
  <div class="activity-detail">
    <div class="page-header">
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>
    
    <el-card v-if="activity" class="activity-card">
      <div class="activity-header">
        <h1 class="activity-title">{{ activity.title }}</h1>
        <div class="activity-meta">
          <el-tag :type="getTypeColor(activity.type)">
            {{ getTypeLabel(activity.type) }}
          </el-tag>
          <el-tag :type="getStatusType(activity.status)">
            {{ getStatusLabel(activity.status) }}
          </el-tag>
        </div>
      </div>
      
      <div class="activity-content">
        <el-row :gutter="20">
          <el-col :xs="24" :lg="16">
            <div class="activity-description">
              <h3>活动描述</h3>
              <p>{{ activity.description }}</p>
            </div>
            
            <div class="activity-requirements" v-if="activity.requirements">
              <h3>参与要求</h3>
              <p>{{ activity.requirements }}</p>
            </div>
            
            <div class="activity-contact">
              <h3>联系方式</h3>
              <p>{{ activity.contactInfo }}</p>
            </div>
          </el-col>
          
          <el-col :xs="24" :lg="8">
            <div class="activity-info">
              <h3>活动信息</h3>
              <div class="info-list">
                <div class="info-item">
                  <el-icon><Location /></el-icon>
                  <span class="label">活动地点：</span>
                  <span>{{ activity.location }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Clock /></el-icon>
                  <span class="label">开始时间：</span>
                  <span>{{ formatDateTime(activity.startTime) }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Clock /></el-icon>
                  <span class="label">结束时间：</span>
                  <span>{{ formatDateTime(activity.endTime) }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Calendar /></el-icon>
                  <span class="label">报名截止：</span>
                  <span>{{ formatDateTime(activity.registrationDeadline) }}</span>
                </div>
                <div class="info-item">
                  <el-icon><User /></el-icon>
                  <span class="label">参与人数：</span>
                  <span>{{ activity.currentParticipants }}/{{ activity.maxParticipants }}人</span>
                </div>
                <div class="info-item">
                  <el-icon><Star /></el-icon>
                  <span class="label">积分奖励：</span>
                  <span>{{ activity.points }}分</span>
                </div>
                <div class="info-item">
                  <el-icon><Avatar /></el-icon>
                  <span class="label">创建者：</span>
                  <span>{{ activity.creatorName }}</span>
                </div>
              </div>
            </div>
            
            <div class="activity-actions" v-if="showActions">
              <el-button 
                v-if="canSignup"
                type="primary" 
                size="large" 
                style="width: 100%"
                @click="signupActivity"
              >
                立即报名
              </el-button>
              <el-button 
                v-else-if="isSignedUp"
                type="info" 
                size="large" 
                style="width: 100%"
                disabled
              >
                已报名
              </el-button>
              <el-button 
                v-else-if="isFullOrExpired"
                type="info" 
                size="large" 
                style="width: 100%"
                disabled
              >
                {{ getDisabledReason() }}
              </el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <!-- 参与者列表 -->
    <el-card v-if="activity && activity.status === 'PUBLISHED'" title="参与者列表" class="participants-card">
      <el-table :data="participants" stripe>
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="studentId" label="学号" v-if="showStudentId" />
        <el-table-column prop="signupTime" label="报名时间">
          <template #default="{ row }">
            {{ formatDateTime(row.signupTime) }}
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
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>
    
    <!-- 错误状态 -->
    <el-result
      v-if="error"
      icon="error"
      title="加载失败"
      :sub-title="error"
    >
      <template #extra>
        <el-button type="primary" @click="fetchActivityDetail">重试</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Location, Clock, Calendar, User, Star, Avatar } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { activityAPI, signupAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'

const route = useRoute()
const authStore = useAuthStore()

const activity = ref(null)
const participants = ref([])
const loading = ref(false)
const error = ref('')
const mySignups = ref([])

const typeLabels = {
  ENVIRONMENTAL: '环保公益',
  COMMUNITY: '社区服务',
  EDUCATION: '教育支教',
  ELDERLY_CARE: '敬老助残',
  OTHER: '其他'
}

const showActions = computed(() => {
  return authStore.user?.role === 'STUDENT' && activity.value?.status === 'PUBLISHED'
})

const showStudentId = computed(() => {
  return authStore.user?.role !== 'STUDENT'
})

const canSignup = computed(() => {
  if (!activity.value) return false
  const now = new Date()
  const deadline = new Date(activity.value.registrationDeadline)
  return !isSignedUp.value && 
         activity.value.currentParticipants < activity.value.maxParticipants && 
         now < deadline
})

const isSignedUp = computed(() => {
  return mySignups.value.includes(activity.value?.id)
})

const isFullOrExpired = computed(() => {
  if (!activity.value) return false
  const now = new Date()
  const deadline = new Date(activity.value.registrationDeadline)
  return activity.value.currentParticipants >= activity.value.maxParticipants || now >= deadline
})

const getTypeLabel = (type) => {
  return typeLabels[type] || type
}

const getTypeColor = (type) => {
  const colorMap = {
    ENVIRONMENTAL: 'success',
    COMMUNITY: 'primary',
    EDUCATION: 'warning',
    ELDERLY_CARE: 'danger',
    OTHER: 'info'
  }
  return colorMap[type] || 'info'
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

const getDisabledReason = () => {
  if (!activity.value) return ''
  const now = new Date()
  const deadline = new Date(activity.value.registrationDeadline)
  
  if (activity.value.currentParticipants >= activity.value.maxParticipants) {
    return '名额已满'
  }
  if (now >= deadline) {
    return '报名已截止'
  }
  return '无法报名'
}

const fetchActivityDetail = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await activityAPI.getActivityById(route.params.id)
    if (response.code === 200) {
      activity.value = response.data
    }
  } catch (err) {
    console.error('获取活动详情失败:', err)
    error.value = '获取活动详情失败'
    // 使用模拟数据
    activity.value = {
      id: 1,
      title: '社区环保志愿活动',
      type: 'ENVIRONMENTAL',
      status: 'PUBLISHED',
      description: '参与社区环保活动，清理公园垃圾，保护环境从我做起。活动将在市中心公园进行，需要参与者自备垃圾袋和手套。我们将提供必要的工具和指导，希望大家能够积极参与，为环保事业贡献自己的力量。',
      requirements: '1. 身体健康，能够进行户外活动\n2. 自备垃圾袋和手套\n3. 穿着适合户外活动的服装\n4. 准时参加，不得无故缺席',
      location: '市中心公园',
      startTime: '2024-01-20T09:00:00',
      endTime: '2024-01-20T17:00:00',
      registrationDeadline: '2024-01-18T23:59:59',
      maxParticipants: 30,
      currentParticipants: 25,
      points: 15,
      creatorName: '张老师',
      contactInfo: '联系电话：138-0013-8001\n邮箱：zhang@example.com'
    }
  } finally {
    loading.value = false
  }
}

const fetchParticipants = async () => {
  try {
    const response = await signupAPI.getActivitySignups(route.params.id, { status: 'APPROVED' })
    if (response.code === 200) {
      participants.value = response.data.records
    }
  } catch (err) {
    console.error('获取参与者列表失败:', err)
    // 使用模拟数据
    participants.value = [
      {
        name: '张三',
        studentId: '2021001',
        signupTime: '2024-01-15T10:30:00',
        status: 'APPROVED'
      },
      {
        name: '李四',
        studentId: '2021002',
        signupTime: '2024-01-15T11:00:00',
        status: 'APPROVED'
      }
    ]
  }
}

const fetchMySignups = async () => {
  if (authStore.user?.role === 'STUDENT') {
    try {
      // 这里应该获取当前用户的报名记录
      mySignups.value = [1] // 模拟已报名活动ID
    } catch (err) {
      console.error('获取我的报名失败:', err)
    }
  }
}

const signupActivity = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要报名参加 "${activity.value.title}" 吗？`,
      '确认报名',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await signupAPI.signupActivity(activity.value.id)
    ElMessage.success('报名成功！请等待审核。')
    
    // 更新本地状态
    mySignups.value.push(activity.value.id)
    activity.value.currentParticipants++
  } catch (err) {
    if (err !== 'cancel') {
      console.error('报名失败:', err)
    }
  }
}

onMounted(() => {
  fetchActivityDetail()
  fetchParticipants()
  fetchMySignups()
})
</script>

<style lang="scss" scoped>
.activity-detail {
  .page-header {
    margin-bottom: 20px;
  }
  
  .activity-card {
    margin-bottom: 20px;
    
    .activity-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24px;
      
      .activity-title {
        margin: 0;
        font-size: 28px;
        font-weight: 600;
        color: #303133;
        flex: 1;
        margin-right: 20px;
      }
      
      .activity-meta {
        display: flex;
        gap: 8px;
      }
    }
    
    .activity-content {
      .activity-description,
      .activity-requirements,
      .activity-contact {
        margin-bottom: 24px;
        
        h3 {
          margin: 0 0 12px 0;
          font-size: 18px;
          color: #303133;
        }
        
        p {
          margin: 0;
          color: #606266;
          line-height: 1.8;
          white-space: pre-line;
        }
      }
      
      .activity-info {
        background: #f8f9fa;
        padding: 20px;
        border-radius: 8px;
        margin-bottom: 20px;
        
        h3 {
          margin: 0 0 16px 0;
          font-size: 18px;
          color: #303133;
        }
        
        .info-list {
          .info-item {
            display: flex;
            align-items: center;
            margin-bottom: 12px;
            font-size: 14px;
            
            .el-icon {
              color: #409eff;
              margin-right: 8px;
            }
            
            .label {
              color: #909399;
              min-width: 80px;
            }
            
            &:last-child {
              margin-bottom: 0;
            }
          }
        }
      }
      
      .activity-actions {
        margin-top: 20px;
      }
    }
  }
  
  .participants-card {
    margin-bottom: 20px;
  }
  
  .loading {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .activity-detail {
    .activity-header {
      flex-direction: column;
      gap: 16px;
      
      .activity-title {
        margin-right: 0;
      }
    }
  }
}
</style>
