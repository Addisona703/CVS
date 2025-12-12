/**
 * 分页组合式函数
 * 提供列表数据的分页加载、下拉刷新、上拉加载更多功能
 */
import { ref, computed } from 'vue'

/**
 * 使用分页功能
 * @param {Function} apiFunction - API调用函数
 * @param {Object} options - 配置选项
 * @param {number} options.pageSize - 每页大小（默认20）
 * @param {Object} options.params - 额外的查询参数（可选）
 * @param {boolean} options.immediate - 是否立即加载（默认true）
 * @returns {Object} 分页状态和方法
 */
export function usePagination(apiFunction, options = {}) {
  const {
    pageSize = 20,
    params: extraParams = {},
    immediate = true
  } = options

  // 数据列表
  const list = ref([])

  // 加载状态
  const loading = ref(false)

  // 刷新状态
  const refreshing = ref(false)

  // 是否已加载完成
  const finished = ref(false)

  // 错误信息
  const error = ref(null)

  // 当前页码
  const page = ref(1)

  // 总记录数
  const total = ref(0)

  // 总页数
  const totalPages = computed(() => {
    return Math.ceil(total.value / pageSize)
  })

  // 是否有更多数据
  const hasMore = computed(() => {
    return page.value < totalPages.value
  })

  // 是否为空
  const isEmpty = computed(() => {
    return !loading.value && !refreshing.value && list.value.length === 0
  })

  /**
   * 加载数据
   * @param {boolean} isRefresh - 是否是刷新操作
   * @returns {Promise<void>}
   */
  const loadData = async (isRefresh = false) => {
    // 防止重复加载
    if (loading.value || (finished.value && !isRefresh)) {
      return
    }

    // 设置状态
    if (isRefresh) {
      refreshing.value = true
      page.value = 1
      finished.value = false
      error.value = null
    } else {
      loading.value = true
    }

    try {
      // 构建请求参数 - 使用 PageDTO 格式
      const requestParams = {
        pageNum: page.value,
        pageSize: pageSize,
        params: extraParams
      }

      const response = await apiFunction(requestParams)

      const records = response.records || response.list || (response.data && response.data.records) || []

      if (isRefresh) {
        list.value = records
      } else {
        list.value.push(...records)
      }

      total.value = response.total || (response.data && response.data.total) || list.value.length

      // 检查是否加载完成
      if (list.value.length >= total.value) {
        finished.value = true
      } else {
        page.value++
      }

      // 清除错误
      error.value = null

    } catch (err) {
      console.error('加载数据失败:', err)
      error.value = err

      // 显示错误提示
      uni.showToast({
        title: err.message || '加载失败',
        icon: 'none'
      })

    } finally {
      loading.value = false
      refreshing.value = false
    }
  }

  /**
   * 刷新数据（下拉刷新）
   * @returns {Promise<void>}
   */
  const refresh = async () => {
    return loadData(true)
  }

  /**
   * 加载更多数据（上拉加载）
   * @returns {Promise<void>}
   */
  const loadMore = async () => {
    if (!loading.value && !finished.value) {
      return loadData(false)
    }
  }

  /**
   * 重置分页状态
   */
  const reset = () => {
    list.value = []
    page.value = 1
    total.value = 0
    loading.value = false
    refreshing.value = false
    finished.value = false
    error.value = null
  }

  /**
   * 更新查询参数并重新加载
   * @param {Object} newParams - 新的查询参数
   * @returns {Promise<void>}
   */
  const updateParams = async (newParams) => {
    Object.assign(extraParams, newParams)
    reset()
    return loadData(true)
  }

  /**
   * 删除列表项
   * @param {number|string} id - 项目ID
   * @param {string} idField - ID字段名（默认'id'）
   */
  const removeItem = (id, idField = 'id') => {
    const index = list.value.findIndex(item => item[idField] === id)
    if (index !== -1) {
      list.value.splice(index, 1)
      total.value = Math.max(0, total.value - 1)
    }
  }

  /**
   * 更新列表项
   * @param {number|string} id - 项目ID
   * @param {Object} data - 更新的数据
   * @param {string} idField - ID字段名（默认'id'）
   */
  const updateItem = (id, data, idField = 'id') => {
    const index = list.value.findIndex(item => item[idField] === id)
    if (index !== -1) {
      list.value[index] = { ...list.value[index], ...data }
    }
  }

  /**
   * 添加列表项到开头
   * @param {Object} item - 新项目
   */
  const prependItem = (item) => {
    list.value.unshift(item)
    total.value++
  }

  /**
   * 添加列表项到末尾
   * @param {Object} item - 新项目
   */
  const appendItem = (item) => {
    list.value.push(item)
    total.value++
  }

  // 立即加载
  if (immediate) {
    loadData(true)
  }

  return {
    // 状态
    list,
    loading,
    refreshing,
    finished,
    error,
    page,
    total,
    totalPages,
    hasMore,
    isEmpty,

    // 方法
    loadData,
    refresh,
    loadMore,
    reset,
    updateParams,
    removeItem,
    updateItem,
    prependItem,
    appendItem
  }
}
