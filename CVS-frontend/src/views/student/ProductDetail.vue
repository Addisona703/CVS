<template>
  <div class="product-detail">
    <div class="page-header">
      <el-button 
        type="text" 
        @click="goBack"
        class="back-button">
        <el-icon><ArrowLeft /></el-icon>
        返回商城
      </el-button>
    </div>

    <el-card v-loading="loading" class="detail-card">
      <template v-if="product">
        <el-row :gutter="40">
          <!-- 商品图片 -->
          <el-col :xs="24" :md="12">
            <div class="product-image-section">
              <div class="main-image">
                <img 
                  :src="getProductImageUrl(product.imageUrl)" 
                  :alt="product.name"
                  class="product-image"
                  @error="handleImageError" />
                
                <!-- 状态标签 -->
                <div class="status-badges">
                  <el-tag v-if="product.stock === 0" type="danger" size="large">
                    已兑完
                  </el-tag>
                  <el-tag v-else-if="product.stock <= 10" type="warning" size="large">
                    仅剩{{ product.stock }}件
                  </el-tag>
                  <el-tag v-if="product.status === 0" type="info" size="large">
                    已下架
                  </el-tag>
                </div>
              </div>
            </div>
          </el-col>

          <!-- 商品信息 -->
          <el-col :xs="24" :md="12">
            <div class="product-info-section">
              <!-- 商品基本信息 -->
              <div class="basic-info">
                <h1 class="product-title">{{ product.name }}</h1>
                
                <div class="product-meta">
                  <div class="category-info" v-if="product.categoryName">
                    <el-tag type="primary" plain>{{ product.categoryName }}</el-tag>
                  </div>
                  
                  <div class="points-info">
                    <span class="points-label">兑换积分：</span>
                    <span class="points-value">
                      <el-icon color="#e6a23c"><Star /></el-icon>
                      {{ product.pointsRequired }}
                    </span>
                  </div>
                  
                  <div class="stock-info">
                    <span class="stock-label">剩余库存：</span>
                    <span class="stock-value">{{ product.stock }}件</span>
                  </div>
                </div>

                <!-- 积分充足性提示 -->
                <div class="points-status">
                  <div v-if="userPoints >= product.pointsRequired" class="points-sufficient">
                    <el-icon color="#67c23a"><Check /></el-icon>
                    <span>积分充足，可以兑换</span>
                  </div>
                  <div v-else class="points-insufficient">
                    <el-icon color="#f56c6c"><Close /></el-icon>
                    <span>还需 {{ product.pointsRequired - userPoints }} 积分可兑换</span>
                  </div>
                  <div class="current-points">
                    <span>我的积分：{{ userPoints }}</span>
                  </div>
                </div>

                <!-- 兑换按钮 -->
                <div class="action-section">
                  <el-button 
                    type="primary" 
                    size="large"
                    :disabled="!canRedeem"
                    @click="showRedeemDialog = true"
                    class="redeem-button">
                    <el-icon><ShoppingCart /></el-icon>
                    {{ getButtonText() }}
                  </el-button>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 商品详细描述 -->
        <el-divider content-position="left">商品详情</el-divider>
        <div class="product-description">
          <p>{{ product.description }}</p>
        </div>

        <!-- 领取说明 -->
        <el-divider content-position="left">领取说明</el-divider>
        <div class="pickup-instructions">
          <el-alert
            title="重要提醒"
            type="info"
            :closable="false"
            show-icon>
            <template #default>
              <div class="instruction-content">
                <p><strong>领取地点：</strong>学工处办公室（行政楼201室）</p>
                <p><strong>领取时间：</strong>周一至周五 9:00-17:00</p>
                <p><strong>领取方式：</strong>兑换成功后，请携带兑换凭证（二维码）到指定地点领取</p>
                <p><strong>注意事项：</strong></p>
                <ul>
                  <li>请在兑换后7个工作日内领取，逾期视为放弃</li>
                  <li>领取时请出示学生证或身份证明</li>
                  <li>凭证仅限本人使用，不可转让</li>
                  <li>如有疑问，请联系学工处：0731-12345678</li>
                </ul>
              </div>
            </template>
          </el-alert>
        </div>
      </template>

      <!-- 商品不存在 -->
      <el-empty v-else-if="!loading" description="商品不存在或已下架" />
    </el-card>

    <!-- 兑换确认对话框 -->
    <el-dialog
      v-model="showRedeemDialog"
      title="确认兑换"
      width="500px"
      :before-close="handleDialogClose">
      <div class="redeem-confirm">
        <div class="confirm-product">
          <img :src="getProductImageUrl(product?.imageUrl)" class="confirm-image" />
          <div class="confirm-info">
            <h4>{{ product?.name }}</h4>
            <p class="confirm-points">
              <el-icon><Star /></el-icon>
              需要积分：{{ product?.pointsRequired }}
            </p>
          </div>
        </div>
        
        <el-divider />
        
        <div class="points-calculation">
          <div class="calc-row">
            <span>当前积分：</span>
            <span class="points-current">{{ userPoints }}</span>
          </div>
          <div class="calc-row">
            <span>消耗积分：</span>
            <span class="points-cost">-{{ product?.pointsRequired }}</span>
          </div>
          <el-divider />
          <div class="calc-row total">
            <span>兑换后积分：</span>
            <span class="points-remaining">{{ userPoints - (product?.pointsRequired || 0) }}</span>
          </div>
        </div>

        <el-alert
          title="兑换后将生成电子凭证，请妥善保管用于线下领取"
          type="warning"
          :closable="false"
          show-icon />
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRedeemDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            :loading="redeeming"
            @click="confirmRedeem">
            确认兑换
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { mallAPI } from '@/api/mall'
import { pointsAPI } from '@/api/points'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, 
  Star, 
  Check, 
  Close, 
  ShoppingCart 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const product = ref(null)
const userPoints = ref(0)
const showRedeemDialog = ref(false)
const redeeming = ref(false)

// 计算属性
const canRedeem = computed(() => {
  if (!product.value) return false
  return product.value.status === 1 && 
         product.value.stock > 0 && 
         userPoints.value >= product.value.pointsRequired
})

// 方法
const goBack = () => {
  router.push('/student/mall')
}

// 拼接完整图片URL
const getProductImageUrl = (url) => {
  if (!url) return '/src/assets/images/default-product.png'
  
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
  event.target.src = '/src/assets/images/default-product.png'
}

const getButtonText = () => {
  if (!product.value) return '加载中...'
  if (product.value.status === 0) return '商品已下架'
  if (product.value.stock === 0) return '已兑完'
  if (userPoints.value < product.value.pointsRequired) return '积分不足'
  return '立即兑换'
}

const loadProductDetail = async () => {
  loading.value = true
  try {
    const productId = route.params.id
    const response = await mallAPI.getProductDetail(productId)
    if (response.code === 200) {
      product.value = response.data
    } else {
      ElMessage.error(response.message || '获取商品详情失败')
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

const loadUserPoints = async () => {
  try {
    const response = await pointsAPI.getCurrentUserPointsStats()
    if (response.code === 200) {
      userPoints.value = response.data.totalPoints || 0
    }
  } catch (error) {
    console.error('获取用户积分失败:', error)
  }
}

const handleDialogClose = (done) => {
  if (redeeming.value) {
    ElMessage.warning('兑换进行中，请稍候...')
    return
  }
  done()
}

const confirmRedeem = async () => {
  redeeming.value = true
  try {
    const response = await mallAPI.redeemProduct({
      productId: product.value.id
    })
    
    if (response.code === 200) {
      ElMessage.success('兑换成功！')
      showRedeemDialog.value = false
      
      // 跳转到兑换凭证页面
      router.push(`/student/voucher/${response.data.id}`)
    } else {
      ElMessage.error(response.message || '兑换失败')
    }
  } catch (error) {
    console.error('兑换失败:', error)
    ElMessage.error(error.response?.data?.message || '兑换失败')
  } finally {
    redeeming.value = false
  }
}

// 生命周期
onMounted(() => {
  loadProductDetail()
  loadUserPoints()
})
</script>

<style scoped>
.product-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.back-button {
  font-size: 16px;
  color: #409eff;
}

.detail-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-image-section {
  position: relative;
}

.main-image {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 8px;
}

.status-badges {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-info-section {
  padding: 20px 0;
}

.product-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
  line-height: 1.4;
}

.product-meta {
  margin-bottom: 30px;
}

.category-info {
  margin-bottom: 15px;
}

.points-info, .stock-info {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 16px;
}

.points-label, .stock-label {
  color: #606266;
  margin-right: 8px;
}

.points-value {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 20px;
  font-weight: bold;
  color: #e6a23c;
}

.stock-value {
  font-weight: bold;
  color: #67c23a;
}

.points-status {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.points-sufficient, .points-insufficient {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  margin-bottom: 10px;
}

.points-sufficient {
  color: #67c23a;
}

.points-insufficient {
  color: #f56c6c;
}

.current-points {
  font-size: 14px;
  color: #909399;
}

.action-section {
  margin-top: 30px;
}

.redeem-button {
  width: 100%;
  height: 50px;
  font-size: 18px;
  font-weight: bold;
}

.product-description {
  padding: 20px 0;
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
}

.pickup-instructions {
  padding: 20px 0;
}

.instruction-content p {
  margin-bottom: 10px;
  line-height: 1.6;
}

.instruction-content ul {
  margin-top: 10px;
  padding-left: 20px;
}

.instruction-content li {
  margin-bottom: 5px;
  line-height: 1.5;
}

/* 对话框样式 */
.redeem-confirm {
  padding: 10px 0;
}

.confirm-product {
  display: flex;
  gap: 15px;
  align-items: center;
}

.confirm-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.confirm-info h4 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #303133;
}

.confirm-points {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #e6a23c;
  font-weight: bold;
  margin: 0;
}

.points-calculation {
  margin: 20px 0;
}

.calc-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 16px;
}

.calc-row.total {
  font-weight: bold;
  font-size: 18px;
}

.points-current {
  color: #67c23a;
}

.points-cost {
  color: #f56c6c;
}

.points-remaining {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-detail {
    padding: 10px;
  }
  
  .product-title {
    font-size: 24px;
  }
  
  .product-image {
    height: 300px;
  }
  
  .redeem-button {
    height: 45px;
    font-size: 16px;
  }
}
</style>