import { ref, reactive } from 'vue'

/**
 * 分页功能组合式函数
 * @param {Object} options 配置选项
 * @returns {Object} 分页相关的响应式数据和方法
 */
export function usePagination(options = {}) {
  const {
    defaultPageSize = 10,
    defaultCurrent = 1
  } = options

  // 加载状态
  const loading = ref(false)

  // 分页数据
  const pagination = reactive({
    current: defaultCurrent,    // 当前页码
    size: defaultPageSize,      // 每页条数
    total: 0,                   // 总记录数
    totalPages: 0               // 总页数
  })

  /**
   * 处理页码变化
   * @param {number} page 新页码
   */
  const handleCurrentChange = (page) => {
    pagination.current = page
  }

  /**
   * 处理每页条数变化
   * @param {number} size 新的每页条数
   */
  const handleSizeChange = (size) => {
    pagination.size = size
    pagination.current = 1 // 重置到第一页
  }

  /**
   * 更新分页信息
   * @param {Object} pageData 分页数据
   */
  const updatePagination = (pageData) => {
    if (pageData) {
      pagination.current = pageData.pageNum || pageData.current || 1
      pagination.size = pageData.pageSize || pageData.size || defaultPageSize
      pagination.total = pageData.total || 0
      pagination.totalPages = pageData.totalPages || pageData.pages || 0
    }
  }

  /**
   * 重置分页
   */
  const resetPagination = () => {
    pagination.current = defaultCurrent
    pagination.size = defaultPageSize
    pagination.total = 0
    pagination.totalPages = 0
  }

  /**
   * 获取分页参数
   * @returns {Object} 分页参数
   */
  const getPaginationParams = () => {
    return {
      pageNum: pagination.current,
      pageSize: pagination.size
    }
  }

  return {
    loading,
    pagination,
    handleCurrentChange,
    handleSizeChange,
    updatePagination,
    resetPagination,
    getPaginationParams
  }
}