<template>
  <div class="certificates">
    <div class="page-header">
      <h1>志愿证明</h1>
      <el-button type="primary" @click="showApplyDialog = true">
        <el-icon><Plus /></el-icon>
        申请证明
      </el-button>
    </div>
    
    <!-- 申请须知 -->
    <el-alert
      title="申请须知"
      type="info"
      :closable="false"
      show-icon
    >
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
      <el-card 
        v-for="certificate in certificateList" 
        :key="certificate.id" 
        class="certificate-card"
        shadow="hover"
      >
        <div class="certificate-header">
          <h3 class="certificate-title">{{ certificate.title }}</h3>
          <el-tag :type="getStatusType(certificate.status)">
            {{ getStatusLabel(certificate.status) }}
          </el-tag>
        </div>
        
        <div class="certificate-content">
          <div class="certificate-info">
            <div class="info-item">
              <span class="label">申请时间：</span>
              <span>{{ formatDateTime(certificate.applyTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">服务时长：</span>
              <span>{{ certificate.totalHours }}小时</span>
            </div>
            <div class="info-item">
              <span class="label">服务次数：</span>
              <span>{{ certificate.activityCount }}次</span>
            </div>
            <div class="info-item" v-if="certificate.status === 'APPROVED'">
              <span class="label">审核时间：</span>
              <span>{{ formatDateTime(certificate.approveTime) }}</span>
            </div>
            <div class="info-item" v-if="certificate.status === 'APPROVED'">
              <span class="label">审核人：</span>
              <span>{{ certificate.approver }}</span>
            </div>
          </div>
          
          <div class="certificate-description">
            <span class="label">申请说明：</span>
            <p>{{ certificate.description }}</p>
          </div>
          
          <div class="rejection-reason" v-if="certificate.status === 'REJECTED' && certificate.rejectionReason">
            <el-alert
              :title="certificate.rejectionReason"
              type="error"
              :closable="false"
              show-icon
            />
          </div>
        </div>
        
        <div class="certificate-actions">
          <el-button 
            v-if="certificate.status === 'APPROVED'"
            type="primary" 
            @click="downloadCertificate(certificate)"
          >
            <el-icon><Download /></el-icon>
            下载证明
          </el-button>
          <el-button 
            v-if="certificate.status === 'PENDING'"
            type="danger" 
            @click="cancelApplication(certificate)"
          >
            取消申请
          </el-button>
          <el-button type="info" @click="viewDetail(certificate)">
            查看详情
          </el-button>
        </div>
      </el-card>
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="certificateList.length > 0">
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
    <el-empty v-if="!loading && certificateList.length === 0" description="暂无证明申请" />
    
    <!-- 申请证明对话框 -->
    <el-dialog
      v-model="showApplyDialog"
      title="申请志愿证明"
      width="600px"
    >
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="证明标题" prop="title">
          <el-input v-model="applyForm.title" placeholder="请输入证明标题" />
        </el-form-item>
        <el-form-item label="申请说明" prop="description">
          <el-input
            v-model="applyForm.description"
            type="textarea"
            :rows="4"
            placeholder="请说明申请证明的用途和要求"
          />
        </el-form-item>
        <el-form-item label="包含活动">
          <el-checkbox-group v-model="applyForm.activityIds">
            <el-checkbox 
              v-for="record in serviceRecords" 
              :key="record.id" 
              :label="record.id"
            >
              {{ record.activityTitle }} ({{ record.serviceHours }}小时)
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleApply">
          提交申请
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="证明详情"
      width="700px"
    >
      <div v-if="selectedCertificate" class="certificate-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="证明标题">
            {{ selectedCertificate.title }}
          </el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="getStatusType(selectedCertificate.status)">
              {{ getStatusLabel(selectedCertificate.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ formatDateTime(selectedCertificate.applyTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务时长">
            {{ selectedCertificate.totalHours }}小时
          </el-descriptions-item>
          <el-descriptions-item label="服务次数">
            {{ selectedCertificate.activityCount }}次
          </el-descriptions-item>
          <el-descriptions-item label="审核人" v-if="selectedCertificate.approver">
            {{ selectedCertificate.approver }}
          </el-descriptions-item>
          <el-descriptions-item label="申请说明" :span="2">
            {{ selectedCertificate.description }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="included-activities" v-if="selectedCertificate.activities">
          <h4>包含的服务活动</h4>
          <el-table :data="selectedCertificate.activities" stripe>
            <el-table-column prop="title" label="活动名称" />
            <el-table-column prop="serviceDate" label="服务日期" width="120" />
            <el-table-column prop="serviceHours" label="服务时长" width="100">
              <template #default="{ row }">
                {{ row.serviceHours }}小时
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'
import { certificateAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import { usePagination } from '@/composables/usePagination'

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const certificateList = ref([])
const serviceRecords = ref([])
const statusFilter = ref('')
const showApplyDialog = ref(false)
const showDetailDialog = ref(false)
const selectedCertificate = ref(null)
const submitting = ref(false)
const applyFormRef = ref()

const applyForm = reactive({
  title: '',
  description: '',
  activityIds: []
})

const applyRules = {
  title: [
    { required: true, message: '请输入证明标题', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入申请说明', trigger: 'blur' }
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
      current: pagination.current,
      size: pagination.size,
      status: statusFilter.value || undefined
    }
    // 这里应该调用获取用户证明列表的API
    // const response = await certificateAPI.getUserCertificates(userId, params)
    
    // 使用模拟数据
    certificateList.value = [
      {
        id: 1,
        title: '2024年上半年志愿服务证明',
        status: 'APPROVED',
        applyTime: '2024-01-25T10:00:00',
        approveTime: '2024-01-26T15:30:00',
        approver: '张老师',
        totalHours: 15,
        activityCount: 3,
        description: '申请用于奖学金评定',
        activities: [
          { title: '社区环保志愿活动', serviceDate: '2024-01-20', serviceHours: 3 },
          { title: '敬老院志愿服务', serviceDate: '2024-01-15', serviceHours: 4 },
          { title: '图书馆志愿服务', serviceDate: '2024-01-10', serviceHours: 2 }
        ]
      },
      {
        id: 2,
        title: '社会实践证明',
        status: 'PENDING',
        applyTime: '2024-01-28T14:20:00',
        totalHours: 8,
        activityCount: 2,
        description: '申请用于社会实践学分认定'
      },
      {
        id: 3,
        title: '志愿服务时长证明',
        status: 'REJECTED',
        applyTime: '2024-01-20T09:15:00',
        totalHours: 2,
        activityCount: 1,
        description: '申请用于课外活动学分',
        rejectionReason: '服务时长不足，建议累计更多服务时长后再申请'
      }
    ]
    
    updatePagination({
      current: 1,
      size: 10,
      total: 3,
      pages: 1
    })
  } catch (error) {
    console.error('获取证明列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchServiceRecords = async () => {
  try {
    // 这里应该调用获取用户服务记录的API
    // const response = await recordAPI.getUserRecords(userId)
    
    // 使用模拟数据
    serviceRecords.value = [
      { id: 1, activityTitle: '社区环保志愿活动', serviceHours: 3 },
      { id: 2, activityTitle: '敬老院志愿服务', serviceHours: 4 },
      { id: 3, activityTitle: '图书馆志愿服务', serviceHours: 2 }
    ]
  } catch (error) {
    console.error('获取服务记录失败:', error)
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

const downloadCertificate = (certificate) => {
  // 这里应该调用下载证明的API
  ElMessage.success('证明下载成功')
}

const cancelApplication = async (certificate) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消申请 "${certificate.title}" 吗？`,
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里应该调用取消申请的API
    ElMessage.success('取消申请成功')
    fetchCertificateList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消申请失败:', error)
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
    title: '',
    description: '',
    activityIds: []
  })
  applyFormRef.value?.resetFields()
}

onMounted(() => {
  fetchServiceRecords()
  fetchCertificateList()
})
</script>

<style lang="scss" scoped>
.certificates {
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
  
  .notice-list {
    margin: 0;
    padding-left: 20px;
    
    li {
      margin-bottom: 4px;
      color: #606266;
    }
  }
  
  .filter-card {
    margin: 20px 0;
  }
  
  .certificate-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-bottom: 20px;
    
    .certificate-card {
      .certificate-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 16px;
        
        .certificate-title {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #303133;
          flex: 1;
          margin-right: 12px;
        }
      }
      
      .certificate-content {
        .certificate-info {
          margin-bottom: 16px;
          
          .info-item {
            display: flex;
            margin-bottom: 8px;
            font-size: 14px;
            
            .label {
              color: #909399;
              min-width: 80px;
            }
          }
        }
        
        .certificate-description {
          margin-bottom: 16px;
          
          .label {
            color: #909399;
            font-size: 14px;
          }
          
          p {
            margin: 8px 0 0 0;
            color: #606266;
            line-height: 1.6;
          }
        }
        
        .rejection-reason {
          margin-bottom: 16px;
        }
      }
      
      .certificate-actions {
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
