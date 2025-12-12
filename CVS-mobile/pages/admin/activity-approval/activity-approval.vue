<template>
  <view class="activity-approval-container">
    <!-- 待审核活动列表 -->
    <view class="activity-list">
      <view
        class="activity-item"
        v-for="activity in activityList"
        :key="activity.id"
      >
        <view class="activity-header">
          <text class="activity-title">{{ activity.title }}</text>
          <view class="activity-status pending">待审核</view>
        </view>
        
        <view class="activity-info">
          <view class="info-row">
            <text class="info-label">创建者:</text>
            <text class="info-value">{{ activity.organizerName }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">时间:</text>
            <text class="info-value">{{ formatTime(activity.startTime) }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">地点:</text>
            <text class="info-value">{{ activity.location }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">人数:</text>
            <text class="info-value">{{ activity.maxParticipants }}人</text>
          </view>
        </view>

        <view class="activity-description">
          <text class="description-label">活动描述:</text>
          <text class="description-text">{{ activity.description }}</text>
        </view>

        <view class="activity-actions">
          <button class="action-btn detail" @click="handleDetail(activity)">
            查看详情
          </button>
          <button class="action-btn reject" @click="handleReject(activity)">
            拒绝
          </button>
          <button class="action-btn approve" @click="handleApprove(activity)">
            通过
          </button>
        </view>
      </view>
      
      <empty-state v-if="!loading && activityList.length === 0" message="暂无待审核活动" />
      <loading-state v-if="loading" />
    </view>

    <!-- 审核意见弹窗 -->
    <view class="modal" v-if="showModal" @click="handleCloseModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ modalTitle }}</text>
        </view>
        <view class="modal-body">
          <textarea
            class="opinion-input"
            v-model="opinion"
            :placeholder="modalPlaceholder"
            maxlength="200"
          />
          <text class="char-count">{{ opinion.length }}/200</text>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="handleCloseModal">取消</button>
          <button class="modal-btn confirm" @click="handleConfirm">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getActivityList, approveActivity } from '@/api/activity'
import { usePagination } from '@/composables/usePagination'
import { formatDateTime, formatTime } from '@/utils/format'
import EmptyState from '@/components/common/empty-state/empty-state.vue'
import LoadingState from '@/components/common/loading-state/loading-state.vue'

const showModal = ref(false)
const modalTitle = ref('')
const modalPlaceholder = ref('')
const opinion = ref('')
const currentActivity = ref(null)
const approvalAction = ref('')

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
      status: 'PENDING_APPROVAL'
    }
  }
  const result = await getActivityList(params)
  return result
})

const handleDetail = (activity) => {
  uni.navigateTo({
    url: `/pages/common/activity-detail/activity-detail?id=${activity.id}`
  })
}

const handleApprove = (activity) => {
  currentActivity.value = activity
  approvalAction.value = 'approve'
  modalTitle.value = '通过审核'
  modalPlaceholder.value = '请输入审核意见（可选）'
  opinion.value = ''
  showModal.value = true
}

const handleReject = (activity) => {
  currentActivity.value = activity
  approvalAction.value = 'reject'
  modalTitle.value = '拒绝审核'
  modalPlaceholder.value = '请输入拒绝原因'
  opinion.value = ''
  showModal.value = true
}

const handleCloseModal = () => {
  showModal.value = false
  currentActivity.value = null
  approvalAction.value = ''
  opinion.value = ''
}

const handleConfirm = async () => {
  if (approvalAction.value === 'reject' && !opinion.value.trim()) {
    uni.showToast({
      title: '请输入拒绝原因',
      icon: 'none'
    })
    return
  }

  try {
    const approved = approvalAction.value === 'approve'
    await approveActivity(currentActivity.value.id, approved, opinion.value)

    uni.showToast({
      title: approved ? '审核通过' : '已拒绝',
      icon: 'success'
    })

    handleCloseModal()
    refresh()
  } catch (error) {
    console.error('审核失败:', error)
    uni.showToast({
      title: '审核失败',
      icon: 'none'
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
.activity-approval-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.activity-list {
  padding: 20rpx;
}

.activity-item {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.activity-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.activity-title {
  flex: 1;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-right: 20rpx;
}

.activity-status {
  padding: 8rpx 20rpx;
  border-radius: 32rpx;
  font-size: 24rpx;
  white-space: nowrap;
}

.activity-status.pending {
  background-color: #FFF3E0;
  color: #E37318;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  align-items: center;
  font-size: 26rpx;
}

.info-label {
  color: #999;
  margin-right: 12rpx;
  min-width: 120rpx;
}

.info-value {
  color: #333;
  flex: 1;
}

.activity-description {
  margin-bottom: 20rpx;
  padding: 20rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
}

.description-label {
  display: block;
  font-size: 26rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.description-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.activity-actions {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 32rpx;
  font-size: 26rpx;
}

.action-btn.detail {
  background-color: #f5f5f5;
  color: #666;
}

.action-btn.reject {
  background-color: #E34D59;
  color: #fff;
}

.action-btn.approve {
  background-color: #00A870;
  color: #fff;
}

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
