<template>
  <view class="category-management-container">
    <!-- 顶部操作栏 -->
    <view class="top-bar">
      <button class="add-btn" @click="handleAdd">+ 添加分类</button>
    </view>

    <!-- 分类列表 -->
    <view class="category-list">
      <view
        class="category-item"
        v-for="category in categoryList"
        :key="category.id"
      >
        <view class="category-info">
          <text class="category-name">{{ category.name }}</text>
          <!-- <text class="category-count">{{ category.productCount || 0 }}个商品</text> -->
        </view>
        <view class="category-actions">
          <button class="action-btn edit" @click="handleEdit(category)">编辑</button>
          <button class="action-btn delete" @click="handleDelete(category)">删除</button>
        </view>
      </view>
      
      <empty-state v-if="!loading && categoryList.length === 0" message="暂无分类" />
      <loading-state v-if="loading" />
    </view>

    <!-- 添加/编辑分类弹窗 -->
    <view class="modal" v-if="showModal" @click="handleCloseModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ editingCategory ? '编辑分类' : '添加分类' }}</text>
        </view>
        <view class="modal-body">
          <input
            class="category-input"
            v-model="categoryName"
            placeholder="请输入分类名称"
          />
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="handleCloseModal">取消</button>
          <button class="modal-btn confirm" @click="handleSave">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, onMounted } from 'vue'
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/mall'
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
      await this.loadData()
    } finally {
      uni.stopPullDownRefresh()
    }
  },
  setup() {
    const categoryList = ref([])
    const loading = ref(false)
    const showModal = ref(false)
    const editingCategory = ref(null)
    const categoryName = ref('')

const loadData = async () => {
  loading.value = true
  try {
    const result = await getCategoryList()
    console.log('分类列表响应:', result)
    // 后端直接返回数组，不是包含list字段的对象
    categoryList.value = Array.isArray(result) ? result : (result.data || result.list || [])
    console.log('分类列表数据:', categoryList.value)
  } catch (error) {
    console.error('加载分类失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingCategory.value = null
  categoryName.value = ''
  showModal.value = true
}

const handleEdit = (category) => {
  editingCategory.value = category
  categoryName.value = category.name
  showModal.value = true
}

const handleCloseModal = () => {
  showModal.value = false
  editingCategory.value = null
  categoryName.value = ''
}

const handleSave = async () => {
  if (!categoryName.value.trim()) {
    uni.showToast({
      title: '请输入分类名称',
      icon: 'none'
    })
    return
  }

  try {
    if (editingCategory.value) {
      await updateCategory(editingCategory.value.id, { name: categoryName.value })
      uni.showToast({
        title: '更新成功',
        icon: 'success'
      })
    } else {
      await createCategory({ name: categoryName.value })
      uni.showToast({
        title: '添加成功',
        icon: 'success'
      })
    }
    handleCloseModal()
    loadData()
  } catch (error) {
    console.error('保存分类失败:', error)
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  }
}

const handleDelete = async (category) => {
  if (category.productCount > 0) {
    uni.showToast({
      title: '该分类下有商品，无法删除',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认删除',
    content: `确定要删除分类"${category.name}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteCategory(category.id)
          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
          loadData()
        } catch (error) {
          console.error('删除分类失败:', error)
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

    onMounted(() => {
      loadData()
    })

    return {
      categoryList,
      loading,
      showModal,
      editingCategory,
      categoryName,
      loadData,
      handleAdd,
      handleEdit,
      handleCloseModal,
      handleSave,
      handleDelete
    }
  }
}
</script>

<style scoped>
.category-management-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.top-bar {
  padding: 20rpx;
  background-color: #fff;
}

.add-btn {
  width: 100%;
  height: 64rpx;
  line-height: 64rpx;
  background-color: #FF8C42;
  color: #fff;
  border-radius: 32rpx;
  font-size: 28rpx;
}

.category-list {
  padding: 20rpx;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.category-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.category-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.category-count {
  font-size: 24rpx;
  color: #999;
}

.category-actions {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.action-btn.edit {
  background-color: #FF8C42;
  color: #fff;
}

.action-btn.delete {
  background-color: #E34D59;
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

.category-input {
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
