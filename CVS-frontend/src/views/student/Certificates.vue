<template>
  <div class="certificates">
    <div class="page-header">
      <h1>志愿证明</h1>
      <el-button type="primary" @click="showApplyDialog = true">
        <el-icon>
          <Plus />
        </el-icon>
        申请证明
      </el-button>
    </div>

    <!-- 申请须知 -->
    <el-alert title="申请须知" type="info" :closable="false" show-icon>
      <template #default>
        <ul class="notice-list">
          <li>志愿证明用于证明您参与志愿服务的经历和时长</li>
          <li>申请前请确保您的服务记录真实有效</li>
          <li>证明审核通过后可下载电子版证明文件</li>
          <li>如有疑问请联系相关老师</li>
        </ul>
      </template>
    </el-alert>

    <!-- 状态筛选 -->
    <el-card class="filter-card">
      <el-radio-group v-model="statusFilter" @change="handleFilterChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="PENDING">待审核</el-radio-button>
        <el-radio-button label="APPROVED">已通过</el-radio-button>
        <el-radio-button label="REJECTED">已拒绝</el-radio-button>
      </el-radio-group>
    </el-card>

    <!-- 证明列表 -->
    <div class="certificate-list">
      <el-card v-for="certificate in certificateList" :key="certificate.id" class="certificate-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <h3 class="certificate-title">志愿服务证明</h3>
              <span class="certificate-subtitle">
                服务时间：{{ certificate.startDate }} 至 {{ certificate.endDate }}
              </span>
            </div>
            <el-tag :type="getStatusType(certificate.status)" size="large">
              {{ getStatusLabel(certificate.status) }}
            </el-tag>
          </div>
        </template>

        <div class="info-row">
          <div class="info-item">
            <span class="label">申请时间：</span>
            <span>{{ formatDateTime(certificate.createdAt) }}</span>
          </div>
          <div class="info-item" v-if="certificate.certificateNumber">
            <span class="label">证明编号：</span>
            <span>{{ certificate.certificateNumber }}</span>
          </div>
          <div class="info-item" v-if="certificate.status === 'APPROVED'">
            <span class="label">审核时间：</span>
            <span>{{ formatDateTime(certificate.approvedAt) }}</span>
          </div>
          <div class="info-item" v-if="certificate.status === 'APPROVED'">
            <span class="label">审核人：</span>
            <span>{{ certificate.approverName }}</span>
          </div>
        </div>

        <div class="certificate-description">
          <span class="label">申请目的：</span>
          <p>{{ certificate.purpose }}</p>
        </div>

        <div class="rejection-reason" v-if="certificate.status === 'REJECTED' && certificate.rejectReason">
          <el-alert :title="certificate.rejectReason" type="error" :closable="false" show-icon />
        </div>

        <template #footer>
          <div class="card-actions">
            <a v-if="certificate.status === 'APPROVED'" href="javascript:void(0)"
              @click="previewCertificate(certificate)">
              预览证书
            </a>
            <a v-if="certificate.status === 'APPROVED'" href="javascript:void(0)"
              @click="downloadCertificate(certificate)">
              下载证书
            </a>
            <a v-if="certificate.status === 'PENDING'" href="javascript:void(0)" class="danger"
              @click="cancelApplication(certificate)">
              删除申请
            </a>
            <a href="javascript:void(0)" @click="viewDetail(certificate)">
              查看详情
            </a>
          </div>
        </template>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="certificateList.length > 0">
      <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size"
        :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && certificateList.length === 0" description="暂无证明申请" />

    <!-- 申请证明对话框 -->
    <el-dialog v-model="showApplyDialog" title="申请志愿证明" width="600px" :close-on-click-modal="false">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px" size="large">
        <el-form-item label="申请目的" prop="purpose">
          <el-input v-model="applyForm.purpose" type="textarea" :rows="3" placeholder="请说明申请证明的用途" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="applyForm.startDate" type="date" placeholder="选择开始日期" format="YYYY-MM-DD"
                value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="applyForm.endDate" type="date" placeholder="选择结束日期" format="YYYY-MM-DD"
                value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleApply">
          提交申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="证明详情" width="700px">
      <div v-if="selectedCertificate" class="certificate-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="证明编号" v-if="selectedCertificate.certificateNumber">
            {{ selectedCertificate.certificateNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="getStatusType(selectedCertificate.status)">
              {{ getStatusLabel(selectedCertificate.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ formatDateTime(selectedCertificate.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务开始日期">
            {{ selectedCertificate.startDate }}
          </el-descriptions-item>
          <el-descriptions-item label="服务结束日期">
            {{ selectedCertificate.endDate }}
          </el-descriptions-item>
          <el-descriptions-item label="审核人" v-if="selectedCertificate.approverName">
            {{ selectedCertificate.approverName }}
          </el-descriptions-item>
          <el-descriptions-item label="审核时间" v-if="selectedCertificate.approvedAt">
            {{ formatDateTime(selectedCertificate.approvedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="申请目的" :span="2">
            {{ selectedCertificate.purpose }}
          </el-descriptions-item>
          <el-descriptions-item label="拒绝原因" :span="2" v-if="selectedCertificate.rejectReason">
            {{ selectedCertificate.rejectReason }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'
import { certificateAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import { usePagination } from '@/composables/usePagination'

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const certificateList = ref([])
const statusFilter = ref('')
const showApplyDialog = ref(false)
const showDetailDialog = ref(false)
const selectedCertificate = ref(null)
const submitting = ref(false)
const applyFormRef = ref()

const applyForm = reactive({
  purpose: '',
  startDate: '',
  endDate: ''
})

const applyRules = {
  purpose: [
    { required: true, message: '请输入申请目的', trigger: 'blur' },
    { min: 5, message: '申请目的不能少于5个字符', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ]
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

const fetchCertificateList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size
    }

    const response = await certificateAPI.getMyCertificates(params)
    if (response.code === 200) {
      const data = response.data
      certificateList.value = data.records || []

      // 根据状态筛选
      if (statusFilter.value) {
        certificateList.value = certificateList.value.filter(cert => cert.status === statusFilter.value)
      }

      updatePagination({
        current: data.current || pagination.current,
        size: data.size || pagination.size,
        total: data.total || 0,
        pages: data.pages || 0
      })
    } else {
      ElMessage.error(response.message || '获取证明列表失败')
      certificateList.value = []
    }
  } catch (error) {
    console.error('获取证明列表失败:', error)
    ElMessage.error('网络错误，请稍后重试')
    certificateList.value = []
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



const handleFilterChange = () => {
  pagination.current = 1
  fetchCertificateList()
}

const viewDetail = (certificate) => {
  selectedCertificate.value = certificate
  showDetailDialog.value = true
}

const previewCertificate = async (certificate) => {
  // 显示加载提示
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在生成证书预览...',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  
  try {
    const response = await certificateAPI.previewCertificate(certificate.id)
    
    // 创建Blob对象并在新窗口打开
    const blob = new Blob([response], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    window.open(url, '_blank')
    
    // 延迟清理URL，确保PDF已加载
    setTimeout(() => {
      window.URL.revokeObjectURL(url)
    }, 100)
    
    ElMessage.success('证书预览已打开')
  } catch (error) {
    console.error('预览证书失败:', error)
    ElMessage.error('预览失败，请稍后重试')
  } finally {
    loadingInstance.close()
  }
}

const downloadCertificate = async (certificate) => {
  // 显示加载提示
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在生成证书文件...',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  
  try {
    const response = await certificateAPI.downloadCertificate(certificate.id)
    
    // 创建Blob对象
    const blob = new Blob([response], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    
    // 创建临时a标签触发下载
    const link = document.createElement('a')
    link.href = url
    link.download = `志愿服务证书_${certificate.username || certificate.id}.pdf`
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('证书下载成功')
  } catch (error) {
    // 检查是否是IDM拦截（204状态码或ERR_NETWORK）
    if (error.code === 'ERR_NETWORK' || error.response?.status === 204) {
      // IDM已经处理下载，不显示错误
      console.log('下载已被下载管理器接管')
      ElMessage.success('证书下载已开始')
    } else {
      console.error('下载证书失败:', error)
      ElMessage.error('下载失败，请稍后重试')
    }
  } finally {
    loadingInstance.close()
  }
}

const cancelApplication = async (certificate) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条证明申请吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await certificateAPI.deleteCertificate(certificate.id)
    ElMessage.success('删除成功')
    fetchCertificateList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败，请稍后重试')
    }
  }
}

const handleApply = async () => {
  try {
    await applyFormRef.value.validate()
    submitting.value = true

    const applyData = {
      ...applyForm
    }

    await certificateAPI.applyCertificate(applyData)
    ElMessage.success('申请提交成功，请等待审核')

    showApplyDialog.value = false
    resetApplyForm()
    fetchCertificateList()
  } catch (error) {
    console.error('申请失败:', error)
  } finally {
    submitting.value = false
  }
}

const resetApplyForm = () => {
  Object.assign(applyForm, {
    purpose: '',
    startDate: '',
    endDate: ''
  })
  applyFormRef.value?.resetFields()
}

onMounted(() => {
  fetchCertificateList()
})
</script>

<style lang="scss" scoped>
.certificates {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h1 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
    }
  }

  .notice-list {
    margin: 0;
    padding-left: 20px;

    li {
      margin-bottom: 6px;
      color: #64748b;
      line-height: 1.6;
    }
  }

  .filter-card {
    margin: 20px 0;

    :deep(.el-card__body) {
      padding: 16px 20px;
    }
  }

  .certificate-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
    gap: 20px;
    margin-bottom: 20px;

    .certificate-card {
      display: flex;
      flex-direction: column;
      transition: all 0.3s ease;
      border-radius: 12px;
      overflow: hidden;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      :deep(.el-card__header) {
        padding: 20px;
        border-bottom: 1px solid #e5e7eb;
        flex-shrink: 0;
        background: linear-gradient(135deg, rgba(59, 130, 246, 0.03), rgba(79, 70, 229, 0.03));
      }

      :deep(.el-card__body) {
        padding: 20px;
        flex-grow: 1;
      }

      :deep(.el-card__footer) {
        padding: 16px 20px;
        border-top: 1px solid #e5e7eb;
        background-color: #f8fafc;
        flex-shrink: 0;
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;

        .header-left {
          flex: 1;
          margin-right: 16px;

          .certificate-title {
            margin: 0 0 8px 0;
            font-size: 18px;
            font-weight: 600;
            color: #1f2937;
            line-height: 1.4;
          }

          .certificate-subtitle {
            font-size: 13px;
            color: #64748b;
          }
        }
      }

      .info-row {
        display: flex;
        flex-direction: column;
        gap: 10px;
        margin-bottom: 16px;

        .info-item {
          display: flex;
          font-size: 14px;
          line-height: 1.5;

          .label {
            color: #64748b;
            min-width: 90px;
            font-weight: 500;
          }

          span:not(.label) {
            color: #475569;
          }
        }
      }

      .certificate-description {
        margin-bottom: 16px;
        padding: 12px;
        background: #f8fafc;
        border-radius: 8px;

        .label {
          color: #64748b;
          font-size: 13px;
          font-weight: 500;
        }

        p {
          margin: 8px 0 0 0;
          color: #475569;
          line-height: 1.6;
        }
      }

      .rejection-reason {
        margin-top: 16px;
      }

      .card-actions {
        display: flex;
        gap: 20px;
        align-items: center;

        a {
          color: #3b82f6;
          text-decoration: none;
          font-size: 14px;
          font-weight: 500;
          transition: all 0.2s;

          &:hover {
            color: #2563eb;
            transform: translateY(-1px);
          }

          &.danger {
            color: #ef4444;

            &:hover {
              color: #dc2626;
            }
          }
        }
      }
    }
  }

  .pagination {
    text-align: center;
    margin-top: 20px;
  }

  .certificate-detail {
    .included-activities {
      margin-top: 20px;

      h4 {
        margin: 0 0 16px 0;
        font-size: 16px;
        color: #303133;
      }
    }
  }
}

@media (max-width: 768px) {
  .certificates {
    .certificate-actions {
      flex-direction: column;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style>
