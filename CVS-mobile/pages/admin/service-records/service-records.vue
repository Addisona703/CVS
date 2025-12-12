<template>
  <view class="service-records-container">
    <!-- 顶部操作栏 -->
    <view class="top-bar">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索学生姓名或活动名称"
          @confirm="handleSearch"
        />
      </view>
      <button class="filter-btn" @click="showFilterSheet = true">
        <text class="filter-icon">筛选</text>
        <view v-if="hasActiveFilters" class="filter-badge"></view>
      </button>
      <button class="export-btn" @click="handleExport">
        <text>导出</text>
      </button>
    </view>

    <!-- 统计概览 -->
    <view class="stats-section">
      <view class="stat-item">
        <text class="stat-value">{{ totalRecords }}</text>
        <text class="stat-label">总记录数</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ totalHours }}h</text>
        <text class="stat-label">总服务时长</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ totalPoints }}</text>
        <text class="stat-label">总积分</text>
      </view>
    </view>

    <!-- 服务记录列表 - 可滚动区域 -->
    <scroll-view class="records-list" scroll-y :show-scrollbar="false">
      <view
        class="record-item"
        v-for="record in recordList"
        :key="record.id"
      >
        <view class="record-header">
          <view class="user-info">
            <text class="user-name">{{ record.userName }}</text>
            <text class="activity-name">{{ record.activityTitle }}</text>
          </view>
          <view class="record-points">+{{ record.pointsEarned }}积分</view>
        </view>
        
        <view class="record-info">
          <view class="info-row">
            <text class="info-label">服务时长:</text>
            <text class="info-value">{{ formatDuration(record.durationMinutes) }}</text>
          </view>
          <view class="info-row" v-if="record.description">
            <text class="info-label">服务描述:</text>
            <text class="info-value">{{ record.description }}</text>
          </view>
          <view class="info-row" v-if="record.evaluation">
            <text class="info-label">服务评价:</text>
            <text class="info-value">{{ record.evaluation }}</text>
          </view>
          <view class="info-row" v-if="record.rating">
            <text class="info-label">评分:</text>
            <text class="info-value">{{ record.rating }}分</text>
          </view>
        </view>

        <view class="record-footer">
          <text class="record-date">{{ formatDate(record.createdAt) }}</text>
        </view>
      </view>
      
      <empty-state v-if="!loading && recordList.length === 0" message="暂无服务记录" />
      <loading-state v-if="loading" />
    </scroll-view>

    <!-- 筛选底部弹窗 -->
    <view v-if="showFilterSheet" class="filter-sheet-mask" @click="showFilterSheet = false">
      <view class="filter-sheet" @click.stop>
        <view class="sheet-handle"></view>
        <view class="sheet-header">
          <text class="sheet-title">筛选</text>
          <text class="sheet-reset" @click="handleResetFilters">重置</text>
        </view>

        <view class="sheet-body">
          <!-- 日期筛选 -->
          <view class="filter-group">
            <text class="filter-group-title">日期范围</text>
            <view class="date-range">
              <picker mode="date" :value="startDate" @change="handleStartDateChange">
                <view class="date-picker-box">
                  <text class="date-label">开始日期</text>
                  <text class="date-value">{{ startDate || '请选择' }}</text>
                </view>
              </picker>
              <text class="date-separator">至</text>
              <picker mode="date" :value="endDate" @change="handleEndDateChange">
                <view class="date-picker-box">
                  <text class="date-label">结束日期</text>
                  <text class="date-value">{{ endDate || '请选择' }}</text>
                </view>
              </picker>
            </view>
          </view>
        </view>

        <view class="sheet-footer">
          <button class="sheet-btn cancel" @click="showFilterSheet = false">取消</button>
          <button class="sheet-btn confirm" @click="handleConfirmFilter">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getServiceRecordList } from '@/api/serviceRecord'
import { usePagination } from '@/composables/usePagination'
import { formatTime, formatDuration, formatDate } from '@/utils/format'
import EmptyState from '@/components/common/empty-state/empty-state.vue'
import LoadingState from '@/components/common/loading-state/loading-state.vue'

const searchKeyword = ref('')
const startDate = ref('')
const endDate = ref('')
const showFilterSheet = ref(false)

const hasActiveFilters = computed(() => {
  return startDate.value !== '' || endDate.value !== ''
})

const {
  list: recordList,
  loading,
  loadData,
  refresh,
  updateParams
} = usePagination(getServiceRecordList, {
  params: {
    userName: searchKeyword.value || undefined,
    activityTitle: searchKeyword.value || undefined
  }
})

const totalRecords = computed(() => recordList.value.length)
const totalHours = computed(() => {
  return (recordList.value.reduce((sum, record) => sum + (record.durationMinutes || 0), 0) / 60).toFixed(1)
})
const totalPoints = computed(() => {
  return recordList.value.reduce((sum, record) => sum + (record.pointsEarned || 0), 0)
})

const handleSearch = () => {
  updateParams({
    userName: searchKeyword.value || undefined,
    activityTitle: searchKeyword.value || undefined
  })
}

const handleStartDateChange = (e) => {
  startDate.value = e.detail.value
}

const handleEndDateChange = (e) => {
  endDate.value = e.detail.value
}

const handleConfirmFilter = () => {
  showFilterSheet.value = false
  // 注意：后端ServiceRecordSearchDTO不支持日期筛选
  // 如果需要日期筛选，需要后端添加相应字段
  updateParams({
    userName: searchKeyword.value || undefined,
    activityTitle: searchKeyword.value || undefined
  })
}

const handleResetFilters = () => {
  startDate.value = ''
  endDate.value = ''
  searchKeyword.value = ''
  showFilterSheet.value = false
  updateParams({
    userName: undefined,
    activityTitle: undefined
  })
}

const handleExport = async () => {
  if (recordList.value.length === 0) {
    uni.showToast({
      title: '暂无数据可导出',
      icon: 'none'
    })
    return
  }

  uni.showLoading({ title: '导出中...' })
  
  try {
    // 生成CSV内容
    const headers = ['学生姓名', '活动名称', '服务时长(分钟)', '获得积分', '服务描述', '评分', '创建时间']
    const csvContent = [
      headers.join(','),
      ...recordList.value.map(record => [
        `"${(record.userName || '').replace(/"/g, '""')}"`,
        `"${(record.activityTitle || '').replace(/"/g, '""')}"`,
        record.durationMinutes || 0,
        record.pointsEarned || 0,
        `"${(record.description || '').replace(/"/g, '""')}"`,
        record.rating || '',
        formatDate(record.createdAt)
      ].join(','))
    ].join('\n')
    
    // 添加BOM以支持Excel正确显示中文
    const BOM = '\uFEFF'
    const csvData = BOM + csvContent
    
    // H5环境：触发浏览器下载
    // #ifdef H5
    const blob = new Blob([csvData], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `服务记录_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    uni.hideLoading()
    uni.showToast({
      title: '导出成功',
      icon: 'success'
    })
    // #endif
    
    // 小程序/APP环境：保存到本地文件
    // #ifndef H5
    try {
      const fileName = `服务记录_${Date.now()}.csv`
      const fs = uni.getFileSystemManager()
      
      // 获取平台特定的文件路径
      let filePath = ''
      // #ifdef MP-WEIXIN
      filePath = `${wx.env.USER_DATA_PATH}/${fileName}`
      // #endif
      // #ifdef APP-PLUS
      filePath = `${plus.io.convertLocalFileSystemURL('_doc')}/${fileName}`
      // #endif
      // #if !defined(MP-WEIXIN) && !defined(APP-PLUS)
      filePath = `${uni.env.USER_DATA_PATH || ''}/${fileName}`
      // #endif
      
      fs.writeFile({
        filePath: filePath,
        data: csvData,
        encoding: 'utf8',
        success: () => {
          uni.hideLoading()
          uni.showModal({
            title: '导出成功',
            content: `文件已保存：${fileName}`,
            showCancel: false,
            success: () => {
              // 尝试打开文档
              uni.openDocument({
                filePath: filePath,
                showMenu: true,
                success: () => {
                  console.log('打开文件成功')
                },
                fail: (err) => {
                  console.error('打开文件失败:', err)
                  uni.showToast({
                    title: '文件已保存',
                    icon: 'success'
                  })
                }
              })
            }
          })
        },
        fail: (err) => {
          uni.hideLoading()
          console.error('文件写入失败:', err)
          uni.showToast({
            title: `导出失败: ${err.errMsg || '未知错误'}`,
            icon: 'none',
            duration: 2000
          })
        }
      })
    } catch (err) {
      uni.hideLoading()
      console.error('导出异常:', err)
      uni.showToast({
        title: '导出失败，请重试',
        icon: 'none',
        duration: 2000
      })
    }
    // #endif
  } catch (error) {
    uni.hideLoading()
    console.error('导出失败:', error)
    uni.showToast({
      title: `导出失败: ${error.message || '未知错误'}`,
      icon: 'none',
      duration: 2000
    })
  }
}

// 下拉刷新
onPullDownRefresh(async () => {
  try {
    await refresh()
  } finally {
    uni.stopPullDownRefresh()
  }
})

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.service-records-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  overflow: hidden;
}

.top-bar {
  display: flex;
  gap: 16rpx;
  padding: 20rpx;
  background-color: #fff;
  align-items: center;
  flex-shrink: 0;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  height: 64rpx;
  background-color: #f5f5f5;
  border-radius: 32rpx;
  padding: 0 20rpx;
  gap: 12rpx;
}

.search-icon {
  font-size: 28rpx;
  color: #999;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  background: transparent;
}

.filter-btn,
.export-btn {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 24rpx;
  border-radius: 32rpx;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.filter-btn {
  background-color: #f5f5f5;
  color: #333;
}

.export-btn {
  background-color: #00A870;
  color: #fff;
}

.filter-icon {
  font-size: 28rpx;
}

.filter-badge {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 16rpx;
  height: 16rpx;
  background-color: #E34D59;
  border-radius: 50%;
  border: 2rpx solid #fff;
}

/* 筛选底部弹窗 */
.filter-sheet-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.filter-sheet {
  width: 100%;
  max-height: 80vh;
  background-color: #fff;
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.sheet-handle {
  width: 80rpx;
  height: 8rpx;
  background-color: #E5E7EB;
  border-radius: 4rpx;
  margin: 16rpx auto;
}

.sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #F3F4F6;
}

.sheet-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #111827;
}

.sheet-reset {
  font-size: 28rpx;
  color: #FF8C42;
}

.sheet-body {
  flex: 1;
  overflow-y: auto;
  padding: 32rpx;
}

.filter-group {
  margin-bottom: 40rpx;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-group-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 20rpx;
}

.date-range {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.date-picker-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background-color: #F3F4F6;
  border-radius: 16rpx;
}

.date-label {
  font-size: 28rpx;
  color: #6B7280;
}

.date-value {
  font-size: 28rpx;
  color: #111827;
  font-weight: 500;
}

.date-separator {
  font-size: 24rpx;
  color: #9CA3AF;
  text-align: center;
  padding: 8rpx 0;
}

.sheet-footer {
  display: flex;
  gap: 20rpx;
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #F3F4F6;
  background-color: #fff;
}

.sheet-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.sheet-btn.cancel {
  background-color: #F3F4F6;
  color: #6B7280;
}

.sheet-btn.confirm {
  background-color: #FF8C42;
  color: #fff;
}

.stats-section {
  display: flex;
  background-color: #fff;
  padding: 24rpx 20rpx;
  border-bottom: 16rpx solid #f5f5f5;
  flex-shrink: 0;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF8C42;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

.records-list {
  flex: 1;
  height: 0;
  padding: 20rpx;
}

.records-list::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}

.record-item {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.record-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.activity-name {
  font-size: 26rpx;
  color: #666;
}

.record-points {
  padding: 8rpx 20rpx;
  background-color: #FFF3E0;
  color: #E37318;
  border-radius: 32rpx;
  font-size: 24rpx;
  font-weight: bold;
}

.record-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  align-items: flex-start;
  font-size: 26rpx;
}

.info-label {
  color: #999;
  margin-right: 12rpx;
  min-width: 140rpx;
}

.info-value {
  color: #333;
  flex: 1;
  line-height: 1.6;
}

.record-footer {
  padding-top: 20rpx;
  border-top: 1rpx solid #f5f5f5;
}

.record-date {
  font-size: 24rpx;
  color: #999;
}
</style>
