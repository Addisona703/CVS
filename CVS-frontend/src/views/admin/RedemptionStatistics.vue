<template>
  <div class="redemption-statistics">
    <div class="page-header">
      <h1>统计报表</h1>
    </div>
    
    <!-- 时间筛选 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="统计时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="refreshData">刷新数据</el-button>
          <el-button type="success" @click="exportData" :loading="exporting">
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalRedemptions || 0 }}</div>
              <div class="stat-label">总兑换次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon points">
              <el-icon><Coin /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalPoints || 0 }}</div>
              <div class="stat-label">总消耗积分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon verified">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.verifiedCount || 0 }}</div>
              <div class="stat-label">已核销数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pendingCount || 0 }}</div>
              <div class="stat-label">待核销数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20">
      <!-- 商品兑换排行图表 -->
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <span>商品兑换排行</span>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <!-- 库存预警列表 -->
      <el-col :span="8">
        <el-card class="warning-card">
          <template #header>
            <div class="card-header">
              <span>库存预警</span>
              <el-tag type="danger" size="small">{{ lowStockProducts.length }}</el-tag>
            </div>
          </template>
          
          <div class="warning-list">
            <div 
              v-for="product in lowStockProducts" 
              :key="product.id"
              class="warning-item"
            >
              <div class="product-info">
                <div class="product-name">{{ product.name }}</div>
                <div class="product-stock">
                  剩余库存：
                  <span class="stock-value">{{ product.stock }}</span>
                  / 预警值：{{ product.stockWarning || 10 }}
                </div>
              </div>
              <el-tag type="danger" size="small">库存不足</el-tag>
            </div>
            
            <div v-if="lowStockProducts.length === 0" class="no-warning">
              <el-icon><Check /></el-icon>
              <span>暂无库存预警</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Download, 
  ShoppingCart, 
  Coin, 
  Check, 
  Clock 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { mallAPI } from '@/api'

const chartRef = ref()
let chartInstance = null
const exporting = ref(false)
const dateRange = ref([])

const filterForm = reactive({
  startDate: '',
  endDate: ''
})

const statistics = ref({
  totalRedemptions: 0,
  totalPoints: 0,
  verifiedCount: 0,
  pendingCount: 0
})

const productRanking = ref([])
const lowStockProducts = ref([])

// 处理日期变化
const handleDateChange = (dates) => {
  if (dates && dates.length === 2) {
    filterForm.startDate = dates[0]
    filterForm.endDate = dates[1]
  } else {
    filterForm.startDate = ''
    filterForm.endDate = ''
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const params = {}
    if (filterForm.startDate && filterForm.endDate) {
      params.startDate = filterForm.startDate
      params.endDate = filterForm.endDate
    }
    
    const response = await mallAPI.getRedemptionStatistics(params)
    if (response.code === 200) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 获取商品排行
const fetchProductRanking = async () => {
  try {
    const response = await mallAPI.getProductRanking({ limit: 10 })
    if (response.code === 200) {
      productRanking.value = response.data
      await nextTick()
      renderChart()
    }
  } catch (error) {
    console.error('获取商品排行失败:', error)
  }
}

// 获取库存预警
const fetchLowStockProducts = async () => {
  try {
    const response = await mallAPI.getLowStockProducts()
    if (response.code === 200) {
      lowStockProducts.value = response.data
    }
  } catch (error) {
    console.error('获取库存预警失败:', error)
  }
}

// 渲染图表
const renderChart = () => {
  if (!chartRef.value || productRanking.value.length === 0) return
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  chartInstance = echarts.init(chartRef.value)
  
  const option = {
    title: {
      text: '商品兑换排行TOP10',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'normal'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        const data = params[0]
        return `${data.name}<br/>兑换次数: ${data.value}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: productRanking.value.map(item => item.productName),
      axisLabel: {
        interval: 0,
        rotate: 45,
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      name: '兑换次数'
    },
    series: [
      {
        name: '兑换次数',
        type: 'bar',
        data: productRanking.value.map(item => item.redemptionCount),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#67c23a' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#67c23a' },
              { offset: 1, color: '#409eff' }
            ])
          }
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  })
}

// 导出数据
const exportData = async () => {
  exporting.value = true
  try {
    const params = {}
    if (filterForm.startDate && filterForm.endDate) {
      params.startDate = filterForm.startDate
      params.endDate = filterForm.endDate
    }
    
    const response = await mallAPI.exportRedemptions(params)
    
    // 创建下载链接
    const blob = new Blob([response], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名
    const dateStr = filterForm.startDate && filterForm.endDate 
      ? `${filterForm.startDate}_${filterForm.endDate}`
      : new Date().toISOString().split('T')[0]
    link.download = `兑换记录_${dateStr}.xlsx`
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 刷新数据
const refreshData = () => {
  fetchStatistics()
  fetchProductRanking()
  fetchLowStockProducts()
}

onMounted(() => {
  // 设置默认时间范围为最近30天
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 30)
  
  dateRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
  
  handleDateChange(dateRange.value)
  refreshData()
})
</script>

<style lang="scss" scoped>
.redemption-statistics {
  .page-header {
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
  }
  
  .stats-cards {
    margin-bottom: 20px;
    
    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        
        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 15px;
          font-size: 24px;
          color: white;
          
          &.total {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }
          
          &.points {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }
          
          &.verified {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }
          
          &.pending {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          }
        }
        
        .stat-info {
          .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
          }
          
          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-top: 5px;
          }
        }
      }
    }
  }
  
  .chart-card {
    .chart-container {
      height: 400px;
      width: 100%;
    }
  }
  
  .warning-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .warning-list {
      max-height: 400px;
      overflow-y: auto;
      
      .warning-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .product-info {
          flex: 1;
          
          .product-name {
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
          }
          
          .product-stock {
            font-size: 12px;
            color: #909399;
            
            .stock-value {
              color: #f56c6c;
              font-weight: bold;
            }
          }
        }
      }
      
      .no-warning {
        text-align: center;
        padding: 40px 0;
        color: #909399;
        
        .el-icon {
          font-size: 24px;
          color: #67c23a;
          margin-bottom: 8px;
        }
      }
    }
  }
}
</style>