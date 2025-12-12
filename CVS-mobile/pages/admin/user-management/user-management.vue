<template>
  <view class="user-management-container">
    <!-- 搜索和筛选 -->
    <view class="search-section">
      <view class="search-bar">
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索用户名或姓名"
          @confirm="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </view>
      <view class="filter-bar">
        <view
          class="filter-item"
          :class="{ active: selectedRole === role.value }"
          v-for="role in roles"
          :key="role.value"
          @click="handleRoleFilter(role.value)"
        >
          {{ role.label }}
        </view>
      </view>
    </view>

    <!-- 用户列表 -->
    <view class="user-list">
      <user-card
        v-for="user in userList"
        :key="user.id"
        :user="user"
        @edit="handleEdit"
        @delete="handleDelete"
      />
      
      <empty-state v-if="!loading && userList.length === 0" message="暂无用户" />
      <loading-state v-if="loading" />
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getUserList, deleteUser } from '@/api/user'
import { usePagination } from '@/composables/usePagination'
import UserCard from '@/components/business/user-card/user-card.vue'
import EmptyState from '@/components/common/empty-state/empty-state.vue'
import LoadingState from '@/components/common/loading-state/loading-state.vue'

const searchKeyword = ref('')
const selectedRole = ref('')

const roles = ref([
  { label: '全部', value: '' },
  { label: '学生', value: 'STUDENT' },
  { label: '教师', value: 'TEACHER' },
  { label: '学工处', value: 'ADMIN' }
])

const {
  list: userList,
  loading,
  loadData,
  refresh,
  updateParams
} = usePagination(getUserList, {
  params: {
    username: searchKeyword.value || undefined,
    name: searchKeyword.value || undefined,
    role: selectedRole.value || undefined
  }
})

const handleSearch = () => {
  updateParams({
    username: searchKeyword.value || undefined,
    name: searchKeyword.value || undefined,
    role: selectedRole.value || undefined
  })
}

const handleRoleFilter = (role) => {
  selectedRole.value = role
  updateParams({
    username: searchKeyword.value || undefined,
    name: searchKeyword.value || undefined,
    role: role || undefined
  })
}

const handleEdit = (user) => {
  uni.navigateTo({
    url: `/pages/admin/user-edit/user-edit?id=${user.id}`
  })
}

const handleDelete = async (user) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除用户"${user.username}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteUser(user.id)
          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
          refresh()
        } catch (error) {
          console.error('删除用户失败:', error)
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
.user-management-container {
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
  margin-bottom: 20rpx;
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

.filter-bar {
  display: flex;
  gap: 16rpx;
}

.filter-item {
  padding: 12rpx 24rpx;
  background-color: #f5f5f5;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #666;
}

.filter-item.active {
  background-color: #FF8C42;
  color: #fff;
}

.user-list {
  padding: 20rpx;
}
</style>
