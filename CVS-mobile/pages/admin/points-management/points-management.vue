<template>
  <view class="points-management-container">
    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-bar">
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索学生姓名或学号"
          @confirm="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </view>
    </view>

    <!-- 用户积分列表 -->
    <view class="points-list">
      <view
        class="points-item"
        v-for="user in userList"
        :key="user.id"
      >
        <view class="user-info">
          <text class="user-name">{{ user.name }}</text>
          <text class="user-id">用户名: {{ user.username }}</text>
        </view>
        <view class="points-info">
          <text class="points-value">{{ user.totalPoints }}积分</text>
          <button class="adjust-btn" @click="handleAdjust(user)">
            <text class="adjust-icon">✏️</text>
          </button>
        </view>
      </view>
      
      <empty-state v-if="!loading && userList.length === 0" message="暂无用户" />
      <loading-state v-if="loading" />
    </view>

    <!-- 积分调整弹窗 -->
    <view class="modal" v-if="showModal" @click="handleCloseModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">调整积分</text>
        </view>
        <view class="modal-body">
          <view class="user-display">
            <text class="user-display-name">{{ currentUser?.name }}</text>
            <text class="user-display-points">当前积分: {{ currentUser?.totalPoints }}</text>
          </view>
          
          <view class="input-group">
            <text class="input-label">积分数量</text>
            <view class="points-adjuster">
              <button class="adjust-btn minus" @click="handleSubtract">
                <text class="adjust-icon">−</text>
              </button>
              <input
                class="points-input-center"
                v-model.number="adjustAmount"
                type="number"
                placeholder="0"
              />
              <button class="adjust-btn plus" @click="handleAdd">
                <text class="adjust-icon">+</text>
              </button>
            </view>
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="handleCloseModal">取消</button>
          <button class="modal-btn confirm" @click="handleConfirm">确定</button>
        </view>
      </view>
    </view>

    <!-- 积分变动记录弹窗 -->
    <view class="modal" v-if="showRecordsModal" @click="handleCloseRecordsModal">
      <view class="modal-content records-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">积分变动记录</text>
        </view>
        <view class="modal-body">
          <view class="records-list">
            <view
              class="record-item"
              v-for="record in pointsRecords"
              :key="record.id"
            >
              <view class="record-info">
                <text class="record-type">{{ record.type }}</text>
                <text class="record-reason">{{ record.reason }}</text>
                <text class="record-time">{{ formatTime(record.createdAt) }}</text>
              </view>
              <view
                class="record-amount"
                :class="record.amount > 0 ? 'positive' : 'negative'"
              >
                {{ record.amount > 0 ? '+' : '' }}{{ record.amount }}
              </view>
            </view>
            
            <empty-state v-if="pointsRecords.length === 0" message="暂无记录" />
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn confirm full" @click="handleCloseRecordsModal">
            关闭
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, onMounted } from 'vue'
import { getPointsRankingPage, awardPoints } from '@/api/points'
import { usePagination } from '@/composables/usePagination'
import { formatTime } from '@/utils/format'
import EmptyState from '@/components/common/empty-state/empty-state.vue'
import LoadingState from '@/components/common/loading-state/loading-state.vue'

export default {
  components: {
    EmptyState,
    LoadingState
  },
  // 下拉刷新
  async onPullDownRefresh() {
    try {
      await this.refresh()
    } finally {
      uni.stopPullDownRefresh()
    }
  },
  setup() {

const searchKeyword = ref('')
const showModal = ref(false)
const showRecordsModal = ref(false)
const currentUser = ref(null)
const adjustAmount = ref(0)
const pointsRecords = ref([])

const {
  list: userList,
  loading,
  loadData,
  refresh,
  updateParams
} = usePagination(
  async (requestParams) => {
    // 使用积分排行榜API，它返回的是PageVO格式
    const result = await getPointsRankingPage({
      page: requestParams.pageNum,
      size: requestParams.pageSize
    })
    return result
  },
  {
    params: {},
    immediate: true  // 自动加载，不需要在onMounted中再次调用
  }
)

const handleSearch = () => {
  // 积分排行榜API不支持搜索，需要刷新列表
  // 如果需要搜索功能，需要在前端过滤或者后端添加搜索参数
  refresh()
}

const handleAdjust = (user) => {
  console.log('选中的用户数据:', user)
  currentUser.value = user
  adjustAmount.value = 0
  showModal.value = true
}

const handleAdd = () => {
  adjustAmount.value = (adjustAmount.value || 0) + 1
}

const handleSubtract = () => {
  const newValue = (adjustAmount.value || 0) - 1
  // 可以减到负数（扣除积分）
  adjustAmount.value = newValue
}

const handleCloseModal = () => {
  showModal.value = false
  currentUser.value = null
  adjustAmount.value = 0
}

const handleConfirm = async () => {
  console.log('点击确定按钮', {
    adjustAmount: adjustAmount.value,
    currentUser: currentUser.value
  })

  if (!adjustAmount.value || adjustAmount.value === 0) {
    uni.showToast({
      title: '请输入有效的积分数量',
      icon: 'none'
    })
    return
  }

  try {
    const amount = adjustAmount.value
    
    // 如果是扣除（负数），检查用户积分是否足够
    if (amount < 0 && currentUser.value.totalPoints < Math.abs(amount)) {
      uni.showToast({
        title: '用户积分不足',
        icon: 'none'
      })
      return
    }

    // 使用userId字段（确保是数字类型）
    const userId = Number(currentUser.value.userId || currentUser.value.id)
    
    if (!userId || isNaN(userId)) {
      console.error('无效的用户ID:', currentUser.value)
      uni.showToast({
        title: '用户ID无效',
        icon: 'none'
      })
      return
    }
    
    console.log('准备调用API', { userId, amount, currentUser: currentUser.value })
    
    await awardPoints(userId, amount)

    console.log('API调用成功')

    handleCloseModal()
    
    // 立即刷新列表
    await refresh()
    
    uni.showToast({
      title: '调整成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('调整积分失败:', error)
    uni.showToast({
      title: error.message || '调整失败',
      icon: 'none'
    })
  }
}

const handleViewRecords = async (user) => {
  try {
    const result = await getPointsRecords({ userId: user.id })
    pointsRecords.value = result.list || []
    showRecordsModal.value = true
  } catch (error) {
    console.error('加载积分记录失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

const handleCloseRecordsModal = () => {
  showRecordsModal.value = false
  pointsRecords.value = []
}

    // onMounted 不需要调用 loadData，因为 usePagination 已经设置了 immediate: true

    return {
      searchKeyword,
      showModal,
      showRecordsModal,
      currentUser,
      adjustAmount,
      pointsRecords,
      userList,
      loading,
      loadData,
      refresh,
      updateParams,
      handleSearch,
      handleAdjust,
      handleAdd,
      handleSubtract,
      handleCloseModal,
      handleConfirm,
      handleViewRecords,
      handleCloseRecordsModal,
      formatTime
    }
  }
}
</script>

<style scoped>
.points-management-container {
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
}

.search-bar {
  display: flex;
  align-items: center;
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
  margin-left: 20rpx;
  padding: 0 32rpx;
  height: 64rpx;
  line-height: 64rpx;
  background-color: #FF8C42;
  color: #fff;
  border-radius: 32rpx;
  font-size: 28rpx;
}

.points-list {
  padding: 20rpx;
}

.points-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
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

.user-id {
  font-size: 24rpx;
  color: #999;
}

.points-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.points-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #E37318;
}

.adjust-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #F3F4F6;
  border-radius: 50%;
  border: none;
  transition: all 0.3s ease;
}

.adjust-btn:active {
  transform: scale(0.95);
  background-color: #E5E7EB;
}

.adjust-btn::after {
  border: none;
}

.adjust-icon {
  font-size: 32rpx;
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

.modal-content.records-modal {
  width: 650rpx;
  max-height: 80vh;
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
  max-height: 60vh;
  overflow-y: auto;
}

.user-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
}

.user-display-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.user-display-points {
  font-size: 32rpx;
  font-weight: bold;
  color: #E37318;
}

.points-adjuster {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.adjust-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  font-size: 40rpx;
  font-weight: normal;
  transition: all 0.2s ease;
  background-color: #F3F4F6;
  color: #6B7280;
}

.adjust-btn:active {
  transform: scale(0.95);
  background-color: #E5E7EB;
}

.adjust-btn::after {
  border: none;
}

.adjust-icon {
  font-size: 40rpx;
  line-height: 1;
}

.points-input-center {
  flex: 1;
  height: 64rpx;
  text-align: center;
  background-color: #f5f5f5;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.input-group {
  margin-bottom: 24rpx;
}

.input-label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 12rpx;
}

.points-input {
  width: 100%;
  height: 64rpx;
  padding: 0 20rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  font-size: 28rpx;
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
  border: none;
  border-radius: 0;
}

.modal-btn.cancel {
  background-color: #fff;
  color: #666;
}

.modal-btn.confirm {
  background-color: #FF8C42;
  color: #fff;
}

.modal-btn::after {
  border: none;
}

.modal-btn.full {
  flex: none;
  width: 100%;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
}

.record-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.record-type {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
}

.record-reason {
  font-size: 24rpx;
  color: #666;
}

.record-time {
  font-size: 22rpx;
  color: #999;
}

.record-amount {
  font-size: 32rpx;
  font-weight: bold;
}

.record-amount.positive {
  color: #00A870;
}

.record-amount.negative {
  color: #E34D59;
}
</style>
