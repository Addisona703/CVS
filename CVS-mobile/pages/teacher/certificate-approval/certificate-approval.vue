<template>
  <view class="certificate-approval">
    <!-- 待审核列表 -->
    <view class="approval-list">
      <loading-state v-if="loading && certificates.length === 0" />
      <empty-state v-else-if="!loading && certificates.length === 0" description="暂无待审核证明" />
      <view v-else>
        <view
          v-for="certificate in certificates"
          :key="certificate.id"
          class="certificate-card"
        >
          <!-- 学生信息 -->
          <view class="student-info">
            <view class="student-name">{{ certificate.studentName }}</view>
            <view class="student-id">学号: {{ certificate.studentId }}</view>
          </view>

          <!-- 证明类型 -->
          <view class="certificate-type">
            <text class="type-label">证明类型:</text>
            <text class="type-value">{{ getCertificateTypeName(certificate.type) }}</text>
          </view>

          <!-- 申请时间 -->
          <view class="apply-time">
            <text class="time-label">申请时间:</text>
            <text class="time-value">{{ formatDateTime(certificate.createdAt) }}</text>
          </view>

          <!-- 服务记录汇总 -->
          <view class="service-summary">
            <view class="summary-title">服务记录汇总</view>
            <view class="summary-item">
              <text class="summary-label">参与活动数:</text>
              <text class="summary-value">{{ certificate.activityCount || 0 }} 次</text>
            </view>
            <view class="summary-item">
              <text class="summary-label">总服务时长:</text>
              <text class="summary-value highlight">{{ formatDuration(certificate.totalDuration || 0) }}</text>
            </view>
            <view class="summary-item">
              <text class="summary-label">累计积分:</text>
              <text class="summary-value">{{ certificate.totalPoints || 0 }} 分</text>
            </view>
          </view>

          <!-- 申请说明 -->
          <view v-if="certificate.reason" class="apply-reason">
            <view class="reason-label">申请说明</view>
            <view class="reason-content">{{ certificate.reason }}</view>
          </view>

          <!-- 操作按钮 -->
          <view class="action-buttons">
            <button class="btn btn-reject" @click="rejectCertificate(certificate)">
              拒绝
            </button>
            <button class="btn btn-approve" @click="approveCertificate(certificate)">
              通过
            </button>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="hasMore" class="load-more" @click="loadMore">
          <text v-if="loadingMore">加载中...</text>
          <text v-else>加载更多</text>
        </view>
        <view v-else-if="certificates.length > 0" class="no-more">没有更多了</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getCertificateList, approveCertificate as approveCertificateApi } from '@/api/certificate'
import { formatDateTime, formatDuration } from '@/utils/format'
import LoadingState from '@/components/common/loading-state/loading-state.vue'
import EmptyState from '@/components/common/empty-state/empty-state.vue'

const certificateTypes = {
  SERVICE_HOURS: '服务时长证明',
  PARTICIPATION: '活动参与证明',
  VOLUNTEER: '志愿者证明'
}

const certificates = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10

// 下拉刷新
onPullDownRefresh(async () => {
  try {
    await loadCertificates(true)
  } finally {
    uni.stopPullDownRefresh()
  }
})

onMounted(() => {
  loadCertificates()
})

const loadCertificates = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    certificates.value = []
    hasMore.value = true
  }

  if (loading.value || loadingMore.value) return

  if (page.value === 1) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const res = await getCertificateList({
      page: page.value,
      pageSize,
      status: 'PENDING',
      sortBy: 'createdAt',
      sortOrder: 'desc'
    })

    const newCertificates = res.records || []
    if (page.value === 1) {
      certificates.value = newCertificates
    } else {
      certificates.value.push(...newCertificates)
    }

    hasMore.value = certificates.value.length < res.total
  } catch (error) {
    console.error('加载证明列表失败:', error)
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
  loadCertificates()
}

const getCertificateTypeName = (type) => {
  return certificateTypes[type] || type
}

const approveCertificate = (certificate) => {
  uni.showModal({
    title: '确认通过',
    content: `确定通过 ${certificate.studentName} 的证明申请吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await approveCertificateApi(certificate.id, {
            status: 'APPROVED',
            reviewNote: ''
          })
          uni.showToast({
            title: '审核成功',
            icon: 'success'
          })
          loadCertificates(true)
        } catch (error) {
          console.error('审核失败:', error)
          uni.showToast({
            title: '审核失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

const rejectCertificate = (certificate) => {
  uni.showModal({
    title: '拒绝申请',
    editable: true,
    placeholderText: '请输入拒绝原因',
    success: async (res) => {
      if (res.confirm) {
        try {
          await approveCertificateApi(certificate.id, {
            status: 'REJECTED',
            reviewNote: res.content || '不符合证明条件'
          })
          uni.showToast({
            title: '已拒绝',
            icon: 'success'
          })
          loadCertificates(true)
        } catch (error) {
          console.error('操作失败:', error)
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.certificate-approval {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 32rpx;
}

.approval-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.certificate-card {
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

.certificate-type,
.apply-time {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
  font-size: 26rpx;
}

.type-label,
.time-label {
  color: #999;
}

.type-value,
.time-value {
  color: #333;
}

.service-summary {
  margin: 24rpx 0;
  padding: 24rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
}

.summary-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
  font-size: 26rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.summary-label {
  color: #666;
}

.summary-value {
  color: #333;
  font-weight: 500;

  &.highlight {
    color: #52c41a;
    font-weight: bold;
  }
}

.apply-reason {
  margin-bottom: 24rpx;
}

.reason-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.reason-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  padding: 16rpx;
  background: #f8f9fa;
  border-radius: 8rpx;
}

.action-buttons {
  display: flex;
  gap: 16rpx;
}

.btn {
  flex: 1;
  height: 72rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: none;

  &.btn-reject {
    background: #f5f5f5;
    color: #666;
  }

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
