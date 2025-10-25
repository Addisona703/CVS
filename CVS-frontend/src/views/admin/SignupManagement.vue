<template>
  <div class="admin-signup-management">
    <div class="page-header">
      <h1>报名审核</h1>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.activityTitle" placeholder="请输入活动名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input v-model="searchForm.userName" placeholder="请输入学生姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
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
        <el-table-column prop="activityTitle" label="活动名称" min-width="180" />
        <el-table-column prop="name" label="学生姓名" width="100" />
        <el-table-column prop="username" label="学号" width="120" />
        <el-table-column prop="reason" label="申请原因" width="150">
          <template #default="{ row }">
            <el-tooltip :content="row.reason || '暂无'" placement="top">
              <div class="text-ellipsis">{{ row.reason || '暂无' }}</div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="报名时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="approveSignup(row)">
              通过
            </el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="rejectSignup(row)">
              拒绝
            </el-button>
            <el-tag v-if="row.status === 'APPROVED'" type="success" size="small">
              已通过
            </el-tag>
            <el-tag v-if="row.status === 'REJECTED'" type="danger" size="small">
              已拒绝
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRejectConfirm" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { signupAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import { usePagination } from '@/composables/usePagination'

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const signupList = ref([])
const selectedSignups = ref([])
const rejectDialogVisible = ref(false)
const submitting = ref(false)
const currentSignup = ref(null)

const searchForm = reactive({
  activityTitle: '',
  userName: '',
  status: ''
})

const rejectForm = reactive({
  reason: ''
})

const getStatusLabel = (status) => STATUS_LABELS[status] || status
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
      pageNum: pagination.current,
      pageSize: pagination.size,
      params: searchForm
    }
    const response = await signupAPI.getSignupList(params)
    if (response.code === 200) {
      signupList.value = response.data.records
      updatePagination(response.data)
    }
  } catch (error) {
    console.error('获取报名列表失败:', error)
    ElMessage.error('获取报名列表失败')
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
    activityTitle: '',
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
    await ElMessageBox.confirm(
      `确定通过 ${row.name} 的报名申请吗？`,
      '确认通过',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    await signupAPI.approveSignup(row.id)
    ElMessage.success('审核通过')
    fetchSignupList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error(error.response?.data?.message || '审核失败')
    }
  }
}

const rejectSignup = (row) => {
  currentSignup.value = row
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const handleRejectConfirm = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  try {
    submitting.value = true
    await signupAPI.rejectSignup(currentSignup.value.id, rejectForm.reason)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    fetchSignupList()
  } catch (error) {
    console.error('拒绝失败:', error)
    ElMessage.error(error.response?.data?.message || '拒绝失败')
  } finally {
    submitting.value = false
  }
}

const batchApprove = async () => {
  try {
    await ElMessageBox.confirm(
      `确定批量通过选中的 ${selectedSignups.value.length} 条报名申请吗？`,
      '确认批量通过',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    const ids = selectedSignups.value.map(item => item.id)
    await signupAPI.batchApprove(ids)
    ElMessage.success('批量审核通过')
    fetchSignupList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
      ElMessage.error(error.response?.data?.message || '批量审核失败')
    }
  }
}

const batchReject = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `确定批量拒绝选中的 ${selectedSignups.value.length} 条报名申请吗？`,
      '批量拒绝',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝原因',
        inputType: 'textarea',
        inputValidator: (value) => {
          if (!value || !value.trim()) {
            return '请输入拒绝原因'
          }
          return true
        }
      }
    )

    const ids = selectedSignups.value.map(item => item.id)
    await signupAPI.batchReject(ids, reason)
    ElMessage.success('批量拒绝成功')
    fetchSignupList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量拒绝失败:', error)
      ElMessage.error(error.response?.data?.message || '批量拒绝失败')
    }
  }
}

onMounted(() => {
  fetchSignupList()
})
</script>

<style lang="scss" scoped>
.admin-signup-management {
  .page-header {
    margin-bottom: 24px;

    h1 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
  }

  .search-card {
    margin-bottom: 16px;
  }

  .table-header {
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .text-ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
