<template>
  <div class="signup-management">
    <div class="page-header">
      <h1>报名管理</h1>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="活动名称">
          <el-select v-model="searchForm.activityId" placeholder="请选择活动" clearable style="width: 240px">
            <el-option v-for="activity in myActivities" :key="activity.id" :label="activity.title"
              :value="activity.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input v-model="searchForm.userName" placeholder="请输入学生姓名" clearable />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 180px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 报名列表 -->
    <el-card>
      <div class="table-header">
        <div class="batch-actions">
          <el-button type="success" :disabled="selectedSignups.length === 0" @click="batchApprove">
            批量通过
          </el-button>
          <el-button type="danger" :disabled="selectedSignups.length === 0" @click="batchReject">
            批量拒绝
          </el-button>
        </div>
      </div>

      <el-table :data="signupList" :loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="activityTitle" label="活动名称" min-width="200">
          <template #default="{ row }">
            <div class="activity-title-cell" :title="row.activityTitle || '暂无'">
              {{ row.activityTitle || '暂无' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="学生姓名" width="120" />
        <el-table-column prop="username" label="学号" width="120" />
        <el-table-column prop="reason" label="申请原因" width="180">
          <template #default="{ row }">
            <div class="reason-cell" :title="row.reason || '暂无'">
              {{ row.reason || '暂无' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="报名时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <!-- 待审核状态：显示通过/拒绝按钮 -->
            <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="approveSignup(row)">
              通过
            </el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="rejectSignup(row)">
              拒绝
            </el-button>

            <!-- 已通过状态：不显示任何按钮 -->
            <el-tag v-if="row.status === 'APPROVED'" type="success" size="small">
              已通过
            </el-tag>

            <!-- 已拒绝状态：显示标签 -->
            <el-tag v-if="row.status === 'REJECTED'" type="danger" size="small">
              已拒绝
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size"
          :total="pagination.total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { signupAPI, activityAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import { usePagination } from '@/composables/usePagination'

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const signupList = ref([])
const myActivities = ref([])
const selectedSignups = ref([])

const searchForm = reactive({
  activityId: '',
  userName: '',
  status: 'PENDING'  // 默认显示待审核
})

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

const fetchMyActivities = async () => {
  try {
    // 使用正确的分页查询接口
    const response = await activityAPI.getMyActivities({
      pageNum: 1,
      pageSize: 1000,
      params: {}
    })
    if (response.code === 200) {
      myActivities.value = response.data.records || []
    }
  } catch (error) {
    console.error('获取我的活动失败:', error)
    ElMessage.error('获取活动列表失败，请稍后重试')
    myActivities.value = []
  }
}

const fetchSignupList = async () => {
  loading.value = true
  try {
    // 使用正确的分页查询接口
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      params: {
        activityId: searchForm.activityId || undefined,
        userName: searchForm.userName || undefined,
        status: searchForm.status || undefined
      }
    }

    const response = await signupAPI.getSignupList(params)
    if (response.code === 200) {
      signupList.value = response.data.records || []
      updatePagination(response.data)
    } else {
      ElMessage.error(response.message || '获取报名列表失败')
      signupList.value = []
    }
  } catch (error) {
    console.error('获取报名列表失败:', error)
    ElMessage.error('网络错误，请稍后重试')
    signupList.value = []
    updatePagination({
      current: 1,
      size: pagination.size,
      total: 0,
      pages: 0
    })
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchSignupList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    activityId: '',
    userName: '',
    status: ''
  })
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedSignups.value = selection
}

const approveSignup = async (row) => {
  try {
    await signupAPI.approve(row.id)
    ElMessage.success('审核通过')
    fetchSignupList()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error(error.response?.data?.message || '审核失败，请稍后重试')
  }
}

const rejectSignup = async (row) => {
  try {
    const { value: rejectReason } = await ElMessageBox.prompt(
      '请输入拒绝原因',
      '拒绝报名',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '请输入拒绝原因'
      }
    )

    await signupAPI.reject(row.id, { rejectReason })
    ElMessage.success('已拒绝')
    fetchSignupList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝失败:', error)
      ElMessage.error(error.response?.data?.message || '拒绝失败，请稍后重试')
    }
  }
}

const batchApprove = async () => {
  try {
    const pendingSignups = selectedSignups.value.filter(signup => signup.status === 'PENDING')
    if (pendingSignups.length === 0) {
      ElMessage.warning('没有待审核的报名')
      return
    }

    await ElMessageBox.confirm(
      `确定要批量通过选中的 ${pendingSignups.length} 个报名吗？`,
      '批量审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const signupIds = pendingSignups.map(signup => signup.id)
    await signupAPI.batchApprove(signupIds)
    ElMessage.success('批量审核完成')
    fetchSignupList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
      ElMessage.error(error.response?.data?.message || '批量审核失败，请稍后重试')
    }
  }
}

const batchReject = async () => {
  try {
    const pendingSignups = selectedSignups.value.filter(signup => signup.status === 'PENDING')
    if (pendingSignups.length === 0) {
      ElMessage.warning('没有待审核的报名')
      return
    }

    const { value: rejectReason } = await ElMessageBox.prompt(
      `请输入批量拒绝的原因（将应用于所有选中的 ${pendingSignups.length} 个报名）`,
      '批量拒绝',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入拒绝原因...',
        inputValidator: (value) => {
          if (!value || value.trim().length === 0) {
            return '请输入拒绝原因'
          }
          return true
        }
      }
    )

    const signupIds = pendingSignups.map(signup => signup.id)
    await signupAPI.batchReject({
      signupIds,
      rejectReason: rejectReason.trim()
    })
    ElMessage.success('批量拒绝完成')
    fetchSignupList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量拒绝失败:', error)
      ElMessage.error(error.response?.data?.message || '批量拒绝失败，请稍后重试')
    }
  }
}



onMounted(() => {
  fetchMyActivities()
  fetchSignupList()  // 直接加载报名列表
})
</script>

<style lang="scss" scoped>
.signup-management {
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

  .table-header {
    margin-bottom: 16px;

    .batch-actions {
      display: flex;
      gap: 12px;
    }
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
  }

  // 活动名称单元格样式
  .activity-title-cell {
    max-width: 180px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    cursor: help;

    &:hover {
      color: #409eff;
    }
  }

  // 申请原因单元格样式
  .reason-cell {
    max-width: 160px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    cursor: help;

    &:hover {
      color: #409eff;
    }
  }
}
</style>
