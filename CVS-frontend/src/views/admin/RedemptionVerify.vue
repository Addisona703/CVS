<template>
  <div class="redemption-verify">
    <div class="page-header">
      <h1>兑换核销</h1>
    </div>
    
    <el-row :gutter="20">
      <!-- 扫码核销区域 -->
      <el-col :span="12">
        <el-card class="scan-card">
          <template #header>
            <div class="card-header">
              <span>扫码核销</span>
              <el-button 
                type="primary" 
                size="small" 
                @click="toggleCamera"
              >
                {{ cameraActive ? '关闭摄像头' : '开启摄像头' }}
              </el-button>
            </div>
          </template>
          
          <div class="scan-area">
            <div v-if="!cameraActive" class="camera-placeholder">
              <el-icon size="60"><Camera /></el-icon>
              <p>点击上方按钮开启摄像头进行扫码</p>
            </div>
            
            <div v-else class="camera-container">
              <qrcode-stream
                @decode="onDecode"
                @init="onInit"
                @error="onError"
              >
                <div class="scan-overlay">
                  <div class="scan-frame"></div>
                  <p class="scan-tip">请将二维码对准扫描框</p>
                </div>
              </qrcode-stream>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 手动输入区域 -->
      <el-col :span="12">
        <el-card class="manual-card">
          <template #header>
            <span>手动输入凭证</span>
          </template>
          
          <div class="manual-input">
            <el-form :model="manualForm" @submit.prevent="handleManualVerify">
              <el-form-item label="凭证编号">
                <el-input 
                  v-model="manualForm.voucherCode" 
                  placeholder="请输入凭证编号"
                  clearable
                  @keyup.enter="handleManualVerify"
                >
                  <template #append>
                    <el-button 
                      type="primary" 
                      @click="handleManualVerify"
                      :loading="verifying"
                    >
                      查询
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 核销记录 -->
    <el-card class="history-card">
      <template #header>
        <span>最近核销记录</span>
      </template>
      
      <el-table :data="recentVerifications" :loading="historyLoading" stripe>
        <el-table-column prop="voucherCode" label="凭证编号" width="150" />
        <el-table-column prop="userName" label="学生姓名" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="pointsSpent" label="消耗积分" width="100" />
        <el-table-column prop="verifiedAt" label="核销时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.verifiedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="verifiedByName" label="核销人员" width="120" />
      </el-table>
    </el-card>

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
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { QrcodeStream } from 'vue-qrcode-reader'
import { mallAPI } from '@/api'
import { formatDateTime } from '@/utils/format'

const cameraActive = ref(false)
const verifying = ref(false)
const confirming = ref(false)
const historyLoading = ref(false)
const confirmDialogVisible = ref(false)
const redemptionInfo = ref(null)
const recentVerifications = ref([])

const manualForm = reactive({
  voucherCode: ''
})

// 切换摄像头
const toggleCamera = () => {
  cameraActive.value = !cameraActive.value
}

// 二维码解码
const onDecode = (decodedString) => {
  console.log('扫码结果:', decodedString)
  handleVerifyVoucher(decodedString)
}

// 摄像头初始化
const onInit = async (promise) => {
  try {
    await promise
    console.log('摄像头初始化成功')
  } catch (error) {
    console.error('摄像头初始化失败:', error)
    ElMessage.error('摄像头初始化失败，请检查设备权限')
    cameraActive.value = false
  }
}

// 摄像头错误处理
const onError = (error) => {
  console.error('摄像头错误:', error)
  ElMessage.error('摄像头访问失败')
  cameraActive.value = false
}

// 手动输入核销
const handleManualVerify = () => {
  if (!manualForm.voucherCode.trim()) {
    ElMessage.warning('请输入凭证编号')
    return
  }
  handleVerifyVoucher(manualForm.voucherCode.trim())
}

// 处理凭证核销
const handleVerifyVoucher = async (voucherCode) => {
  if (verifying.value) return
  
  verifying.value = true
  try {
    // 先查询兑换记录信息
    const response = await mallAPI.getRedemptionByVoucher(voucherCode)
    if (response.code === 200) {
      redemptionInfo.value = response.data
      confirmDialogVisible.value = true
      
      // 关闭摄像头
      if (cameraActive.value) {
        cameraActive.value = false
      }
      
      // 清空手动输入
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
    fetchRecentVerifications()
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

// 获取最近核销记录
const fetchRecentVerifications = async () => {
  historyLoading.value = true
  try {
    const response = await mallAPI.getAllRedemptions({
      status: 1, // 已核销
      pageSize: 10,
      pageNum: 1
    })
    if (response.code === 200) {
      recentVerifications.value = response.data.records
    }
  } catch (error) {
    console.error('获取核销记录失败:', error)
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
  fetchRecentVerifications()
})

onUnmounted(() => {
  // 组件销毁时关闭摄像头
  if (cameraActive.value) {
    cameraActive.value = false
  }
})
</script>

<style lang="scss" scoped>
.redemption-verify {
  .page-header {
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }
  
  .scan-card, .manual-card {
    height: 400px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .scan-area {
    height: 320px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .camera-placeholder {
      text-align: center;
      color: #909399;
      
      p {
        margin-top: 10px;
        font-size: 14px;
      }
    }
    
    .camera-container {
      width: 100%;
      height: 100%;
      position: relative;
      
      .scan-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        
        .scan-frame {
          width: 200px;
          height: 200px;
          border: 2px solid #409eff;
          border-radius: 8px;
          position: relative;
          
          &::before,
          &::after {
            content: '';
            position: absolute;
            width: 20px;
            height: 20px;
            border: 3px solid #409eff;
          }
          
          &::before {
            top: -3px;
            left: -3px;
            border-right: none;
            border-bottom: none;
          }
          
          &::after {
            bottom: -3px;
            right: -3px;
            border-left: none;
            border-top: none;
          }
        }
        
        .scan-tip {
          margin-top: 20px;
          color: #409eff;
          font-size: 14px;
          background: rgba(0, 0, 0, 0.7);
          padding: 5px 10px;
          border-radius: 4px;
        }
      }
    }
  }
  
  .manual-input {
    padding: 20px 0;
  }
  
  .history-card {
    margin-top: 20px;
  }
  
  .confirm-content {
    .status-warning {
      margin-top: 15px;
    }
  }
}
</style>