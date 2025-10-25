<template>
  <div class="activity-review-page">
    <div class="page-header">
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">活动审核管理</h1>
    </div>

    <el-card v-if="activity" class="activity-info-card">
      <div class="activity-summary">
        <h2>{{ activity.title }}</h2>
        <div class="activity-meta">
          <el-tag :type="getTypeColor(activity.type)">
            {{ getTypeLabel(activity.type) }}
          </el-tag>
          <el-tag :type="getStatusType(activity.status)">
            {{ getStatusLabel(activity.status) }}
          </el-tag>
        </div>
      </div>
    </el-card>

    <el-card class="review-card">
      <!-- 标签页导航 -->
      <el-tabs v-model="activeTab" class="review-tabs">
        <el-tab-pane label="报名审核" name="registration" data-tab="registration">
          <div class="registration-review">
            <!-- 状态筛选器 -->
            <div class="filter-bar">
              <el-select
                v-model="registrationFilter"
                placeholder="选择状态"
                clearable
                style="width: 200px"
                class="status-filter"
                data-filter="status"
                @change="handleRegistrationFilterChange"
              >
                <el-option label="全部" value="" />
                <el-option label="待审核" value="pending" />
                <el-option label="已通过" value="approved" />
                <el-option label="已拒绝" value="rejected" />
              </el-select>
            </div>

            <!-- 报名审核列表 -->
            <div class="review-list" data-highlight="pending">
              <el-table :data="registrationList" v-loading="registrationLoading">
                <el-table-column prop="studentName" label="学生姓名" width="120" />
                <el-table-column prop="studentNumber" label="学号" width="120" />
                <el-table-column prop="reason" label="报名原因" min-width="200" show-overflow-tooltip />
                <el-table-column prop="signupTime" label="报名时间" width="160">
                  <template #default="{ row }">
                    {{ formatDateTime(row.signupTime) }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getRegistrationStatusType(row.status)">
                      {{ getRegistrationStatusLabel(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="{ row }">
                    <el-button
                      v-if="row.status === 'PENDING'"
                      type="success"
                      size="small"
                      @click="approveRegistration(row)"
                    >
                      通过
                    </el-button>
                    <el-button
                      v-if="row.status === 'PENDING'"
                      type="danger"
                      size="small"
                      @click="rejectRegistration(row)"
                    >
                      拒绝
                    </el-button>
                    <span v-else class="status-text">
                      {{ getRegistrationStatusLabel(row.status) }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="签退审核" name="checkout" data-tab="checkout">
          <div class="checkout-review">
            <!-- 状态筛选器 -->
            <div class="filter-bar">
              <el-select
                v-model="checkoutFilter"
                placeholder="选择状态"
                clearable
                style="width: 200px"
                class="status-filter"
                data-filter="status"
                @change="handleCheckoutFilterChange"
              >
                <el-option label="全部" value="" />
                <el-option label="待审核" value="pending" />
                <el-option label="已通过" value="approved" />
                <el-option label="已拒绝" value="rejected" />
              </el-select>
            </div>

            <!-- 签退审核列表 -->
            <div class="review-list" data-highlight="pending">
              <el-table :data="checkoutList" v-loading="checkoutLoading">
                <el-table-column prop="studentName" label="学生姓名" width="120" />
                <el-table-column prop="studentNumber" label="学号" width="120" />
                <el-table-column prop="checkoutReason" label="签退原因" min-width="200" show-overflow-tooltip />
                <el-table-column prop="checkoutTime" label="申请时间" width="160">
                  <template #default="{ row }">
                    {{ formatDateTime(row.checkoutTime) }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getCheckoutStatusType(row.status)">
                      {{ getCheckoutStatusLabel(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="{ row }">
                    <el-button
                      v-if="row.status === 'PENDING'"
                      type="success"
                      size="small"
                      @click="approveCheckout(row)"
                    >
                      通过
                    </el-button>
                    <el-button
                      v-if="row.status === 'PENDING'"
                      type="danger"
                      size="small"
                      @click="rejectCheckout(row)"
                    >
                      拒绝
                    </el-button>
                    <span v-else class="status-text">
                      {{ getCheckoutStatusLabel(row.status) }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useNotificationNavigation } from '@/composables/useNotificationNavigation'
import { activityAPI, signupAPI } from '@/api'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const { checkNotificationNavigation } = useNotificationNavigation()

const activity = ref(null)
const activeTab = ref('registration')
const registrationList = ref([])
const checkoutList = ref([])
const registrationLoading = ref(false)
const checkoutLoading = ref(false)
const registrationFilter = ref('')
const checkoutFilter = ref('')

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

const getStatusLabel = (status) => {
  const statusLabels = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    CANCELLED: '已取消',
    COMPLETED: '已完成'
  }
  return statusLabels[status] || status
}

const getStatusType = (status) => {
  const colorMap = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    CANCELLED: 'danger',
    COMPLETED: 'primary'
  }
  return colorMap[status] || 'info'
}

const getRegistrationStatusLabel = (status) => {
  const statusLabels = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return statusLabels[status] || status
}

const getRegistrationStatusType = (status) => {
  const colorMap = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return colorMap[status] || 'info'
}

const getCheckoutStatusLabel = (status) => {
  const statusLabels = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return statusLabels[status] || status
}

const getCheckoutStatusType = (status) => {
  const colorMap = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return colorMap[status] || 'info'
}

const fetchActivity = async () => {
  try {
    const response = await activityAPI.getActivityById(route.params.id)
    if (response.code === 200) {
      activity.value = response.data
    }
  } catch (error) {
    console.error('获取活动信息失败:', error)
    ElMessage.error('获取活动信息失败')
  }
}

const fetchRegistrationList = async () => {
  registrationLoading.value = true
  try {
    const params = {
      activityId: route.params.id
    }
    if (registrationFilter.value) {
      params.status = registrationFilter.value.toUpperCase()
    }
    
    const response = await signupAPI.getActivitySignups(route.params.id, params)
    if (response.code === 200) {
      registrationList.value = response.data.records || []
    }
  } catch (error) {
    console.error('获取报名列表失败:', error)
    ElMessage.error('获取报名列表失败')
  } finally {
    registrationLoading.value = false
  }
}

const fetchCheckoutList = async () => {
  checkoutLoading.value = true
  try {
    const params = {
      activityId: route.params.id,
      type: 'checkout'
    }
    if (checkoutFilter.value) {
      params.status = checkoutFilter.value.toUpperCase()
    }
    
    // 这里应该调用签退审核API
    // const response = await checkoutAPI.getCheckoutRequests(route.params.id, params)
    // 模拟数据
    checkoutList.value = [
      {
        id: 1,
        studentName: '张三',
        studentNumber: '2021001',
        checkoutReason: '有急事需要提前离开',
        checkoutTime: '2024-01-20T15:30:00',
        status: 'PENDING'
      }
    ]
  } catch (error) {
    console.error('获取签退列表失败:', error)
    ElMessage.error('获取签退列表失败')
  } finally {
    checkoutLoading.value = false
  }
}

const handleRegistrationFilterChange = () => {
  fetchRegistrationList()
}

const handleCheckoutFilterChange = () => {
  fetchCheckoutList()
}

const approveRegistration = async (row) => {
  try {
    await signupAPI.approveSignup(row.id)
    ElMessage.success('审核通过')
    fetchRegistrationList()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

const rejectRegistration = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入拒绝原因：',
      '拒绝报名',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝原因...'
      }
    )
    
    await signupAPI.rejectSignup(row.id, { reason })
    ElMessage.success('已拒绝')
    fetchRegistrationList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝失败:', error)
      ElMessage.error('拒绝失败')
    }
  }
}

const approveCheckout = async (row) => {
  try {
    // 这里应该调用签退审核通过API
    ElMessage.success('签退审核通过')
    fetchCheckoutList()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

const rejectCheckout = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入拒绝原因：',
      '拒绝签退',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝原因...'
      }
    )
    
    // 这里应该调用签退审核拒绝API
    ElMessage.success('已拒绝签退')
    fetchCheckoutList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝失败:', error)
      ElMessage.error('拒绝失败')
    }
  }
}

// 初始化标签页
const initializeTab = () => {
  const tab = route.query.tab
  if (tab && ['registration', 'checkout'].includes(tab)) {
    activeTab.value = tab
  }
  
  const status = route.query.status
  if (status) {
    if (activeTab.value === 'registration') {
      registrationFilter.value = status
    } else if (activeTab.value === 'checkout') {
      checkoutFilter.value = status
    }
  }
}

onMounted(() => {
  fetchActivity()
  initializeTab()
  
  if (activeTab.value === 'registration') {
    fetchRegistrationList()
  } else {
    fetchCheckoutList()
  }
  
  checkNotificationNavigation()
})
</script>

<style lang="scss" scoped>
.activity-review-page {
  padding: 24px;
  
  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    
    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .activity-info-card {
    margin-bottom: 24px;
    
    .activity-summary {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h2 {
        margin: 0;
        font-size: 20px;
        color: #303133;
      }
      
      .activity-meta {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .review-card {
    .review-tabs {
      margin-top: 16px;
    }
    
    .filter-bar {
      margin-bottom: 16px;
      padding: 16px;
      background: var(--bg-color-hover, #f5f7fa);
      border-radius: var(--border-radius-content, 8px);
    }
    
    .review-list {
      .status-text {
        color: #909399;
        font-size: 14px;
      }
    }
  }
}

</style>
