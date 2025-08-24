<template>
  <div class="activities">
    <div class="page-header">
      <h1>活动报名</h1>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="活动标题">
          <el-input v-model="searchForm.title" placeholder="请输入活动标题" clearable />
        </el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="环保公益" value="ENVIRONMENTAL" />
            <el-option label="社区服务" value="COMMUNITY" />
            <el-option label="教育支教" value="EDUCATION" />
            <el-option label="敬老助残" value="ELDERLY_CARE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动时间">
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
    
    <!-- 活动列表 -->
    <div class="activity-grid">
      <el-card 
        v-for="activity in activityList" 
        :key="activity.id" 
        class="activity-card"
        shadow="hover"
      >
        <div class="activity-header">
          <h3 class="activity-title">{{ activity.title }}</h3>
          <el-tag :type="getTypeColor(activity.type)">
            {{ getTypeLabel(activity.type) }}
          </el-tag>
        </div>
        
        <div class="activity-content">
          <p class="activity-description">{{ activity.description }}</p>
          
          <div class="activity-info">
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <span>{{ activity.location }}</span>
            </div>
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(activity.startTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>{{ activity.currentParticipants }}/{{ activity.maxParticipants }}人</span>
            </div>
            <div class="info-item">
              <el-icon><Star /></el-icon>
              <span>{{ activity.points }}积分</span>
            </div>
          </div>
          
          <div class="activity-meta">
            <span class="creator">创建者：{{ activity.creatorName }}</span>
            <span class="deadline">报名截止：{{ formatDate(activity.registrationDeadline) }}</span>
          </div>
        </div>
        
        <div class="activity-actions">
          <el-button type="primary" @click="viewActivity(activity.id)">
            查看详情
          </el-button>
          <el-button 
            v-if="canSignup(activity)"
            type="success" 
            @click="signupActivity(activity)"
          >
            立即报名
          </el-button>
          <el-button 
            v-else-if="isSignedUp(activity)"
            type="info" 
            disabled
          >
            已报名
          </el-button>
          <el-button 
            v-else-if="isFullOrExpired(activity)"
            type="info" 
            disabled
          >
            {{ getDisabledReason(activity) }}
          </el-button>
        </div>
      </el-card>
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="activityList.length > 0">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 空状态 -->
    <el-empty v-if="!loading && activityList.length === 0" description="暂无活动" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Clock, User, Star } from '@element-plus/icons-vue'
import { activityAPI, signupAPI } from '@/api'
import { formatDate, formatDateTime } from '@/utils/format'
import { usePagination } from '@/composables/usePagination'

const router = useRouter()
const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination(12)

const activityList = ref([])
const mySignups = ref([])

const searchForm = reactive({
  title: '',
  type: '',
  dateRange: []
})

const typeLabels = {
  ENVIRONMENTAL: '环保公益',
  COMMUNITY: '社区服务',
  EDUCATION: '教育支教',
  ELDERLY_CARE: '敬老助残',
  OTHER: '其他'
}

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

const fetchActivityList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      status: 'PUBLISHED', // 只显示已发布的活动
      ...searchForm
    }
    const response = await activityAPI.getActivityList(params)
    if (response.code === 200) {
      activityList.value = response.data.records
      updatePagination(response.data)
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
    // 使用模拟数据
    activityList.value = [
      {
        id: 1,
        title: '社区环保志愿活动',
        type: 'ENVIRONMENTAL',
        description: '参与社区环保活动，清理公园垃圾，保护环境从我做起。活动将在市中心公园进行，需要参与者自备垃圾袋和手套。',
        location: '市中心公园',
        startTime: '2024-01-20T09:00:00',
        endTime: '2024-01-20T17:00:00',
        registrationDeadline: '2024-01-18T23:59:59',
        maxParticipants: 30,
        currentParticipants: 25,
        points: 15,
        creatorName: '张老师'
      },
      {
        id: 2,
        title: '敬老院志愿服务',
        type: 'ELDERLY_CARE',
        description: '为敬老院老人提供陪伴和帮助，包括聊天、整理房间、协助用餐等。需要有耐心和爱心的同学参与。',
        location: '阳光敬老院',
        startTime: '2024-01-25T14:00:00',
        endTime: '2024-01-25T18:00:00',
        registrationDeadline: '2024-01-23T23:59:59',
        maxParticipants: 20,
        currentParticipants: 15,
        points: 20,
        creatorName: '李老师'
      }
    ]
    updatePagination({
      current: 1,
      size: 12,
      total: 2,
      pages: 1
    })
  } finally {
    loading.value = false
  }
}

const fetchMySignups = async () => {
  try {
    // 这里应该获取当前用户的报名记录
    // const response = await signupAPI.getUserSignups(userId)
    mySignups.value = [1] // 模拟已报名活动ID
  } catch (error) {
    console.error('获取我的报名失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchActivityList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    type: '',
    dateRange: []
  })
  handleSearch()
}

const viewActivity = (id) => {
  router.push(`/activities/${id}`)
}

const canSignup = (activity) => {
  const now = new Date()
  const deadline = new Date(activity.registrationDeadline)
  return !isSignedUp(activity) && 
         activity.currentParticipants < activity.maxParticipants && 
         now < deadline
}

const isSignedUp = (activity) => {
  return mySignups.value.includes(activity.id)
}

const isFullOrExpired = (activity) => {
  const now = new Date()
  const deadline = new Date(activity.registrationDeadline)
  return activity.currentParticipants >= activity.maxParticipants || now >= deadline
}

const getDisabledReason = (activity) => {
  const now = new Date()
  const deadline = new Date(activity.registrationDeadline)
  
  if (activity.currentParticipants >= activity.maxParticipants) {
    return '名额已满'
  }
  if (now >= deadline) {
    return '报名已截止'
  }
  return '无法报名'
}

const signupActivity = async (activity) => {
  try {
    await ElMessageBox.confirm(
      `确定要报名参加 "${activity.title}" 吗？`,
      '确认报名',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await signupAPI.signupActivity(activity.id)
    ElMessage.success('报名成功！请等待审核。')
    
    // 更新本地状态
    mySignups.value.push(activity.id)
    fetchActivityList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('报名失败:', error)
    }
  }
}

onMounted(() => {
  fetchMySignups()
  fetchActivityList()
})
</script>

<style lang="scss" scoped>
.activities {
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
  
  .activity-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
    
    .activity-card {
      height: fit-content;
      
      .activity-header {
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
      
      .activity-content {
        .activity-description {
          color: #606266;
          line-height: 1.6;
          margin-bottom: 16px;
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
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
        
        .activity-meta {
          display: flex;
          justify-content: space-between;
          font-size: 12px;
          color: #909399;
          margin-bottom: 16px;
        }
      }
      
      .activity-actions {
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
  .activities {
    .activity-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
