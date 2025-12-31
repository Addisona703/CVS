<template>
  <div class="certificate-review">
    <div class="page-header">
      <h1>证书审核</h1>
    </div>
    
    <!-- 待审核证书列表 -->
    <el-card>
      <el-table :data="certificateList" :loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="申请人" width="120" />
        <el-table-column prop="username" label="学号" width="120" />
        <el-table-column prop="certificateNumber" label="证书编号" width="180" />
        <el-table-column prop="purpose" label="申请目的" min-width="150" />
        <el-table-column label="服务统计" width="200">
          <template #default="{ row }">
            <div v-if="row.statistics">
              <div>活动数: {{ row.statistics.completedActivityCount || 0 }}</div>
              <div>总时长: {{ formatHours(row.statistics.totalServiceHours) }}</div>
            </div>
            <div v-else>-</div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">
              拒绝
            </el-button>
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
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝证书申请"
      width="500px"
    >
      <el-form :model="rejectForm" label-width="100px">
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
        <el-button type="primary" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { certificateAPI } from '@/api/certificate'
import { formatDateTime } from '@/utils/format'
import { usePagination } from '@/composables/usePagination'

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const certificateList = ref([])
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  id: null,
  reason: ''
})

const formatHours = (hours) => {
  if (hours === null || hours === undefined) return '0小时'
  return `${hours.toFixed(1)}小时`
}

const fetchCertificateList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size
    }
    const response = await certificateAPI.getPendingCertificates(params)
    if (response.code === 200) {
      certificateList.value = response.data.records
      updatePagination(response.data)
    }
  } catch (error) {
    console.error('获取待审核证书列表失败:', error)
    ElMessage.error('获取待审核证书列表失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过 "${row.name}" 的证书申请吗？`,
      '确认通过',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await approveCertificate(row.id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
    }
  }
}

const handleReject = (row) => {
  rejectForm.id = row.id
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  try {
    await rejectCertificate(rejectForm.id, rejectForm.reason)
    rejectDialogVisible.value = false
  } catch (error) {
    console.error('拒绝失败:', error)
  }
}

const approveCertificate = async (id) => {
  try {
    const response = await certificateAPI.approveCertificate({
      id,
      approved: true
    })
    if (response.code === 200) {
      ElMessage.success('审核通过')
      fetchCertificateList()
    }
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
    throw error
  }
}

const rejectCertificate = async (id, reason) => {
  try {
    const response = await certificateAPI.approveCertificate({
      id,
      approved: false,
      rejectReason: reason
    })
    if (response.code === 200) {
      ElMessage.success('已拒绝申请')
      fetchCertificateList()
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
    throw error
  }
}

onMounted(() => {
  fetchCertificateList()
})
</script>

<style lang="scss" scoped>
.certificate-review {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }
  
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
