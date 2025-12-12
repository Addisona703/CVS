<template>
  <div class="voucher-detail">
    <div class="page-header">
      <el-button 
        type="text" 
        @click="goBack"
        class="back-button">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1>兑换凭证</h1>
    </div>

    <div v-loading="loading" class="voucher-container">
      <div v-if="redemption" class="voucher-content">
        <!-- 状态标题 -->
        <div class="status-header">
          <el-tag 
            :type="getStatusType(redemption.status)"
            size="large"
            class="status-tag">
            {{ getStatusText(redemption.status) }}
          </el-tag>
          <div class="redemption-time">
            兑换时间：{{ formatDate(redemption.createdAt) }}
          </div>
        </div>

        <!-- 二维码区域 - 仅待领取状态显示 -->
        <el-card 
          v-if="redemption.status === 0" 
          class="qr-card"
          shadow="hover">
          <template #header>
            <div class="qr-header">
              <el-icon><Postcard /></el-icon>
              <span>扫码领取</span>
            </div>
          </template>
          
          <div class="qr-container">
            <QrDisplay 
              :value="String(redemption.voucherCode || redemption.id)"
              :size="200"
              :caption="`凭证编号：${redemption.voucherCode || redemption.id}`" />
            
            <div class="qr-instructions">
              <el-alert
                title="使用说明"
                type="info"
                :closable="false"
                show-icon>
                <p>请将此二维码出示给学工处工作人员进行扫码核销</p>
                <p>或提供凭证编号进行手动核销</p>
              </el-alert>
            </div>
          </div>
        </el-card>

        <!-- 商品信息 -->
        <el-card class="product-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Box /></el-icon>
              <span>商品信息</span>
            </div>
          </template>
          
          <div class="product-info">
            <div class="product-image-container">
              <img 
                :src="getProductImageUrl(redemption.productImageUrl)" 
                :alt="redemption.productName"
                class="product-image"
                @error="handleImageError" />
            </div>
            <div class="product-details">
              <h3 class="product-name">{{ redemption.productName }}</h3>
              <div class="product-meta">
                <div class="meta-item">
                  <el-icon color="#e6a23c"><Star /></el-icon>
                  <span>消耗积分：{{ redemption.pointsSpent }}</span>
                </div>
                <div class="meta-item">
                  <el-icon color="#67c23a"><Ticket /></el-icon>
                  <span>凭证编号：{{ redemption.voucherCode }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 领取信息 -->
        <el-card class="pickup-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Location /></el-icon>
              <span>领取信息</span>
            </div>
          </template>
          
          <!-- 待领取状态 - 显示领取说明 -->
          <div v-if="redemption.status === 0" class="pickup-instructions">
            <div class="instruction-item">
              <div class="instruction-icon">
                <el-icon color="#409eff"><OfficeBuilding /></el-icon>
              </div>
              <div class="instruction-content">
                <h4>领取地点</h4>
                <p>行政楼201室 - 学工处办公室</p>
              </div>
            </div>
            
            <div class="instruction-item">
              <div class="instruction-icon">
                <el-icon color="#67c23a"><Clock /></el-icon>
              </div>
              <div class="instruction-content">
                <h4>领取时间</h4>
                <p>周一至周五 9:00-17:00</p>
              </div>
            </div>
            
            <div class="instruction-item">
              <div class="instruction-icon">
                <el-icon color="#e6a23c"><Warning /></el-icon>
              </div>
              <div class="instruction-content">
                <h4>注意事项</h4>
                <p>请携带学生证，出示此凭证进行核销</p>
              </div>
            </div>
          </div>

          <!-- 已领取状态 - 显示核销信息 -->
          <div v-else-if="redemption.status === 1" class="verified-info">
            <el-result
              icon="success"
              title="商品已领取"
              sub-title="感谢您的参与！">
              <template #extra>
                <div class="verify-details">
                  <div class="verify-item">
                    <el-icon><Clock /></el-icon>
                    <span>领取时间：{{ formatDate(redemption.verifiedAt) }}</span>
                  </div>
                  <div v-if="redemption.verifiedByName" class="verify-item">
                    <el-icon><User /></el-icon>
                    <span>核销人员：{{ redemption.verifiedByName }}</span>
                  </div>
                </div>
              </template>
            </el-result>
          </div>

          <!-- 已取消状态 -->
          <div v-else-if="redemption.status === 2" class="cancelled-info">
            <el-result
              icon="info"
              title="兑换已取消"
              sub-title="积分已退还到您的账户">
              <template #extra>
                <el-button type="primary" @click="goToMall">
                  重新兑换
                </el-button>
              </template>
            </el-result>
          </div>
        </el-card>

        <!-- 操作按钮 -->
        <div class="action-buttons" v-if="redemption.status === 0">
          <el-button 
            type="danger" 
            size="large"
            @click="handleCancelRedemption">
            <el-icon><Close /></el-icon>
            取消兑换
          </el-button>
          
          <el-button 
            type="primary" 
            size="large"
            @click="goToMall">
            <el-icon><ShoppingCart /></el-icon>
            继续兑换
          </el-button>
        </div>
      </div>

      <!-- 错误状态 -->
      <el-empty 
        v-else-if="!loading" 
        description="兑换记录不存在或已被删除"
        :image-size="120">
        <el-button type="primary" @click="goBack">
          返回上一页
        </el-button>
      </el-empty>
    </div>

    <!-- 取消兑换确认对话框 -->
    <el-dialog 
      v-model="showCancelDialog" 
      title="确认取消兑换"
      width="400px">
      <div class="cancel-dialog-content">
        <el-alert
          title="取消兑换说明"
          type="warning"
          :closable="false"
          show-icon>
          <p>取消兑换后，消耗的积分将退还到您的账户</p>
          <p>商品库存也将相应恢复</p>
        </el-alert>
        
        <div class="redemption-summary">
          <p><strong>商品名称：</strong>{{ redemption?.productName }}</p>
          <p><strong>消耗积分：</strong>{{ redemption?.pointsSpent }}</p>
          <p><strong>兑换时间：</strong>{{ formatDate(redemption?.createdAt) }}</p>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCancelDialog = false">取消</el-button>
          <el-button 
            type="danger" 
            :loading="cancelling"
            @click="confirmCancelRedemption">
            确认取消兑换
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft,
  Postcard,
  Box,
  Location,
  OfficeBuilding,
  Clock,
  Warning,
  Star,
  Ticket,
  User,
  Close,
  ShoppingCart
} from '@element-plus/icons-vue'
import { mallAPI } from '@/api'
import QrDisplay from '@/components/QrDisplay.vue'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const redemption = ref(null)
const showCancelDialog = ref(false)
const cancelling = ref(false)

// 状态映射
const statusMap = {
  0: { text: '待领取', type: 'warning' },
  1: { text: '已领取', type: 'success' },
  2: { text: '已取消', type: 'info' }
}

// 方法
const loadRedemptionDetail = async () => {
  const redemptionId = route.params.id
  if (!redemptionId) {
    ElMessage.error('兑换记录ID不能为空')
    goBack()
    return
  }

  loading.value = true
  try {
    const response = await mallAPI.getRedemptionDetail(redemptionId)
    if (response.code === 200) {
      redemption.value = response.data
      console.log('网页端兑换详情数据:', redemption.value)
      console.log('网页端二维码内容(voucherCode):', redemption.value.voucherCode)
      console.log('网页端二维码内容类型:', typeof redemption.value.voucherCode)
    } else {
      ElMessage.error(response.message || '获取兑换详情失败')
    }
  } catch (error) {
    console.error('获取兑换详情失败:', error)
    const message = error.response?.data?.message || '获取兑换详情失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  return statusMap[status]?.type || 'info'
}

const getStatusText = (status) => {
  return statusMap[status]?.text || '未知状态'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleCancelRedemption = () => {
  if (redemption.value?.status !== 0) {
    ElMessage.warning('只能取消待领取状态的兑换')
    return
  }
  
  showCancelDialog.value = true
}

const confirmCancelRedemption = async () => {
  if (!redemption.value) return
  
  cancelling.value = true
  try {
    const response = await mallAPI.cancelRedemption(redemption.value.id)
    
    if (response.code === 200) {
      ElMessage.success('兑换已取消，积分已退还')
      showCancelDialog.value = false
      
      // 刷新数据
      await loadRedemptionDetail()
    }
  } catch (error) {
    console.error('取消兑换失败:', error)
    const message = error.response?.data?.message || '取消兑换失败，请重试'
    ElMessage.error(message)
  } finally {
    cancelling.value = false
  }
}

const goBack = () => {
  router.back()
}

const goToMall = () => {
  router.push('/student/mall')
}

// 拼接完整图片URL
const getProductImageUrl = (url) => {
  if (!url) return '/default-product.svg'
  
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // 如果是相对路径，拼接基础URL
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000/api'
  const baseUrl = apiBaseUrl.replace('/api', '')
  
  // 确保路径以 / 开头
  const path = url.startsWith('/') ? url : `/${url}`
  return `${baseUrl}${path}`
}

const handleImageError = (event) => {
  event.target.src = '/default-product.svg'
}

// 生命周期
onMounted(() => {
  loadRedemptionDetail()
})
</script>

<style lang="scss" scoped>
.voucher-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  
  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 24px;
    
    .back-button {
      margin-right: 16px;
      padding: 8px;
      
      .el-icon {
        font-size: 18px;
      }
    }
    
    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }
  
  .voucher-container {
    min-height: 400px;
  }
  
  .voucher-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  
  .status-header {
    text-align: center;
    padding: 20px;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    border-radius: 12px;
    
    .status-tag {
      font-size: 16px;
      padding: 8px 16px;
      margin-bottom: 8px;
    }
    
    .redemption-time {
      font-size: 14px;
      color: #606266;
    }
  }
  
  .qr-card,
  .product-card,
  .pickup-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .qr-container {
    text-align: center;
    
    .qr-instructions {
      margin-top: 20px;
      
      :deep(.el-alert__content) {
        text-align: left;
        
        p {
          margin: 4px 0;
          font-size: 14px;
        }
      }
    }
  }
  
  .product-info {
    display: flex;
    gap: 20px;
    
    .product-image-container {
      flex-shrink: 0;
      
      .product-image {
        width: 120px;
        height: 120px;
        object-fit: cover;
        border-radius: 8px;
        border: 1px solid #ebeef5;
      }
    }
    
    .product-details {
      flex: 1;
      
      .product-name {
        font-size: 20px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 16px 0;
      }
      
      .product-meta {
        display: flex;
        flex-direction: column;
        gap: 12px;
        
        .meta-item {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          color: #606266;
        }
      }
    }
  }
  
  .pickup-instructions {
    .instruction-item {
      display: flex;
      gap: 16px;
      margin-bottom: 20px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .instruction-icon {
        flex-shrink: 0;
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f0f9ff;
        border-radius: 50%;
        
        .el-icon {
          font-size: 20px;
        }
      }
      
      .instruction-content {
        flex: 1;
        
        h4 {
          margin: 0 0 4px 0;
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }
        
        p {
          margin: 0;
          font-size: 14px;
          color: #606266;
          line-height: 1.5;
        }
      }
    }
  }
  
  .verified-info,
  .cancelled-info {
    .verify-details {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-top: 16px;
      
      .verify-item {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
  
  .action-buttons {
    display: flex;
    gap: 16px;
    justify-content: center;
    margin-top: 20px;
    
    .el-button {
      min-width: 120px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }
  }
  
  .cancel-dialog-content {
    .redemption-summary {
      margin-top: 16px;
      padding: 16px;
      background: #f8f9fa;
      border-radius: 6px;
      
      p {
        margin: 0 0 8px 0;
        font-size: 14px;
        color: #606266;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        strong {
          color: #303133;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .voucher-detail {
    padding: 16px;
    
    .page-header {
      h1 {
        font-size: 20px;
      }
    }
    
    .product-info {
      flex-direction: column;
      text-align: center;
      
      .product-image-container {
        align-self: center;
      }
      
      .product-details {
        .product-meta {
          align-items: center;
        }
      }
    }
    
    .pickup-instructions {
      .instruction-item {
        flex-direction: column;
        text-align: center;
        
        .instruction-content {
          h4,
          p {
            text-align: center;
          }
        }
      }
    }
    
    .action-buttons {
      flex-direction: column;
      
      .el-button {
        width: 100%;
      }
    }
  }
}
</style>