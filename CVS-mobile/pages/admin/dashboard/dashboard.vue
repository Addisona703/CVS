<template>
  <view class="dashboard-container">
    <!-- 顶部导航栏 -->
    <view class="header-bar">
      <text class="header-title">学工处首页</text>
      <view class="notification-btn" @click="handleNotification">
        <ph-icon name="bell" color="#666" :size="44" class="notification-icon" />
        <view v-if="unreadCount > 0" class="notification-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</view>
      </view>
    </view>

    <!-- 快捷功能区 -->
    <view class="quick-actions-section">
      <view class="section-header">
        <text class="section-title">快捷功能</text>
      </view>
      <view class="action-grid">
        <view
          class="action-item"
          v-for="action in quickActions"
          :key="action.key"
          @click="handleAction(action.path)"
        >
          <view class="action-icon" :style="{ background: action.gradient }">
            <ph-icon :name="action.icon" color="#fff" :size="52" />
          </view>
          <text class="action-label">{{ action.label }}</text>
        </view>
      </view>
    </view>

    <!-- 统计卡片 -->
  <view class="stats-grid">
      <view class="stat-card" v-for="stat in stats" :key="stat.key">
      <view class="stat-icon" :style="{ background: stat.gradient || stat.color }">
        <text v-if="stat.isIconfont" class="iconfont stat-icon-text">{{ stat.icon }}</text>
        <ph-icon v-else :name="stat.icon" color="#fff" :size="52" />
      </view>
        <view class="stat-content">
          <text class="stat-value">{{ stat.value }}</text>
          <text class="stat-label">{{ stat.label }}</text>
        </view>
      </view>
  </view>

    <!-- 标签页内容区 -->
    <view class="tabbed-section">
      <!-- 标签栏 -->
      <view class="tab-bar">
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'recent' }"
          @click="activeTab = 'recent'"
        >
          <text class="tab-text">活动统计趋势</text>
        </view>
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'pending' }"
          @click="activeTab = 'pending'"
        >
          <text class="tab-text">待审核</text>
          <view v-if="pendingList.length > 0" class="tab-badge">{{ pendingList.length }}</view>
        </view>
        <view class="tab-indicator" :style="{ left: activeTab === 'recent' ? '0' : '50%' }"></view>
      </view>

      <!-- 标签页内容 -->
      <view class="tab-content">
        <!-- 活动统计趋势内容 -->
        <view v-show="activeTab === 'recent'" class="tab-panel">
          <!-- 时间范围选择 -->
          <view class="time-selector">
            <view 
              v-for="option in timeOptions" 
              :key="option.value"
              class="time-option"
              :class="{ active: statisticsDays === option.value }"
              @click="changeStatisticsDays(option.value)"
            >
              <text class="time-text">{{ option.label }}</text>
            </view>
          </view>
          
          <view class="chart-container">
            <view v-if="hasRecentActivities" class="chart-wrapper">
              <canvas canvas-id="activityChart" id="activityChart" class="chart-canvas" @touchstart="touchChart" @touchmove="moveChart" @touchend="touchEndChart"></canvas>
            </view>
            <view v-else class="empty-list">
              <ph-icon name="chart-line" color="#ddd" :size="100" class="empty-icon" />
              <text class="empty-text">暂无活动数据</text>
            </view>
          </view>
        </view>

        <!-- 待审核内容 -->
        <view v-show="activeTab === 'pending'" class="tab-panel">
          <view class="todo-list">
            <view class="todo-item" v-for="item in pendingList" :key="item.id">
              <view class="todo-info">
                <text class="todo-title">新活动：{{ item.title }}</text>
                <text class="todo-sub">创建者：{{ item.organizerName || '未知' }}</text>
              </view>
              <view class="todo-actions">
                <button class="btn-reject" @click="openReject(item)">拒绝</button>
                <button class="btn-approve" @click="approve(item)">通过</button>
              </view>
            </view>
            <view v-if="!loading && pendingList.length === 0" class="empty-list">
              <ph-icon name="clipboard-text" color="#ddd" :size="100" class="empty-icon" />
              <text class="empty-text">暂无待审核</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- Custom Tab Bar -->
    <custom-tabbar :current="0" role="admin" />

    <!-- 拒绝弹窗 -->
    <view class="modal" v-if="showReject" @click="closeReject">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">拒绝审核</text>
        </view>
        <view class="modal-body">
          <textarea
            class="opinion-input"
            v-model="opinion"
            placeholder="请输入拒绝原因"
            maxlength="200"
          />
          <text class="char-count">{{ opinion.length }}/200</text>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="closeReject">取消</button>
          <button class="modal-btn confirm" @click="confirmReject">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getAdminDashboardStats } from '@/api/statistics'
import { getActivityStatistics, getActivityList, approveActivity } from '@/api/activity'
import CustomTabbar from '@/components/common/custom-tabbar/custom-tabbar.vue'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'
import uCharts from '@qiun/ucharts'

let chartInstance = null

const quickActions = ref([
  {
    key: 'users',
    label: '用户管理',
    icon: 'users',
    gradient: 'linear-gradient(135deg, #FF8C42 0%, #FF6B35 100%)',
    path: '/pages/admin/user-management/user-management'
  },
  {
    key: 'activities',
    label: '活动管理',
    icon: 'calendar',
    gradient: 'linear-gradient(135deg, #FFA726 0%, #FF7043 100%)',
    path: '/pages/admin/activity-management/activity-management'
  },
  {
    key: 'approval',
    label: '活动审核',
    icon: 'check-circle',
    gradient: 'linear-gradient(135deg, #FFB74D 0%, #FF9800 100%)',
    path: '/pages/admin/activity-approval/activity-approval'
  },
  {
    key: 'products',
    label: '商品管理',
    icon: 'shopping-cart',
    gradient: 'linear-gradient(135deg, #FFCA28 0%, #FFA000 100%)',
    path: '/pages/admin/product-management/product-management'
  }
])

const stats = ref([
  { key: 'users', label: '总用户数', value: 0, icon: '\ue85c', isIconfont: true, color: '#FF8C42', gradient: 'linear-gradient(135deg, #FF8C42 0%, #FF6B35 100%)' },
  { key: 'activities', label: '总活动数', value: 0, icon: '\ue829', isIconfont: true, color: '#FFA726', gradient: 'linear-gradient(135deg, #FFA726 0%, #FF7043 100%)' },
  { key: 'signups', label: '总报名数', value: 0, icon: 'clipboard-text', isIconfont: false, color: '#FFB74D', gradient: 'linear-gradient(135deg, #FFB74D 0%, #FF9800 100%)' },
  { key: 'serviceRecords', label: '服务记录数', value: 0, icon: '\uea22', isIconfont: true, color: '#FFCA28', gradient: 'linear-gradient(135deg, #FFCA28 0%, #FFA000 100%)' }
])

const pendingList = ref([])
const loading = ref(false)
const showReject = ref(false)
const opinion = ref('')
const currentItem = ref(null)
const unreadCount = ref(0)
const activeTab = ref('recent')
const hasRecentActivities = ref(false)
const statisticsDays = ref(7)
const timeOptions = [
  { label: '7天', value: 7 },
  { label: '30天', value: 30 },
  { label: '90天', value: 90 }
]

const handleNotification = () => {
  uni.navigateTo({
    url: '/pages/common/notifications/notifications'
  })
}

const loadStats = async () => {
  const data = await getAdminDashboardStats()
  stats.value[0].value = data.totalUsers || 0
  stats.value[1].value = data.totalActivities || 0
  stats.value[2].value = data.totalSignups || 0
  stats.value[3].value = data.totalServiceRecords || 0
  const pendingRes = await getActivityList({ pageNum: 1, pageSize: 1, params: { status: 'PENDING_APPROVAL' } })
  stats.value[2].label = '总报名数'
  stats.value[0].label = '总用户数'
}

const loadPending = async () => {
  loading.value = true
  try {
    const res = await getActivityList({ pageNum: 1, pageSize: 6, params: { status: 'PENDING_APPROVAL' } })
    pendingList.value = res.records || []
  } finally {
    loading.value = false
  }
}

const loadChart = async () => {
  try {
    const statsRes = await getActivityStatistics(statisticsDays.value)
    
    // 根据天数进行数据抽样，避免渲染过多数据点
    let sampledData = statsRes.dailyStatistics || []
    const totalDays = sampledData.length
    
    // 根据天数决定采样间隔
    let sampleInterval = 1
    if (statisticsDays.value === 30) {
      sampleInterval = 3 // 30天每3天取一个点，约10个点
    } else if (statisticsDays.value === 90) {
      sampleInterval = 8 // 90天每8天取一个点，约11个点
    }
    
    // 进行采样
    if (sampleInterval > 1) {
      sampledData = sampledData.filter((_, index) => index % sampleInterval === 0 || index === totalDays - 1)
    }
    
    // 格式化日期
    const dates = sampledData.map(i => {
      const d = new Date(i.date)
      return `${d.getMonth() + 1}/${d.getDate()}`
    })
    
    const activityCounts = sampledData.map(i => i.activityCount || 0)
    const participantCounts = sampledData.map(i => i.participantCount || 0)
    
    // 检查是否有活动数据
    hasRecentActivities.value = activityCounts.some(count => count > 0) || participantCounts.some(count => count > 0)
    
    if (hasRecentActivities.value) {
      try {
        // 如果已有实例，直接更新数据
        if (chartInstance) {
          chartInstance.updateData({
            categories: dates,
            series: [
              {
                name: '活动数',
                data: activityCounts,
                color: '#409EFF'
              },
              {
                name: '参与人数',
                data: participantCounts,
                color: '#67C23A'
              }
            ]
          })
          return
        }
        
        // 首次创建图表
        await new Promise(resolve => setTimeout(resolve, 50))
        
        const systemInfo = uni.getSystemInfoSync()
        const canvasWidth = systemInfo.windowWidth - 64
        const canvasHeight = 240
        
        // 根据天数决定显示的标签数量
        const labelCount = statisticsDays.value === 7 ? 4 : (statisticsDays.value === 30 ? 5 : 5)
        
        chartInstance = new uCharts({
          type: 'area',
          context: uni.createCanvasContext('activityChart'),
          width: canvasWidth,
          height: canvasHeight,
          pixelRatio: 1,
          categories: dates,
          series: [
            {
              name: '活动数',
              data: activityCounts,
              color: '#409EFF',
              textColor: '#409EFF',
              textSize: 0
            },
            {
              name: '参与人数',
              data: participantCounts,
              color: '#67C23A',
              textColor: '#67C23A',
              textSize: 0
            }
          ],
          animation: false,  // 关闭动画，提升性能
          enableScroll: false,
          background: '#FFFFFF',
          legend: {
            show: true,
            position: 'top',
            float: 'center',
            padding: 5,
            margin: 5,
            fontSize: 11,
            fontColor: '#606266',
            itemGap: 15
          },
          xAxis: {
            disableGrid: false,
            gridColor: '#F0F0F0',
            fontSize: 10,
            fontColor: '#909399',
            lineColor: '#E4E7ED',
            itemCount: labelCount,
            scrollShow: false,
            boundaryGap: 'justify'
          },
          yAxis: {
            gridType: 'dash',
            dashLength: 4,
            gridColor: '#E4E7ED',
            fontSize: 10,
            fontColor: '#909399',
            splitNumber: 4,
            min: 0,
            showTitle: false
          },
          dataLabel: false,
          dataPointShape: true,
          dataPointShapeType: 'hollow',
          extra: {
            area: {
              type: 'curve',
              opacity: 0.3,  // 增加透明度，让区域更明显
              addLine: true,
              width: 2,
              gradient: true,
              activeWidth: 3
            },
            tooltip: {
              showBox: true,
              showArrow: true,
              showCategory: true,
              borderRadius: 4,
              borderWidth: 1,
              borderColor: '#E4E7ED',
              bgColor: '#FFFFFF',
              fontColor: '#303133',
              fontSize: 11,
              splitLine: true,
              horizentalLine: true
            }
          }
        })
      } catch (chartError) {
        console.error('图表渲染失败:', chartError)
        hasRecentActivities.value = false
      }
    }
  } catch (error) {
    console.error('加载活动统计失败:', error)
    hasRecentActivities.value = false
  }
}

// 图表触摸事件
const touchChart = (e) => {
  if (chartInstance && chartInstance.touchStart) {
    chartInstance.touchStart(e)
  }
}

const moveChart = (e) => {
  if (chartInstance && chartInstance.touchMove) {
    chartInstance.touchMove(e)
  }
}

const touchEndChart = (e) => {
  if (chartInstance && chartInstance.touchEnd) {
    chartInstance.touchEnd(e)
  }
}

let isLoadingChart = false
const chartLoading = ref(false)

const changeStatisticsDays = async (days) => {
  if (statisticsDays.value === days || isLoadingChart) return
  
  statisticsDays.value = days
  isLoadingChart = true
  
  try {
    await loadChart()
  } finally {
    isLoadingChart = false
  }
}

const formatStatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const openReject = (item) => { currentItem.value = item; opinion.value = ''; showReject.value = true }
const closeReject = () => { showReject.value = false; currentItem.value = null; opinion.value = '' }
const approve = async (item) => { await approveActivity(item.id, true, '') ; await Promise.all([loadStats(), loadPending()]) }
const confirmReject = async () => { if (!opinion.value.trim()) { uni.showToast({ title: '请输入原因', icon: 'none' }); return } await approveActivity(currentItem.value.id, false, opinion.value); closeReject(); await Promise.all([loadStats(), loadPending()]) }

const getRoleLabel = (role) => {
  const roleMap = {
    'STUDENT': '学生',
    'TEACHER': '教师',
    'ADMIN': '学工处'
  }
  return roleMap[role] || '未知'
}

const handleAction = (path) => {
  uni.navigateTo({ url: path })
}

// 下拉刷新
onPullDownRefresh(async () => {
  try {
    await Promise.all([loadStats(), loadPending(), loadChart()])
  } finally {
    uni.stopPullDownRefresh()
  }
})

onMounted(async () => {
  await Promise.all([loadStats(), loadPending(), loadChart()])
})
</script>

<style scoped>
/* 极简纯白背景 */
.dashboard-container {
  min-height: 100vh;
  background: #FFFFFF;
  padding-bottom: 120rpx;
}

/* 顶部导航栏 */
.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  padding-top: calc(var(--status-bar-height) + 32rpx);
  background: #FFFFFF;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #111827;
}

.notification-btn {
  position: relative;
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F3F4F6;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.notification-btn:active {
  transform: scale(0.95);
  background: #E5E7EB;
}

.notification-icon {
  font-size: 36rpx;
}

.notification-badge {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: #EF4444;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  color: #FFFFFF;
  font-weight: bold;
  border: 3rpx solid #FFFFFF;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin: 0 32rpx 20rpx;
}

.stat-card {
  background: #F8F8F8;
  border-radius: 20rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.stat-card:active {
  transform: scale(0.98);
}

.stat-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.stat-icon-text {
  font-size: 52rpx;
  color: #fff;
}



.stat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

.chart-section {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin: 0 32rpx 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.section-header {
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.ranking-section {
  display: none;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
}

.rank-number {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background-color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: bold;
  color: #fff;
  margin-right: 20rpx;
}

.rank-number.top-three {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.user-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.user-role {
  font-size: 22rpx;
  color: #999;
}

.user-points {
  font-size: 28rpx;
  font-weight: bold;
  color: #e6a23c;
}

.empty-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 0;
}

.empty-icon {
  margin-bottom: 16rpx;
  opacity: 0.3;
}

.empty-text {
  font-size: 26rpx;
  color: #999;
}

.section-more {
  font-size: 24rpx;
  color: #FF8C42;
}

.quick-actions-section {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  margin: 0 32rpx 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

/* 标签页区域 */
.tabbed-section {
  background: #FFFFFF;
  border-radius: 20rpx;
  margin: 0 32rpx 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.tab-bar {
  display: flex;
  position: relative;
  background: #F9FAFB;
  padding: 8rpx;
  border-radius: 16rpx;
  margin: 24rpx;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 64rpx;
  position: relative;
  z-index: 2;
  transition: all 0.3s ease;
}

.tab-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #6B7280;
  transition: all 0.3s ease;
}

.tab-item.active .tab-text {
  color: #111827;
  font-weight: 600;
}

.tab-badge {
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: #EF4444;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  color: #FFFFFF;
  font-weight: bold;
}

.tab-indicator {
  position: absolute;
  bottom: 8rpx;
  left: 0;
  width: 50%;
  height: 64rpx;
  background: #FFFFFF;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
}

.tab-content {
  padding: 0 24rpx 24rpx;
}

.tab-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #F9FAFB;
  border-radius: 16rpx;
  padding: 20rpx;
  transition: all 0.3s ease;
}

.todo-item:active {
  transform: scale(0.98);
  background: #F3F4F6;
}

.todo-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 28rpx;
  color: #111827;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-sub {
  font-size: 22rpx;
  color: #6B7280;
}

.todo-actions {
  display: flex;
  gap: 12rpx;
  flex-shrink: 0;
}

.btn-approve,
.btn-reject {
  border-radius: 32rpx;
  padding: 12rpx 24rpx;
  font-size: 24rpx;
  border: none;
  transition: all 0.3s ease;
}

.btn-approve {
  background: #FF8C42;
  color: #fff;
}

.btn-approve:active {
  background: #FF6B35;
}

.btn-reject {
  background: #EF4444;
  color: #fff;
}

.btn-reject:active {
  background: #DC2626;
}

.time-selector {
  display: flex;
  gap: 12rpx;
  margin-bottom: 20rpx;
  padding: 0 8rpx;
}

.time-option {
  flex: 1;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F9FAFB;
  border-radius: 12rpx;
  transition: all 0.3s ease;
}

.time-option.active {
  background: #FF8C42;
}

.time-text {
  font-size: 24rpx;
  color: #6B7280;
  font-weight: 500;
}

.time-option.active .time-text {
  color: #FFFFFF;
  font-weight: 600;
}

.chart-container {
  width: 100%;
  padding: 20rpx 0;
}

.chart-wrapper {
  width: 100%;
  height: 240px;
  background: #FFFFFF;
  border-radius: 12rpx;
  overflow: hidden;
}

.chart-canvas {
  width: 100%;
  height: 100%;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24rpx 12rpx;
  background: #F9FAFB;
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.action-item:active {
  transform: scale(0.98);
  background: #F3F4F6;
}

.action-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}



.action-label {
  font-size: 22rpx;
  color: #374151;
  font-weight: 500;
  text-align: center;
  line-height: 1.3;
}

/* 拒绝弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 600rpx;
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.modal-header {
  padding: 32rpx;
  border-bottom: 1rpx solid #eee;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.modal-body {
  padding: 32rpx;
}

.opinion-input {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  font-size: 28rpx;
  line-height: 1.6;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 12rpx;
}

.modal-footer {
  display: flex;
  border-top: 1rpx solid #eee;
}

.modal-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 28rpx;
}

.modal-btn.cancel {
  background-color: #fff;
  color: #666;
}

.modal-btn.confirm {
  background-color: #FF8C42;
  color: #fff;
}
</style>
