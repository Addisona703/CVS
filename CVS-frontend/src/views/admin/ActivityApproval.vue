<template>
  <div class="activity-approval">
    <div class="page-header">
      <h1>活动审核</h1>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="活动标题">
          <el-input v-model="searchForm.title" placeholder="请输入活动标题" clearable />
        </el-form-item>
        <el-form-item label="组织者">
          <el-input v-model="searchForm.organizerName" placeholder="请输入组织者姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 活动列表 -->
      <el-table :data="activityList" v-loading="loading" stripe>
        <el-table-column prop="title" label="活动标题" min-width="200" />
        <el-table-column prop="organizerName" label="组织者" width="120" />
        <el-table-column prop="location" label="地点" width="150" />
        <el-table-column prop="startTime" label="开始时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="maxParticipants" label="人数限制" width="100" align="center" />
        <el-table-column prop="points" label="积分" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button type="success" size="small" @click="handleApprove(row)" v-if="row.status === 'PENDING_APPROVAL'">
              审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
        class="pagination"
      />
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog v-model="approvalDialogVisible" title="活动审核" width="600px">
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="活动标题">
          <span>{{ currentActivity?.title }}</span>
        </el-form-item>
        <el-form-item label="组织者">
          <span>{{ currentActivity?.organizerName }}</span>
        </el-form-item>
        <el-form-item label="审核结果" required>
          <el-radio-group v-model="approvalForm.approved">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="拒绝原因" v-if="!approvalForm.approved" required>
          <el-input
            v-model="approvalForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitApproval" :loading="submitting">
          提交
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="活动详情" width="700px">
      <el-descriptions :column="2" border v-if="currentActivity">
        <el-descriptions-item label="活动标题" :span="2">
          {{ currentActivity.title }}
        </el-descriptions-item>
        <el-descriptions-item label="组织者">
          {{ currentActivity.organizerName }}
        </el-descriptions-item>
        <el-descriptions-item label="活动状态">
          <el-tag :type="getStatusColor(currentActivity.status)">
            {{ getStatusLabel(currentActivity.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="活动地点" :span="2">
          {{ currentActivity.location }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">
          {{ formatDateTime(currentActivity.startTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">
          {{ formatDateTime(currentActivity.endTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="报名截止">
          {{ formatDateTime(currentActivity.registrationDeadline) }}
        </el-descriptions-item>
        <el-descriptions-item label="人数限制">
          {{ currentActivity.maxParticipants }}
        </el-descriptions-item>
        <el-descriptions-item label="积分奖励">
          {{ currentActivity.points }}
        </el-descriptions-item>
        <el-descriptions-item label="联系方式">
          {{ currentActivity.contactInfo }}
        </el-descriptions-item>
        <el-descriptions-item label="活动描述" :span="2">
          {{ currentActivity.description }}
        </el-descriptions-item>
        <el-descriptions-item label="参与要求" :span="2" v-if="currentActivity.requirements">
          {{ currentActivity.requirements }}
        </el-descriptions-item>
        <el-descriptions-item label="拒绝原因" :span="2" v-if="currentActivity.rejectReason">
          <el-text type="danger">{{ currentActivity.rejectReason }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          type="success"
          @click="handleApprove(currentActivity)"
          v-if="currentActivity?.status === 'PENDING_APPROVAL'"
        >
          审核
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { activityAPI } from '@/api'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const submitting = ref(false)
const activityList = ref([])
const approvalDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentActivity = ref(null)

const searchForm = reactive({
  title: '',
  organizerName: '',
  status: 'PENDING_APPROVAL'
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const approvalForm = reactive({
  approved: true,
  rejectReason: ''
})

const getStatusLabel = (status) => STATUS_LABELS[status] || status
const getStatusColor = (status) => STATUS_COLORS[status] || 'info'

// 获取活动列表
const fetchActivityList = async () => {
  loading.value = true
  try {
    const response = await activityAPI.getActivityList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      params: searchForm
    })
    
    if (response.code === 200) {
      activityList.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
    ElMessage.error('获取活动列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchActivityList()
}

// 重置
const handleReset = () => {
  searchForm.title = ''
  searchForm.organizerName = ''
  searchForm.status = 'PENDING_APPROVAL'
  handleSearch()
}

// 查看详情
const handleViewDetail = (activity) => {
  currentActivity.value = activity
  detailDialogVisible.value = true
}

// 审核
const handleApprove = (activity) => {
  currentActivity.value = activity
  approvalForm.approved = true
  approvalForm.rejectReason = ''
  approvalDialogVisible.value = true
}

// 提交审核
const handleSubmitApproval = async () => {
  if (!approvalForm.approved && !approvalForm.rejectReason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定${approvalForm.approved ? '通过' : '拒绝'}该活动吗？`,
      '确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true
    const response = await activityAPI.approveActivity(
      currentActivity.value.id,
      approvalForm.approved,
      approvalForm.rejectReason
    )

    if (response.code === 200) {
      ElMessage.success(response.message || '审核成功')
      approvalDialogVisible.value = false
      detailDialogVisible.value = false
      fetchActivityList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error(error.response?.data?.message || '审核失败')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchActivityList()
})
</script>

<style lang="scss" scoped>
.activity-approval {
  .page-header {
    margin-bottom: var(--space-6, 24px);

    h1 {
      margin: 0;
      font-size: 28px;
      color: #303133;
      font-weight: 600;
    }
  }

  .el-card {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    border-radius: 8px;
  }

  .search-form {
    margin-bottom: var(--space-4, 16px);
  }

  .pagination {
    margin-top: var(--space-4, 16px);
    display: flex;
    justify-content: flex-end;
  }
}
</style>
