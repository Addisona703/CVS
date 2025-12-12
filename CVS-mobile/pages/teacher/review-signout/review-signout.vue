<template>
  <view class="review-signout">
    <!-- 待审核列表 -->
    <view class="review-list">
      <loading-state v-if="loading && records.length === 0" />
      <empty-state v-else-if="!loading && records.length === 0" description="暂无待审核签退" />
      <view v-else>
        <view v-for="record in records" :key="record.id" class="record-card">
          <!-- 学生信息 -->
          <view class="student-info">
            <view class="student-name">{{ record.studentName }}</view>
            <view class="student-id">学号: {{ record.studentNo }}</view>
          </view>

          <!-- 活动信息 -->
          <view class="activity-info">
            <view class="activity-title">{{ record.activityTitle }}</view>
          </view>

          <!-- 时间信息 -->
          <view class="time-info">
            <view class="time-item">
              <text class="time-label">签到时间:</text>
              <text class="time-value">{{ record.checkInTime }}</text>
            </view>
            <view class="time-item">
              <text class="time-label">签退时间:</text>
              <text class="time-value">{{ record.checkOutTime }}</text>
            </view>
            <view class="time-item">
              <text class="time-label">服务时长:</text>
              <text class="time-value highlight">{{ calculateDuration(record.checkInTime, record.checkOutTime) }}</text>
            </view>
          </view>

          <!-- 教师评分 -->
          <view class="rating-section">
            <view class="rating-label">教师评分</view>
            <view class="star-rating">
              <text v-for="star in 5" :key="star" class="star" :class="{ active: star <= record.rating }"
                @click="setRating(record, star)">
                {{ star <= record.rating ? '⭐' : '☆' }} </text>
            </view>
          </view>

          <!-- 评价输入 -->
          <view class="evaluation-section">
            <view class="evaluation-label">教师评价</view>
            <textarea v-model="record.evaluation" class="evaluation-input" placeholder="请输入对学生表现的评价（选填）"
              maxlength="200" />
          </view>

          <!-- 操作按钮 -->
          <view class="action-buttons">
            <button class="btn btn-approve" @click="approveSignout(record)">
              通过审核
            </button>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="hasMore" class="load-more" @click="loadMore">
          <text v-if="loadingMore">加载中...</text>
          <text v-else>加载更多</text>
        </view>
        <view v-else-if="records.length > 0" class="no-more">没有更多了</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { searchReviews, reviewSignup } from '@/api/check'
import LoadingState from '@/components/common/loading-state/loading-state.vue'
import EmptyState from '@/components/common/empty-state/empty-state.vue'

const records = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10

onMounted(() => {
  loadRecords()
})

// 下拉刷新
onPullDownRefresh(async () => {
  await loadRecords(true)
  uni.stopPullDownRefresh()
})

const loadRecords = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    records.value = []
    hasMore.value = true
  }

  if (loading.value || loadingMore.value) return

  if (page.value === 1) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const res = await searchReviews({
      pageNum: page.value,
      pageSize,
      params: {
        reviewStatus: 'PENDING'
      }
    })
    const newRecords = (res.records || []).map(record => ({
      ...record,
      evaluation: '', // 初始化评价字段
      rating: 5 // 初始化评分为5星
    }))

    if (page.value === 1) {
      records.value = newRecords
    } else {
      records.value.push(...newRecords)
    }

    hasMore.value = records.value.length < res.total
  } catch (error) {
    console.error('加载签退记录失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadMore = () => {
  if (!hasMore.value || loadingMore.value) return
  page.value++
  loadRecords()
}

const setRating = (record, star) => {
  record.rating = star
}

const calculateDuration = (checkInTime, checkOutTime) => {
  if (!checkInTime || !checkOutTime) return '0小时'

  const start = new Date(checkInTime.replace(/-/g, '/'))
  const end = new Date(checkOutTime.replace(/-/g, '/'))
  const diffMs = end - start
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))

  if (diffHours > 0) {
    return `${diffHours}小时${diffMinutes}分钟`
  }
  return `${diffMinutes}分钟`
}

const approveSignout = (record) => {
  uni.showModal({
    title: '确认通过',
    content: `确定通过 ${record.studentName} 的签退审核吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          console.log('审核记录:', record)
          await reviewSignup(record.signupId, {
            teacherEvaluation: record.evaluation?.trim() || null,
            teacherRating: record.rating || 5
          })
          uni.showToast({
            title: '审核成功',
            icon: 'success'
          })
          loadRecords(true)
        } catch (error) {
          console.error('审核失败:', error)
          uni.showToast({
            title: error.message || '审核失败',
            icon: 'none'
          })
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.review-signout {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 32rpx;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.record-card {
  background: white;
  border-radius: 16rpx;
  padding: 32rpx;
}

.student-info {
  margin-bottom: 24rpx;
  padding-bottom: 24rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.student-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.student-id {
  font-size: 26rpx;
  color: #999;
}

.activity-info {
  margin-bottom: 24rpx;
}

.activity-title {
  font-size: 28rpx;
  color: #666;
}

.time-info {
  margin-bottom: 24rpx;
}

.time-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
  font-size: 26rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.time-label {
  color: #999;
}

.time-value {
  color: #333;

  &.highlight {
    color: #52c41a;
    font-weight: bold;
  }
}

.rating-section {
  margin-bottom: 24rpx;
}

.rating-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.star-rating {
  display: flex;
  gap: 12rpx;
}

.star {
  font-size: 48rpx;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.star:active {
  transform: scale(1.2);
}

.evaluation-section {
  margin-bottom: 24rpx;
}

.evaluation-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.evaluation-input {
  width: 100%;
  min-height: 150rpx;
  padding: 16rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 26rpx;
  color: #333;
  background: #fafafa;
}

.action-buttons {
  display: flex;
}

.btn {
  width: 100%;
  height: 72rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: none;

  &.btn-approve {
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    color: white;
  }
}

.load-more,
.no-more {
  text-align: center;
  padding: 32rpx;
  font-size: 28rpx;
  color: #999;
}
</style>
