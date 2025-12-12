<script>
import { useAppStore, useAuthStore } from '@/stores'
import { checkAndShowOnboarding } from '@/utils/onboarding'

export default {
  onLaunch() {
    console.log('App Launch')
    
    // 初始化应用状态
    const appStore = useAppStore()
    appStore.init()
    
    // 检查是否需要显示引导页
    const showedOnboarding = checkAndShowOnboarding()
    
    // 如果没有显示引导页，检查登录状态
    if (!showedOnboarding) {
      const authStore = useAuthStore()
      authStore.checkLoginStatus()
    }
  },
  onShow() {
    console.log('App Show')
  },
  onHide() {
    console.log('App Hide')
  }
}
</script>

<style>
/* 引入iconfont图标字体（APP端） */
/* #ifndef H5 */
@import '@/static/fonts/iconfont.css';
/* #endif */

/* 全局样式 */
* {
  box-sizing: border-box;
}

/* 重置默认样式 */
view, text, button, input, textarea {
  margin: 0;
  padding: 0;
}

/* 按钮样式重置 */
button {
  border: none;
  background: none;
  outline: none;
}

button::after {
  border: none;
}

/* 输入框样式 */
input, textarea {
  font-size: 28rpx;
  color: #333333;
}

/* 图片样式 */
image {
  display: block;
  width: 100%;
  height: auto;
}

/* 滚动条样式（H5） */
::-webkit-scrollbar {
  width: 8rpx;
  height: 8rpx;
}

::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 4rpx;
}

::-webkit-scrollbar-track {
  background-color: transparent;
}

/* 隐藏原生tabbar（H5平台） */
uni-tabbar {
  display: none !important;
}

.uni-tabbar {
  display: none !important;
}
</style>
