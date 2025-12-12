import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import { routeInterceptor } from './utils/route-guard'

export function createApp() {
  const app = createSSRApp(App)
  
  // 创建并使用 Pinia
  const pinia = createPinia()
  app.use(pinia)
  
  // 初始化路由守卫
  routeInterceptor()
  
  return {
    app,
    pinia
  }
}
