import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加认证token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 如果是blob类型的响应（如PDF文件），直接返回data
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const res = response.data
    
    // 统一处理响应
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    // 检查是否是下载管理器（如IDM）拦截的请求
    // 这种情况下文件已经开始下载，不需要显示错误
    if (error.code === 'ERR_NETWORK' && error.config?.responseType === 'blob') {
      console.log('下载请求已被下载管理器接管')
      return Promise.reject(error)
    }
    
    console.error('响应错误:', error)
    const { response } = error
    
    if (response) {
      switch (response.status) {
        case 401:
          // Token过期或无效
          ElMessageBox.confirm(
            '登录状态已过期，请重新登录',
            '系统提示',
            {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning'
            }
          ).then(() => {
            const authStore = useAuthStore()
            authStore.logout()
            router.push('/auth/login')
          }).catch(() => {
            // 用户取消
          })
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    
    return Promise.reject(error)
  }
)

export default request
