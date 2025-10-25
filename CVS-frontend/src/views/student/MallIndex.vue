<template>
  <div class="mall-index">
    <div class="page-header">
      <h1>积分商城</h1>
      <!-- 积分余额显示 -->
      <div class="points-banner">
        <div class="points-info">
          <el-icon size="24" color="#e6a23c"><Star /></el-icon>
          <span class="points-label">我的积分：</span>
          <span class="points-value">{{ userPoints }}</span>
        </div>
        <el-button 
          type="primary" 
          plain
          @click="goToMyRedemptions"
          class="my-redemptions-btn">
          <el-icon><List /></el-icon>
          我的兑换
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <el-card class="filter-card">
      <el-row :gutter="20" align="middle">
        <el-col :xs="24" :sm="12" :md="8">
          <!-- 搜索框 -->
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="搜索商品名称或描述"
            clearable
            @input="handleSearch"
            @clear="handleSearch">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :xs="24" :sm="12" :md="16">
          <!-- 分类筛选 -->
          <div class="category-tabs">
            <el-button 
              :type="activeCategory === 'all' ? 'primary' : ''"
              :plain="activeCategory !== 'all'"
              size="small"
              @click="handleCategoryChange('all')">
              全部商品
            </el-button>
            <el-button 
              v-for="category in categories" 
              :key="category.id"
              :type="activeCategory === category.id ? 'primary' : ''"
              :plain="activeCategory !== category.id"
              size="small"
              @click="handleCategoryChange(category.id)">
              {{ category.name }}
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 商品网格 -->
    <div class="products-container">
      <el-row :gutter="20" v-loading="loading">
        <el-col 
          v-for="product in products" 
          :key="product.id"
          :xs="24" :sm="12" :md="8" :lg="6"
          class="product-col">
          <el-card 
            class="product-card"
            :body-style="{ padding: '0' }"
            shadow="hover"
            @click="goToDetail(product.id)">
            <!-- 商品图片 -->
            <div class="product-image-container">
              <img 
                :src="product.imageUrl || '/default-product.svg'" 
                :alt="product.name"
                class="product-image" 
                @error="handleImageError" />
              <!-- 库存标签 -->
              <div class="stock-badge" v-if="product.stock <= 10 && product.stock > 0">
                <el-tag type="warning" size="small">仅剩{{ product.stock }}件</el-tag>
              </div>
              <div class="stock-badge" v-else-if="product.stock === 0">
                <el-tag type="danger" size="small">已兑完</el-tag>
              </div>
            </div>
            
            <!-- 商品信息 -->
            <div class="product-info">
              <h3 class="product-name" :title="product.name">{{ product.name }}</h3>
              <p class="product-description" :title="product.description">
                {{ product.description }}
              </p>
              
              <div class="product-footer">
                <div class="product-points">
                  <el-icon color="#e6a23c"><Star /></el-icon>
                  <span class="points-text">{{ product.pointsRequired }}积分</span>
                </div>
                <div class="product-stock">
                  <span class="stock-text">库存{{ product.stock }}</span>
                </div>
              </div>
              
              <!-- 兑换按钮 -->
              <el-button 
                type="primary" 
                class="redeem-button"
                :disabled="!canRedeem(product)"
                @click.stop="handleRedeem(product)">
                <template v-if="product.stock === 0">
                  已兑完
                </template>
                <template v-else-if="userPoints < product.pointsRequired">
                  积分不足
                </template>
                <template v-else>
                  立即兑换
                </template>
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty 
        v-if="!loading && products.length === 0" 
        description="暂无商品"
        :image-size="120" />
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>

    <!-- 兑换确认对话框 -->
    <el-dialog 
      v-model="showRedeemDialog" 
      title="确认兑换"
      width="400px"
      :before-close="handleCloseRedeemDialog">
      <div class="redeem-dialog-content" v-if="selectedProduct">
        <div class="product-summary">
          <img 
            :src="selectedProduct.imageUrl || '/src/assets/images/default-product.png'" 
            :alt="selectedProduct.name"
            class="dialog-product-image" />
          <div class="product-details">
            <h4>{{ selectedProduct.name }}</h4>
            <p class="points-required">
              <el-icon color="#e6a23c"><Star /></el-icon>
              需要 {{ selectedProduct.pointsRequired }} 积分
            </p>
          </div>
        </div>
        
        <div class="points-check">
          <p>当前积分：<span class="current-points">{{ userPoints }}</span></p>
          <p>兑换后剩余：<span class="remaining-points">{{ userPoints - selectedProduct.pointsRequired }}</span></p>
        </div>
        
        <div class="pickup-notice">
          <el-alert
            title="领取说明"
            type="info"
            :closable="false"
            show-icon>
            <p>兑换成功后，请携带兑换凭证到学工处办公室领取商品</p>
            <p>领取地点：行政楼201室</p>
            <p>领取时间：周一至周五 9:00-17:00</p>
          </el-alert>
        </div>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, Search, List } from '@element-plus/icons-vue'
import { mallAPI } from '@/api'
import { usePagination } from '@/composables/usePagination'

const router = useRouter()
const { loading, pagination, updatePagination } = usePagination()

// 响应式数据
const userPoints = ref(0)
const products = ref([])
const categories = ref([])
const activeCategory = ref('all')
const showRedeemDialog = ref(false)
const selectedProduct = ref(null)
const redeeming = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 搜索防抖定时器
let searchTimer = null

// 计算属性
const canRedeem = computed(() => {
  return (product) => {
    return product.stock > 0 && userPoints.value >= product.pointsRequired
  }
})

// 方法
const loadUserPoints = async () => {
  try {
    const response = await mallAPI.getUserPoints()
    if (response.code === 200) {
      userPoints.value = response.data.totalPoints || 0
    }
  } catch (error) {
    console.error('获取用户积分失败:', error)
    ElMessage.error('获取积分信息失败')
  }
}

const loadCategories = async () => {
  try {
    const response = await mallAPI.getCategories()
    if (response.code === 200) {
      categories.value = response.data || []
    }
  } catch (error) {
    console.error('获取商品分类失败:', error)
  }
}

const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      categoryId: activeCategory.value === 'all' ? null : activeCategory.value,
      keyword: searchForm.keyword.trim() || null
    }
    
    const response = await mallAPI.getProductList(params)
    if (response.code === 200) {
      // 为每个商品添加兑换能力标识
      products.value = (response.data.records || []).map(product => ({
        ...product,
        canRedeem: product.stock > 0 && userPoints.value >= product.pointsRequired
      }))
      
      updatePagination(response.data)
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

const handleCategoryChange = (categoryId) => {
  activeCategory.value = categoryId
  pagination.current = 1
  loadProducts()
}

const handleSearch = () => {
  // 防抖处理
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  searchTimer = setTimeout(() => {
    pagination.current = 1
    loadProducts()
  }, 500)
}

const goToDetail = (productId) => {
  router.push(`/student/mall/product/${productId}`)
}

const handleRedeem = (product) => {
  if (!canRedeem.value(product)) {
    if (product.stock === 0) {
      ElMessage.warning('商品已兑完')
    } else if (userPoints.value < product.pointsRequired) {
      ElMessage.warning('积分不足，无法兑换')
    }
    return
  }
  
  selectedProduct.value = product
  showRedeemDialog.value = true
}

const handleCloseRedeemDialog = () => {
  if (!redeeming.value) {
    showRedeemDialog.value = false
    selectedProduct.value = null
  }
}

const confirmRedeem = async () => {
  if (!selectedProduct.value) return
  
  redeeming.value = true
  try {
    const response = await mallAPI.redeemProduct({
      productId: selectedProduct.value.id
    })
    
    if (response.code === 200) {
      ElMessage.success('兑换成功！')
      showRedeemDialog.value = false
      selectedProduct.value = null
      
      // 刷新数据
      await loadUserPoints()
      await loadProducts()
      
      // 跳转到兑换详情页
      router.push(`/student/mall/voucher/${response.data.id}`)
    }
  } catch (error) {
    console.error('兑换失败:', error)
    const message = error.response?.data?.message || '兑换失败，请重试'
    ElMessage.error(message)
  } finally {
    redeeming.value = false
  }
}

const handleImageError = (event) => {
  event.target.src = '/default-product.svg'
}

const goToMyRedemptions = () => {
  router.push('/student/mall/my-redemptions')
}

// 分页处理方法
const handleCurrentChange = (page) => {
  pagination.current = page
  loadProducts()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadProducts()
}

// 生命周期
onMounted(() => {
  loadUserPoints()
  loadCategories()
  loadProducts()
})
</script>

<style lang="scss" scoped>
.mall-index {
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
    
    .points-banner {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .points-info {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px 20px;
        background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
        border-radius: 20px;
        color: #333;
        font-weight: 600;
        
        .points-label {
          font-size: 14px;
        }
        
        .points-value {
          font-size: 18px;
          color: #d4380d;
        }
      }
      
      .my-redemptions-btn {
        display: flex;
        align-items: center;
        gap: 4px;
        border-radius: 20px;
      }
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
    
    .category-tabs {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }
  
  .products-container {
    min-height: 400px;
    
    .product-col {
      margin-bottom: 20px;
    }
    
    .product-card {
      height: 100%;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
      }
      
      .product-image-container {
        position: relative;
        height: 200px;
        overflow: hidden;
        
        .product-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s ease;
        }
        
        .stock-badge {
          position: absolute;
          top: 8px;
          right: 8px;
        }
        
        &:hover .product-image {
          transform: scale(1.05);
        }
      }
      
      .product-info {
        padding: 16px;
        
        .product-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 8px 0;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .product-description {
          font-size: 14px;
          color: #606266;
          margin: 0 0 12px 0;
          height: 40px;
          overflow: hidden;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
        
        .product-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;
          
          .product-points {
            display: flex;
            align-items: center;
            gap: 4px;
            
            .points-text {
              font-size: 16px;
              font-weight: 600;
              color: #e6a23c;
            }
          }
          
          .product-stock {
            .stock-text {
              font-size: 12px;
              color: #909399;
            }
          }
        }
        
        .redeem-button {
          width: 100%;
        }
      }
    }
  }
  
  .pagination {
    margin-top: 20px;
    text-align: center;
  }
  
  .redeem-dialog-content {
    .product-summary {
      display: flex;
      gap: 16px;
      margin-bottom: 20px;
      
      .dialog-product-image {
        width: 80px;
        height: 80px;
        object-fit: cover;
        border-radius: 8px;
      }
      
      .product-details {
        flex: 1;
        
        h4 {
          margin: 0 0 8px 0;
          font-size: 16px;
          color: #303133;
        }
        
        .points-required {
          display: flex;
          align-items: center;
          gap: 4px;
          margin: 0;
          font-size: 14px;
          color: #e6a23c;
          font-weight: 600;
        }
      }
    }
    
    .points-check {
      margin-bottom: 20px;
      padding: 16px;
      background: #f8f9fa;
      border-radius: 8px;
      
      p {
        margin: 0 0 8px 0;
        font-size: 14px;
        color: #606266;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .current-points,
        .remaining-points {
          font-weight: 600;
          color: #e6a23c;
        }
      }
    }
    
    .pickup-notice {
      :deep(.el-alert__content) {
        p {
          margin: 4px 0;
          font-size: 13px;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .mall-index {
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
      
      .points-banner {
        align-self: stretch;
        
        .points-info {
          justify-content: center;
        }
      }
    }
    
    .filter-card {
      :deep(.el-card__body) {
        padding: 16px;
      }
      
      .category-tabs {
        margin-top: 12px;
        justify-content: center;
      }
    }
  }
}
</style>