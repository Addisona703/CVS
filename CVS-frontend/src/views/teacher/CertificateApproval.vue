<template>
  <div class="teacher-cert-approval">
    <div class="page-header">
      <h1>证明审核</h1>
      <div class="actions">
        <el-button type="primary" :disabled="selectedRows.length===0" @click="bulkApprove">批量通过</el-button>
        <el-button type="danger" :disabled="selectedRows.length===0" @click="openBulkRejectDialog">批量拒绝</el-button>
        <el-button :disabled="selectedRows.length===0" @click="exportSelected">批量导出</el-button>
        <el-button :loading="loading" @click="fetchList">刷新</el-button>
      </div>
    </div>

    <el-result v-if="!canAccess" icon="warning" title="权限不足" sub-title="仅教师与学工处可访问此页面" />
    <template v-else>
      <!-- 筛选搜索 -->
      <el-card class="search-card">
        <el-form :model="searchForm" inline>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 160px">
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item label="用户ID">
            <el-input v-model="searchForm.userId" placeholder="ID" clearable style="width: 160px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 列表 -->
      <el-card>
        <el-table :data="list" :loading="loading" stripe @selection-change="onSelectionChange">
          <el-table-column type="selection" width="48" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="userName" label="学生" min-width="140" />
          <el-table-column prop="validUntil" label="有效期至" width="160" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="申请时间" width="180">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewDetail(row)">查看</el-button>
              <el-button v-if="row.status==='PENDING'" type="success" size="small" @click="confirmApprove(row)">通过</el-button>
              <el-button v-if="row.status==='PENDING'" size="small" @click="exportRow(row)">导出</el-button>
              <el-button v-if="row.status==='PENDING'" type="danger" size="small" @click="openRejectDialog(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>

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

      <!-- 详情对话框 -->
      <el-dialog v-model="detailDialog.visible" title="证明详情" width="780px">
        <div v-if="detailDialog.loading" class="loading-area">
          <el-skeleton :rows="6" animated />
        </div>
        <template v-else>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="学生">{{ detail.user?.name || detail.certificate?.userName }}</el-descriptions-item>
            <el-descriptions-item label="学号/账号">{{ detail.user?.username }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ detail.user?.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detail.user?.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ formatDateTime(detail.certificate?.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="有效期至">{{ detail.certificate?.validUntil }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="statusType(detail.certificate?.status)">
                {{ statusLabel(detail.certificate?.status) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
          <h4 style="margin: 16px 0 8px;">关联服务记录</h4>
          <el-table :data="detail.records" size="small" stripe>
            <el-table-column prop="activityTitle" label="活动" min-width="180" />
            <el-table-column label="时长" width="100">
              <template #default="{ row }">{{ row.durationHours }} 小时</template>
            </el-table-column>
            <el-table-column prop="evaluation" label="评价" min-width="180" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="记录时间" width="160">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </template>
        <template #footer>
          <el-button @click="detailDialog.visible = false">关闭</el-button>
          <el-button @click="exportRow(detail.certificate)">导出</el-button>
          <el-button v-if="detail.certificate?.status==='PENDING'" type="success" @click="confirmApprove(detail.certificate)">通过</el-button>
          <el-button v-if="detail.certificate?.status==='PENDING'" type="danger" @click="openRejectDialog(detail.certificate)">拒绝</el-button>
        </template>
      </el-dialog>
    </template>
    <!-- 拒绝理由对话框 -->
    <el-dialog v-model="rejectDialog.visible" title="拒绝证明申请" width="480px">
      <el-form :model="rejectDialog.form" label-width="96px">
        <el-form-item label="拒绝理由">
          <el-input v-model="rejectDialog.form.reason" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入拒绝理由（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="submitReject">拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { certificateAPI, userAPI, serviceRecordAPI } from '@/api'
import { usePagination } from '@/composables/usePagination'
import { usePermission } from '@/composables/usePermission'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import { formatDateTime } from '@/utils/format'

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()
const { hasAnyRole } = usePermission()
const canAccess = computed(() => hasAnyRole(['TEACHER', 'ADMIN']))

const list = ref([])
const selectedRows = ref([])
const searchForm = reactive({ status: 'PENDING', userId: '' })

const statusLabel = (s) => STATUS_LABELS[s] || s
const statusType = (s) => STATUS_COLORS[s] || 'info'

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      status: searchForm.status || undefined,
      userId: searchForm.userId || undefined
    }
    const resp = await certificateAPI.getAllCertificates(params)
    list.value = resp.data?.records || []
    updatePagination(resp.data || { current: 1, size: params.size, total: 0, pages: 0 })
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchList()
}
const handleReset = () => {
  searchForm.status = 'PENDING'
  searchForm.userId = ''
  handleSearch()
}

// 详情
const detailDialog = reactive({ visible: false, loading: false })
const detail = reactive({ certificate: null, user: null, records: [] })

const viewDetail = async (row) => {
  detailDialog.visible = true
  detailDialog.loading = true
  detail.certificate = row
  detail.user = null
  detail.records = []
  try {
    if (row.userId) {
      const [userResp, recResp] = await Promise.all([
        userAPI.getUserById(row.userId),
        serviceRecordAPI.getByUser(row.userId, { current: 1, size: 10 })
      ])
      detail.user = userResp.data || null
      detail.records = recResp.data?.records || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    detailDialog.loading = false
  }
}

// 审核
const confirmApprove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认通过该证明申请（ID: ${row.id}）？`, '确认操作', { type: 'warning' })
    await certificateAPI.approveCertificate(row.id)
    ElMessage.success('已通过')
    detailDialog.visible = false
    fetchList()
  } catch (e) {
    // cancel or error
  }
}

const onSelectionChange = (rows) => {
  selectedRows.value = rows
}

const bulkApprove = async () => {
  if (selectedRows.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确认批量通过所选 ${selectedRows.value.length} 条申请？`, '确认操作', { type: 'warning' })
    const pending = selectedRows.value.filter(r => r.status === 'PENDING')
    let ok = 0, fail = 0
    for (const r of pending) {
      try {
        await certificateAPI.approveCertificate(r.id)
        ok++
      } catch { fail++ }
    }
    ElMessage.success(`批量通过完成：成功 ${ok} 条，失败 ${fail} 条`)
    fetchList()
  } catch {}
}

// 导出 CSV
const exportRow = (row) => {
  try {
    const headers = ['ID','学生','状态','申请时间','有效期至']
    const lines = [[row.id, row.userName || '', row.status, row.createdAt || '', row.validUntil || '']]
    const csv = [headers.join(','), ...lines.map(a => a.map(v => `"${(v ?? '').toString().replace(/"/g,'""')}"`).join(','))].join('\n')
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `certificate_${row.id}.csv`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

// 批量导出
const exportSelected = () => {
  try {
    if (selectedRows.value.length === 0) return
    const headers = ['ID','学生','状态','申请时间','有效期至']
    const lines = selectedRows.value.map(row => [row.id, row.userName || '', row.status, row.createdAt || '', row.validUntil || ''])
    const csv = [headers.join(','), ...lines.map(a => a.map(v => `"${(v ?? '').toString().replace(/"/g,'""')}"`).join(','))].join('\n')
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `certificates_${Date.now()}.csv`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

const rejectDialog = reactive({ visible: false, form: { reason: '' }, row: null })
const openRejectDialog = (row) => {
  rejectDialog.visible = true
  rejectDialog.row = row
  rejectDialog.form.reason = ''
}
const openBulkRejectDialog = () => {
  if (selectedRows.value.length === 0) return
  rejectDialog.visible = true
  rejectDialog.row = null
  rejectDialog.form.reason = ''
}
const submitReject = async () => {
  try {
    const reason = rejectDialog.form.reason?.trim()
    if (rejectDialog.row) {
      // single
      await certificateAPI.rejectCertificate(rejectDialog.row.id, reason)
      ElMessage.success('已拒绝')
    } else {
      // bulk
      const pending = selectedRows.value.filter(r => r.status === 'PENDING')
      let ok = 0, fail = 0
      for (const r of pending) {
        try {
          await certificateAPI.rejectCertificate(r.id, reason)
          ok++
        } catch { fail++ }
      }
      ElMessage.success(`批量拒绝完成：成功 ${ok} 条，失败 ${fail} 条`)
    }
    rejectDialog.visible = false
    detailDialog.visible = false
    fetchList()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.teacher-cert-approval {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
  .search-card { margin-bottom: 16px; }
  .pagination { margin-top: 12px; text-align: right; }
}
</style>

