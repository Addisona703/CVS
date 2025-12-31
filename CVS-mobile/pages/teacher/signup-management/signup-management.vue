<template>
  <view class="signup-management">
    <!-- 筛选标签页 -->
    <view class="tabs-section">
      <view
        v-for="status in statusOptions"
        :key="status.value"
        class="tab-item"
        :class="{ active: currentStatus === status.value }"
        @click="changeStatus(status.value)"
      >
        <text class="tab-text">{{ status.label }}</text>
        <view v-if="currentStatus === status.value" class="tab-indicator"></view>
      </view>
    </view>

    <!-- 报名列表 -->
    <scroll-view class="scroll-view" scroll-y @scrolltolower="loadMore">
      <view class="signup-list">
        <!-- 加载状态 -->
        <view v-if="loading && signups.length === 0" class="loading-state">
          <view class="loading-spinner"></view>
          <text>加载中...</text>
        </view>
        
        <!-- 空状态 -->
        <view v-else-if="!loading && signups.length === 0" class="empty-state">
          <ph-icon name="clipboard-text" :size="120" color="#ccc" />
          <text class="empty-text">暂无报名记录</text>
        </view>
        
        <!-- 报名卡片 -->
        <view v-else class="card-list">
          <signup-card
            v-for="signup in signups"
            :key="signup.id"
            :signup="signup"
            :show-actions="currentStatus === 'PENDING'"
            :can-approve="currentStatus === 'PENDING'"
            :can-reject="currentStatus === 'PENDING'"
            @approve="approveSignup(signup)"
            @reject="rejectSignup(signup)"
          />
        </view>

        <!-- 加载更多 -->
        <view v-if="loadingMore" class="loading-more">
          <text>加载中...</text>
        </view>
        <view v-else-if="!hasMore && signups.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
      </view>
    </scroll-view>

    <!-- Custom Tab Bar -->
    <custom-tabbar :current="2" role="teacher" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getSignupList, approveSignup as approveSignupApi, rejectSignup as rejectSignupApi } from '@/api/signup'
import SignupCard from '@/components/business/signup-card/signup-card.vue'
import LoadingState from '@/components/common/loading-state/loading-state.vue'
import EmptyState from '@/components/common/empty-state/empty-state.vue'
import CustomTabbar from '@/components/common/custom-tabbar/custom-tabbar.vue'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' }
]

const currentStatus = ref('PENDING')
const signups = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10

onMounted(() => {
  loadSignups()
})

// 下拉刷新
onPullDownRefresh(async () => {
  await loadSignups(true)
  uni.stopPullDownRefresh()
})

const loadSignups = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    signups.value = []
    hasMore.value = true
  }

  if (loading.value || loadingMore.value) return

  if (page.value === 1) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const res = await getSignupList({
      pageNum: page.value,
      pageSize,
      params: {
        status: currentStatus.value || undefined
      }
    })

    const newSignups = res.records || []
    if (page.value === 1) {
      signups.value = newSignups
    } else {
      signups.value.push(...newSignups)
    }

    hasMore.value = signups.value.length < res.total
  } catch (error) {
    console.error('加载报名列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const changeStatus = (status) => {
  if (currentStatus.value === status) return
  currentStatus.value = status
  loadSignups(true)
}

const loadMore = () => {
  if (!hasMore.value || loadingMore.value) return
  page.value++
  loadSignups()
}

const approveSignup = (signup) => {
  const studentName = signup.studentName || signup.student?.realName || signup.student?.name || signup.name || '该学生'
  uni.showModal({
    title: '确认通过',
    content: `确定通过 ${studentName} 的报名申请吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await approveSignupApi(signup.id, {
            status: 'APPROVED',
            reviewNote: ''
          })
          uni.showToast({
            title: '审核成功',
            icon: 'success'
          })
          loadSignups(true)
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

const rejectSignup = (signup) => {
  const studentName = signup.studentName || signup.student?.realName || signup.student?.name || signup.name || '该学生'
  uni.showModal({
    title: '拒绝报名',
    editable: true,
    placeholderText: '请输入拒绝原因',
    success: async (res) => {
      if (res.confirm) {
        try {
          await rejectSignupApi(signup.id, {
            rejectReason: res.content || '不符合要求'
          })
          uni.showToast({
            title: '已拒绝',
            icon: 'success'
          })
          loadSignups(true)
        } catch (error) {
          console.error('审核失败:', error)
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
/* 浅灰色背景 */
.signup-management {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
  padding-bottom: 120rpx;
}

/* 筛选标签页 - 固定在顶部 */
.tabs-section {
  position: sticky;
  top: 0;
  z-index: 99;
  display: flex;
  background: #FFFFFF;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #F0F0F0;
}

.tab-item {
  position: relative;
  flex: 1;
  height: 88rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.tab-text {
  font-size: 28rpx;
  color: #666;
  transition: all 0.3s;
}

.tab-item.active .tab-text {
  color: #52c41a;
  font-weight: 600;
}

/* 主题色下划线 */
.tab-indicator {
  position: absolute;
  bottom: 0;
  width: 48rpx;
  height: 6rpx;
  background: #52c41a;
  border-radius: 3rpx;
}

/* 滚动区域 */
.scroll-view {
  flex: 1;
  background: #F5F5F5;
}

.signup-list {
  padding: 24rpx;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
  color: #999;
  font-size: 28rpx;
  gap: 16rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #E5E5E5;
  border-top-color: #52c41a;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
}



.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 卡片列表 */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

/* 加载更多 */
.loading-more,
.no-more {
  text-align: center;
  padding: 40rpx 0;
  font-size: 26rpx;
  color: #999;
}
</style>
