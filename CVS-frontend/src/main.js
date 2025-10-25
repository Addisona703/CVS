import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import TDesign from 'tdesign-vue-next'
import 'tdesign-vue-next/es/style/index.css'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import { initFontSystem } from './utils/language'

// 导入通知高亮样式
import '@/assets/styles/notification-highlight.scss'

// 导入主题系统
import '@/styles/themes/index.scss'

const app = createApp(App)
const pinia = createPinia()

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default'
})
app.use(TDesign)

// 初始化字体系统
initFontSystem().then(({ language, fontLoaded }) => {
  // console.log(`字体系统初始化完成: 语言=${language}, 字体加载=${fontLoaded}`)
})

// 初始化认证状态
const authStore = useAuthStore()
authStore.initAuth()

app.mount('#app')
