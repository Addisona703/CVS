import { ref, reactive } from 'vue'
import { PAGINATION_CONFIG } from '@/utils/constants'

export function usePagination(initialPageSize = PAGINATION_CONFIG.PAGE_SIZE) {
  const loading = ref(false)
  const pagination = reactive({
    current: 1,
    size: initialPageSize,
    total: 0,
    pages: 0
  })
  
  const handleSizeChange = (size) => {
    pagination.size = size
    pagination.current = 1
  }
  
  const handleCurrentChange = (page) => {
    pagination.current = page
  }
  
  const updatePagination = (data) => {
    pagination.current = data.current
    pagination.size = data.size
    pagination.total = data.total
    pagination.pages = data.pages
  }
  
  const resetPagination = () => {
    pagination.current = 1
    pagination.size = initialPageSize
    pagination.total = 0
    pagination.pages = 0
  }
  
  return {
    loading,
    pagination,
    handleSizeChange,
    handleCurrentChange,
    updatePagination,
    resetPagination
  }
}
