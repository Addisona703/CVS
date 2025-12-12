<template>
  <view class="activity-management-container">
    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-bar">
        <input class="search-input" v-model="searchKeyword" placeholder="搜索活动标题" @confirm="handleSearch" />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </view>
      <button class="filter-btn" @click="showFilterSheet = true">
        <text class="filter-icon">🔍</text>
        <text>筛选</text>
      </button>
    </view>

    <!-- 活动列表 -->
    <view class="activity-list">
      <activity-card v-for="activity in activityList" :key="activity.id" :activity="activity" :show-actions="true"
        @click="handleDetail(activity)" @edit="handleEdit(activity)" @delete="handleDelete(activity)" />

      <empty-state v-if="!loading && activityList.length === 0" message="暂无活动" />
      <loading-state v-if="loading" />
    </view>

    <!-- 筛选底部弹窗 -->
    <view v-if="showFilterSheet" class="filter-sheet-mask" @click="showFilterSheet = false">
      <view class="filter-sheet" @click.stop>
        <view class="sheet-handle"></view>
        <view class="sheet-header">
          <text class="sheet-title">筛选</text>
          <text class="sheet-reset" @click="handleResetFilters">重置</text>
        </view>

        <view class="sheet-body">
          <!-- 状态筛选 -->
          <view class="filter-group">
            <text class="filter-group-title">活动状态</text>
            <view class="filter-options">
              <view
                class="filter-option"
                :class="{ active: selectedStatus === status.value }"
                v-for="status in statusList"
                :key="status.value"
                @click="selectedStatus = status.value"
              >
                {{ status.label }}
              </view>
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
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getActivityList, deleteActivity } from '@/api/activity'
import { usePagination } from '@/composables/usePagination'
import ActivityCard from '@/components/business/activity-card/activity-card.vue'
import EmptyState from '@/components/common/empty-state/empty-state.vue'
import LoadingState from '@/components/common/loading-state/loading-state.vue'

const searchKeyword = ref('')
const selectedStatus = ref('')
const showFilterSheet = ref(false)

const statusList = ref([
  { label: '全部', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '待审核', value: 'PENDING_APPROVAL' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '进行中', value: 'ONGOING' },
  { label: '已完成', value: 'COMPLETED' }
])

const {
  list: activityList,
  loading,
  loadData,
  refresh
} = usePagination(async (requestParams) => {
  const params = {
    ...requestParams,
    params: {
      ...(requestParams.params || {}),
      title: searchKeyword.value || undefined,
      status: selectedStatus.value || undefined
    }
  }
  const result = await getActivityList(params)
  return result
})

const handleSearch = () => {
  refresh()
}

const handleStatusFilter = (status) => {
  selectedStatus.value = status
  refresh()
}

const handleConfirmFilter = () => {
  showFilterSheet.value = false
  refresh()
}

const handleResetFilters = () => {
  selectedStatus.value = ''
  refresh()
  showFilterSheet.value = false
}

const handleDetail = (activity) => {
  uni.navigateTo({
    url: `/pages/common/activity-detail/activity-detail?id=${activity.id}`
  })
}

const handleEdit = (activity) => {
  uni.navigateTo({
    url: `/pages/teacher/create-activity/create-activity?id=${activity.id}`
  })
}

const handleDelete = async (activity) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除活动"${activity.title}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteActivity(activity.id)
          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
          refresh()
        } catch (error) {
          console.error('删除活动失败:', error)
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
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
.activity-management-container {
  min-height: 100vh;
  background: #FFFFFF;
  padding-bottom: 120rpx;
}

.search-section {
  background: #FFFFFF;
  padding: 24rpx;
  margin-bottom: 20rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 16rpx;
}

.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.search-input {
  flex: 1;
  height: 64rpx;
  padding: 0 20rpx;
  background-color: #f5f5f5;
  border-radius: 32rpx;
  font-size: 28rpx;
}

.search-btn {
  padding: 0 32rpx;
  height: 64rpx;
  line-height: 64rpx;
  background-color: #FF8C42;
  color: #fff;
  border-radius: 32rpx;
  font-size: 28rpx;
  white-space: nowrap;
}

.filter-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 0 24rpx;
  height: 64rpx;
  background-color: #f5f5f5;
  border-radius: 32rpx;
  font-size: 28rpx;
  color: #333;
  white-space: nowrap;
}

.filter-icon {
  font-size: 28rpx;
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

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.filter-option {
  padding: 16rpx 32rpx;
  background-color: #F3F4F6;
  border-radius: 32rpx;
  font-size: 28rpx;
  color: #6B7280;
  transition: all 0.3s ease;
}

.filter-option.active {
  background-color: #FF8C42;
  color: #fff;
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

.activity-list {
  padding: 20rpx;
}
</style>
