<template>
  <div class="redemption-verify">
    <div class="page-header">
      <h1>兑换核销</h1>
    </div>
    
    <!-- 选项卡 -->
    <el-tabs v-model="activeTab" class="verify-tabs">
      <!-- 凭证核销选项卡 -->
      <el-tab-pane label="凭证核销" name="verify">
        <el-card class="verify-card">
          <el-form :model="manualForm" @submit.prevent="handleManualVerify">
            <el-form-item label="凭证编号">
              <el-input 
                v-model="manualForm.voucherCode" 
                placeholder="请输入凭证编号，例如：MALL202511090921305783186"
                clearable
                size="large"
                @keyup.enter="handleManualVerify"
              >
                <template #prefix>
                  <el-icon><Ticket /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                size="large"
                @click="handleManualVerify"
                :loading="verifying"
                class="verify-button"
              >
                <el-icon v-if="!verifying"><Search /></el-icon>
                查询并核销
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 核销流程说明 -->
        <div class="verify-tips">
          <el-icon><InfoFilled /></el-icon>
          <div class="tips-content">
            <p><strong>核销流程：</strong></p>
            <p>1. 请学生出示兑换凭证编号</p>
            <p>2. 在上方输入框中输入完整的凭证编号</p>
            <p>3. 点击"查询并核销"按钮，确认信息后完成核销</p>
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 核销记录选项卡 -->
      <el-tab-pane label="核销记录" name="history">
        <el-card class="history-card">
          <div class="history-header">
            <el-radio-group v-model="statusType" @change="fetchVerifications">
              <el-radio-button label="TODAY">今日核销</el-radio-button>
              <el-radio-button label="VERIFIED">累计核销</el-radio-button>
              <el-radio-button label="PENDING">待核销</el-radio-button>
            </el-radio-group>
          </div>
          
          <el-table :data="verifications" :loading="historyLoading" stripe>
            <el-table-column prop="voucherCode" label="凭证编号" width="200" />
            <el-table-column prop="userName" label="学生姓名" width="120" />
            <el-table-column prop="productName" label="商品名称" min-width="180" />
            <el-table-column prop="pointsSpent" label="消耗积分" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="warning">{{ row.pointsSpent }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="verifiedAt" label="核销时间" width="180">
              <template #default="{ row }">
                {{ row.verifiedAt ? formatDateTime(row.verifiedAt) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="verifiedByName" label="核销人员" width="120">
              <template #default="{ row }">
                {{ row.verifiedByName || '-' }}
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-if="pagination.total > 0"
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchVerifications"
            @current-change="fetchVerifications"
            class="pagination"
          />
          
          <el-empty v-if="!verifications.length && !historyLoading" description="暂无核销记录" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 核销确认对话框 -->
    <el-dialog 
      v-model="confirmDialogVisible" 
      title="确认核销"
      width="500px"
    >
      <div v-if="redemptionInfo" class="confirm-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="凭证编号">
            {{ redemptionInfo.voucherCode }}
          </el-descriptions-item>
          <el-descriptions-item label="学生姓名">
            {{ redemptionInfo.userName }}
          </el-descriptions-item>
          <el-descriptions-item label="学生学号">
            {{ redemptionInfo.userStudentId }}
          </el-descriptions-item>
          <el-descriptions-item label="商品名称">
            {{ redemptionInfo.productName }}
          </el-descriptions-item>
          <el-descriptions-item label="消耗积分">
            <el-tag type="warning">{{ redemptionInfo.pointsSpent }}积分</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="兑换时间">
            {{ formatDateTime(redemptionInfo.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="兑换状态">
            <el-tag :type="getStatusType(redemptionInfo.status)">
              {{ getStatusText(redemptionInfo.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="redemptionInfo.status !== 0" class="status-warning">
          <el-alert
            :title="redemptionInfo.status === 1 ? '该凭证已被核销' : '该兑换已被取消'"
            :type="redemptionInfo.status === 1 ? 'warning' : 'error'"
            :closable="false"
            show-icon
          />
        </div>
      </div>
      
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button 
          v-if="redemptionInfo?.status === 0"
          type="primary" 
          @click="confirmVerify"
          :loading="confirming"
        >
          确认核销
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Tickets, Ticket, Search, InfoFilled } from '@element-plus/icons-vue'
import { mallAPI } from '@/api'
import { formatDateTime } from '@/utils/format'

const activeTab = ref('verify')
const statusType = ref('VERIFIED')
const verifying = ref(false)
const confirming = ref(false)
const historyLoading = ref(false)
const confirmDialogVisible = ref(false)
const redemptionInfo = ref(null)
const verifications = ref([])

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const manualForm = reactive({
  voucherCode: ''
})

// 手动输入核销
const handleManualVerify = async () => {
  if (!manualForm.voucherCode.trim()) {
    ElMessage.warning('请输入凭证编号')
    return
  }
  
  if (verifying.value) return
  
  verifying.value = true
  try {
    // 查询兑换记录信息
    const response = await mallAPI.getRedemptionByVoucher(manualForm.voucherCode.trim())
    if (response.code === 200) {
      redemptionInfo.value = response.data
      confirmDialogVisible.value = true
      
      // 清空输入
      manualForm.voucherCode = ''
    }
  } catch (error) {
    console.error('查询凭证失败:', error)
    if (error.response?.status === 404) {
      ElMessage.error('凭证不存在，请检查凭证编号')
    } else {
      ElMessage.error('查询凭证失败')
    }
  } finally {
    verifying.value = false
  }
}

// 确认核销
const confirmVerify = async () => {
  if (!redemptionInfo.value || confirming.value) return
  
  confirming.value = true
  try {
    await mallAPI.verifyRedemption({
      voucherCode: redemptionInfo.value.voucherCode
    })
    
    ElMessage.success('核销成功')
    confirmDialogVisible.value = false
    redemptionInfo.value = null
    
    // 刷新核销记录
    fetchVerifications()
  } catch (error) {
    console.error('核销失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('核销失败')
    }
  } finally {
    confirming.value = false
  }
}

// 获取核销记录
const fetchVerifications = async () => {
  historyLoading.value = true
  try {
    const response = await mallAPI.getRedemptionsByStatus({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      params: {}
    }, statusType.value)
    
    if (response.code === 200) {
      verifications.value = response.data.records || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    console.error('获取核销记录失败:', error)
    ElMessage.error('获取核销记录失败')
  } finally {
    historyLoading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',  // 待领取
    1: 'success',  // 已领取
    2: 'danger'    // 已取消
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '待领取',
    1: '已领取',
    2: '已取消'
  }
  return textMap[status] || '未知'
}

onMounted(() => {
  fetchVerifications()
})
</script>

<style lang="scss" scoped>
.redemption-verify {
  .page-header {
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .verify-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 20px;
    }
    
    :deep(.el-tabs__item) {
      font-size: 15px;
      font-weight: 500;
      padding: 0 20px;
      height: 44px;
      line-height: 44px;
    }
    
    :deep(.el-tabs__active-bar) {
      height: 3px;
    }
  }
  
  .verify-card {
    max-width: 700px;
    margin: 0 auto;
    border: none;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    
    :deep(.el-card__body) {
      padding: 32px;
    }
    
    :deep(.el-form-item__label) {
      font-size: 15px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 8px;
    }
    
    :deep(.el-input) {
      .el-input__wrapper {
        padding: 12px 16px;
        box-shadow: 0 0 0 1px #dcdfe6 inset;
        transition: all 0.3s;
        
        &:hover {
          box-shadow: 0 0 0 1px #c0c4cc inset;
        }
        
        &.is-focus {
          box-shadow: 0 0 0 1px #409eff inset;
        }
      }
      
      .el-input__prefix {
        .el-icon {
          color: #909399;
          font-size: 18px;
        }
      }
    }
    
    .verify-button {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
      border: none;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
      
      .el-icon {
        margin-right: 8px;
      }
      
      &:hover {
        background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
        box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
        transform: translateY(-2px);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
  }
  
  .verify-tips {
    max-width: 700px;
    margin: 20px auto 0;
    padding: 16px 20px;
    background: #f4f4f5;
    border-radius: 8px;
    display: flex;
    gap: 12px;
    
    > .el-icon {
      flex-shrink: 0;
      font-size: 20px;
      color: #409eff;
      margin-top: 2px;
    }
    
    .tips-content {
      flex: 1;
      
      p {
        margin: 0 0 6px 0;
        font-size: 13px;
        line-height: 1.6;
        color: #606266;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        strong {
          color: #303133;
          font-weight: 600;
        }
      }
    }
  }
  
  .history-card {
    border: none;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    
    :deep(.el-card__body) {
      padding: 24px;
    }
    
    .history-header {
      margin-bottom: 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      :deep(.el-radio-group) {
        .el-radio-button__inner {
          padding: 10px 20px;
          font-weight: 500;
        }
      }
    }
    
    :deep(.el-table) {
      font-size: 14px;
      
      th {
        background-color: #fafafa;
        color: #606266;
        font-weight: 600;
      }
      
      .el-table__empty-block {
        padding: 60px 0;
      }
    }
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
  
  .confirm-content {
    .status-warning {
      margin-top: 15px;
    }
  }
}
</style>