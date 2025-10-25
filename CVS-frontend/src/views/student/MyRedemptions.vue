<template>
  <div class="my-redemptions">
    <div class="page-header">
      <h1>我的兑换</h1>
      <div class="header-actions">
        <el-button type="primary" @click="goToMall">
          <el-icon><ShoppingCart /></el-icon>
          继续兑换
        </el-button>
      </div>
    </div>

    <!-- 状态筛选 -->
    <el-card class="filter-card">
      <div class="status-filters">
        <el-button 
          :type="activeStatus === 'all' ? 'primary' : ''"
          :plain="activeStatus !== 'all'"
          @click="handleStatusChange('all')">
          全部记录
        </el-button>
        <el-button 
          :type="activeStatus === 0 ? 'primary' : ''"
          :plain="activeStatus !== 0"
          @click="handleStatusChange(0)">
          <el-icon><Clock /></el-icon>
          待领取
        </el-button>
        <el-button 
          :type="activeStatus === 1 ? 'primary' : ''"
          :plain="activeStatus !== 1"
          @click="handleStatusChange(1)">
          <el-icon><Check /></el-icon>
          已领取
        </el-button>
        <el-button 
          :type="activeStatus === 2 ? 'primary' : ''"
          :plain="activeStatus !== 2"
          @click="handleStatusChange(2)">
          <el-icon><Close /></el-icon>
          已取消
        </el-button>
      </div>
    </el-card>

    <!-- 兑换记录列表 -->
    <div class="redemptions-container">
      <el-row :gutter="20" v-loading="loading">
        <el-col 
          v-for="redemption in redemptions" 
          :key="redemption.id"
          :xs="24" :sm="12" :lg="8"
          class="redemption-col">
          <el-card 
            class="redemption-card"
            shadow="hover">
            <!-- 状态标签 -->
            <div class="status-header">
              <el-tag 
                :type="getStatusType(redemption.status)"
                size="small">
                {{ getStatusText(redemption.status) }}
              </el-tag>
              <span class="redemption-time">
                {{ formatDate(redemption.createdAt) }}
              </span>
            </div>

            <!-- 商品信息 -->
            <div class="product-info">
              <div class="product-image-container">
                <img 
                  :src="redemption.productImageUrl || '/default-product.svg'" 
                  :alt="redemption.productName"
                  class="product-image"
                  @error="handleImageError" />
              </div>
              <div class="product-details">
                <h3 class="product-name" :title="redemption.productName">
                  {{ redemption.productName }}
                </h3>
                <div class="points-info">
                  <el-icon color="#e6a23c"><Star /></el-icon>
                  <span class="points-text">{{ redemption.pointsSpent }}积分</span>
                </div>
              </div>
            </div>

            <!-- 凭证信息 -->
            <div class="voucher-info">
              <div class="voucher-code">
                <span class="label">凭证编号：</span>
                <span class="code">{{ redemption.voucherCode }}</span>
              </div>
              
              <!-- 核销信息 -->
              <div v-if="redemption.status === 1" class="verify-info">
                <div class="verify-time">
                  <el-icon><Clock /></el-icon>
                  <span>领取时间：{{ formatDate(redemption.verifiedAt) }}</span>
                </div>
                <div v-if="redemption.verifiedByName" class="verify-staff">
                  <el-icon><User /></el-icon>
                  <span>核销人员：{{ redemption.verifiedByName }}</span>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="card-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="viewVoucherDetail(redemption)">
                <el-icon><View /></el-icon>
                查看凭证
              </el-button>
              
              <el-button 
                v-if="redemption.status === 0"
                type="danger" 
                size="small"
                plain
                @click="handleCancelRedemption(redemption)">
                <el-icon><Close /></el-icon>
                取消兑换
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty 
        v-if="!loading && redemptions.length === 0" 
        :description="getEmptyDescription()"
        :image-size="120">
        <el-button type="primary" @click="goToMall">
          去兑换商品
        </el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[9, 18, 36]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>

    <!-- 取消兑换确认对话框 -->
    <el-dialog 
      v-model="showCancelDialog" 
      title="确认取消兑换"
      width="400px">
      <div class="cancel-dialog-content" v-if="selectedRedemption">
        <el-alert
          title="取消兑换说明"
          type="warning"
          :closable="false"
          show-icon>
          <p>取消兑换后，消耗的积分将退还到您的账户</p>
          <p>商品库存也将相应恢复</p>
        </el-alert>
        
        <div class="redemption-summary">
          <p><strong>商品名称：</strong>{{ selectedRedemption.productName }}</p>
          <p><strong>消耗积分：</strong>{{ selectedRedemption.pointsSpent }}</p>
          <p><strong>兑换时间：</strong>{{ formatDate(selectedRedemption.createdAt) }}</p>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ShoppingCart, 
  Clock, 
  Check, 
  Close, 
  Star, 
  View, 
  User 
} from '@element-plus/icons-vue'
import { mallAPI } from '@/api'
import { usePagination } from '@/composables/usePagination'

const router = useRouter()
const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

// 响应式数据
const redemptions = ref([])
const activeStatus = ref('all')
const showCancelDialog = ref(false)
const selectedRedemption = ref(null)
const cancelling = ref(false)

// 状态映射
const statusMap = {
  0: { text: '待领取', type: 'warning' },
  1: { text: '已领取', type: 'success' },
  2: { text: '已取消', type: 'info' }
}

// 方法
const loadRedemptions = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      status: activeStatus.value === 'all' ? null : activeStatus.value
    }
    
    const response = await mallAPI.getMyRedemptions(params)
    if (response.code === 200) {
      redemptions.value = response.data.records || []
      updatePagination(response.data)
    }
  } catch (error) {
    console.error('获取兑换记录失败:', error)
    ElMessage.error('获取兑换记录失败')
  } finally {
    loading.value = false
  }
}

const handleStatusChange = (status) => {
  activeStatus.value = status
  pagination.current = 1
  loadRedemptions()
}

const getStatusType = (status) => {
  return statusMap[status]?.type || 'info'
}

const getStatusText = (status) => {
  return statusMap[status]?.text || '未知状态'
}

const getEmptyDescription = () => {
  if (activeStatus.value === 'all') {
    return '暂无兑换记录'
  }
  return `暂无${getStatusText(activeStatus.value)}的兑换记录`
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

const viewVoucherDetail = (redemption) => {
  router.push(`/student/mall/voucher/${redemption.id}`)
}

const handleCancelRedemption = (redemption) => {
  if (redemption.status !== 0) {
    ElMessage.warning('只能取消待领取状态的兑换')
    return
  }
  
  selectedRedemption.value = redemption
  showCancelDialog.value = true
}

const confirmCancelRedemption = async () => {
  if (!selectedRedemption.value) return
  
  cancelling.value = true
  try {
    const response = await mallAPI.cancelRedemption(selectedRedemption.value.id)
    
    if (response.code === 200) {
      ElMessage.success('兑换已取消，积分已退还')
      showCancelDialog.value = false
      selectedRedemption.value = null
      
      // 刷新列表
      await loadRedemptions()
    }
  } catch (error) {
    console.error('取消兑换失败:', error)
    const message = error.response?.data?.message || '取消兑换失败，请重试'
    ElMessage.error(message)
  } finally {
    cancelling.value = false
  }
}

const goToMall = () => {
  router.push('/student/mall')
}

const handleImageError = (event) => {
  event.target.src = '/default-product.svg'
}

// 生命周期
onMounted(() => {
  loadRedemptions()
})
</script>

<style lang="scss" scoped>
.my-redemptions {
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
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
    
    .status-filters {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      
      .el-button {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
  
  .redemptions-container {
    min-height: 400px;
    
    .redemption-col {
      margin-bottom: 20px;
    }
    
    .redemption-card {
      height: 100%;
      display: flex;
      flex-direction: column;
      transition: all 0.3s ease;
      
      :deep(.el-card__body) {
        display: flex;
        flex-direction: column;
        flex: 1;
        height: 100%;
      }
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
      }
      
      .status-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        
        .redemption-time {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .product-info {
        display: flex;
        gap: 12px;
        margin-bottom: 12px;
        
        .product-image-container {
          flex-shrink: 0;
          
          .product-image {
            width: 60px;
            height: 60px;
            object-fit: cover;
            border-radius: 6px;
          }
        }
        
        .product-details {
          flex: 1;
          min-width: 0;
          
          .product-name {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
            margin: 0 0 8px 0;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .points-info {
            display: flex;
            align-items: center;
            gap: 4px;
            
            .points-text {
              font-size: 14px;
              font-weight: 600;
              color: #e6a23c;
            }
          }
        }
      }
      
      .voucher-info {
        margin-bottom: auto;
        padding: 12px;
        background: #f8f9fa;
        border-radius: 6px;
        flex-grow: 1;
        
        .voucher-code {
          margin-bottom: 8px;
          
          .label {
            font-size: 12px;
            color: #606266;
          }
          
          .code {
            font-size: 12px;
            font-family: 'Courier New', monospace;
            color: #303133;
            font-weight: 600;
          }
        }
        
        .verify-info {
          .verify-time,
          .verify-staff {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;
            color: #606266;
            margin-bottom: 4px;
            
            &:last-child {
              margin-bottom: 0;
            }
          }
        }
      }
      
      .card-actions {
        display: flex;
        gap: 8px;
        margin-top: 16px;
        
        .el-button {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 4px;
        }
        
        // 取消兑换按钮改为深红色
        .el-button--danger.is-plain {
          background-color: #f56c6c;
          border-color: #f56c6c;
          color: #ffffff;
          
          &:hover {
            background-color: #f78989;
            border-color: #f78989;
          }
          
          &:active {
            background-color: #dd6161;
            border-color: #dd6161;
          }
        }
      }
    }
  }
  
  .pagination {
    margin-top: 20px;
    text-align: center;
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
  .my-redemptions {
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
      
      .header-actions {
        align-self: stretch;
        
        .el-button {
          flex: 1;
        }
      }
    }
    
    .filter-card {
      :deep(.el-card__body) {
        padding: 16px;
      }
      
      .status-filters {
        justify-content: center;
        
        .el-button {
          flex: 1;
          min-width: 0;
        }
      }
    }
    
    .redemptions-container {
      .redemption-card {
        .product-info {
          .product-details {
            .product-name {
              font-size: 14px;
            }
          }
        }
        
        .card-actions {
          flex-direction: column;
          
          .el-button {
            width: 100%;
          }
        }
      }
    }
  }
}
</style>