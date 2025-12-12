<template>
  <view class="custom-tabbar" :data-role="role">
    <view 
      v-for="(item, index) in tabList" 
      :key="index" 
      class="tab-item" 
      :class="{ active: currentIndex === index }"
      @click="switchTab(index)"
    >
      <ph-icon 
        :name="item.iconInactive"
        :size="getIconSize()"
        :color="currentIndex === index ? getActiveColor() : '#999'"
        class="tab-icon"
      />
      <text class="tab-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'

const props = defineProps({
  current: {
    type: Number,
    default: 0
  },
  role: {
    type: String,
    default: 'student' // 'student', 'teacher', 'admin'
  }
})

const emit = defineEmits(['change'])

const currentIndex = ref(0)

// 学生端 TabBar（使用Phosphor图标）
const studentTabList = [
  {
    iconInactive: 'house',
    iconActive: 'house',
    text: '首页',
    pagePath: '/pages/student-sub/dashboard/dashboard'
  },
  {
    iconInactive: 'magnifying-glass',
    iconActive: 'magnifying-glass',
    text: '发现',
    pagePath: '/pages/student/activities/activities'
  },
  {
    iconInactive: 'clipboard-text',
    iconActive: 'clipboard-text',
    text: '我的报名',
    pagePath: '/pages/student/my-signups/my-signups'
  },
  {
    iconInactive: 'user',
    iconActive: 'user',
    text: '我的',
    pagePath: '/pages/student/profile/profile'
  }
]

// 教师端 TabBar（使用Phosphor图标）
const teacherTabList = [
  {
    iconInactive: 'house',
    iconActive: 'house',
    text: '首页',
    pagePath: '/pages/teacher/dashboard/dashboard'
  },
  {
    iconInactive: 'list',
    iconActive: 'list',
    text: '我的活动',
    pagePath: '/pages/teacher/my-activities/my-activities'
  },
  {
    iconInactive: 'clipboard-text',
    iconActive: 'clipboard-text',
    text: '报名管理',
    pagePath: '/pages/teacher/signup-management/signup-management'
  },
  {
    iconInactive: 'user',
    iconActive: 'user',
    text: '我的',
    pagePath: '/pages/teacher/profile/profile'
  }
]

// 学工处端 TabBar（使用Phosphor图标）
const adminTabList = [
  {
    iconInactive: 'house',
    iconActive: 'house',
    text: '首页',
    pagePath: '/pages/admin/dashboard/dashboard'
  },
  {
    iconInactive: 'chart-line',
    iconActive: 'chart-line',
    text: '统计',
    pagePath: '/pages/admin/statistics/statistics'
  },
  {
    iconInactive: 'gear',
    iconActive: 'gear',
    text: '管理',
    pagePath: '/pages/admin/management/management'
  },
  {
    iconInactive: 'user',
    iconActive: 'user',
    text: '我的',
    pagePath: '/pages/admin/profile/profile'
  }
]

// 根据角色选择对应的 TabBar
const tabList = computed(() => {
  switch (props.role) {
    case 'teacher':
      return teacherTabList
    case 'admin':
      return adminTabList
    default:
      return studentTabList
  }
})

// 根据角色获取激活颜色
const getActiveColor = () => {
  switch (props.role) {
    case 'teacher':
      return '#52c41a' // 绿色
    case 'admin':
      return '#FF8C42' // 橙色
    default:
      return '#0052d9' // 蓝色
  }
}

// 根据角色获取图标大小
const getIconSize = () => {
  return 44 // 调大图标尺寸以适配Phosphor
}

// 根据当前页面路径设置高亮
const updateCurrentIndex = () => {
  try {
    const pages = getCurrentPages()
    if (pages.length > 0) {
      const currentPage = pages[pages.length - 1]
      const currentRoute = '/' + currentPage.route

      const index = tabList.value.findIndex(item => item.pagePath === currentRoute)
      if (index !== -1) {
        currentIndex.value = index
        return
      }
    }
  } catch (error) {
    console.log('获取当前页面失败，使用 props.current')
  }

  // 如果获取失败或找不到匹配的路径，使用传入的 props
  currentIndex.value = props.current
}

// 监听 props.current 变化
watch(() => props.current, () => {
  updateCurrentIndex()
}, { immediate: true })

onMounted(() => {
  console.log('CustomTabbar mounted, role:', props.role)
  // 延迟一下确保页面已经完全加载
  setTimeout(() => {
    updateCurrentIndex()
  }, 100)
  
  // 再次延迟确保布局稳定
  setTimeout(() => {
    updateCurrentIndex()
  }, 500)
})

const switchTab = (index) => {
  if (currentIndex.value === index) return

  const targetUrl = tabList.value[index]?.pagePath
  if (!targetUrl) return

  // 使用 switchTab 实现无动画的页面切换
  // 教师/管理员页不在原生 tabBar 列表里，改用 reLaunch 避免报错
  if (props.role === 'student') {
    uni.switchTab({ url: targetUrl })
  } else {
    uni.reLaunch({ url: targetUrl })
  }
}
</script>

<style lang="scss" scoped>
/* 现代化底部导航栏 - 白色背景，轻微顶部阴影 */
.custom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: calc(100rpx + env(safe-area-inset-bottom));
  height: calc(100rpx + constant(safe-area-inset-bottom));
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: space-around;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.06);
  padding-bottom: env(safe-area-inset-bottom);
  padding-bottom: constant(safe-area-inset-bottom);
  z-index: 9999;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12rpx 0;
  transition: all 0.3s ease;
}

.tab-item:active {
  transform: scale(0.95);
}

/* 图标样式 */
.tab-icon {
  line-height: 1;
  margin-bottom: 6rpx;
  transition: all 0.3s ease;
}

.tab-item.active .tab-icon {
  transform: scale(1.05);
}

/* 未激活的文字 - 灰色 */
.tab-text {
  font-size: 22rpx;
  color: #999999;
  font-weight: 400;
  transition: all 0.3s ease;
}

/* 激活的文字 - 根据角色使用不同颜色 */
.tab-item.active .tab-text {
  font-weight: 600;
}

/* 学生端 - 蓝色 */
.custom-tabbar[data-role="student"] .tab-item.active .tab-text {
  color: #0052d9;
}

/* 教师端 - 绿色 */
.custom-tabbar[data-role="teacher"] .tab-item.active .tab-text {
  color: #52c41a;
}

/* 管理员端 - 橙色 */
.custom-tabbar[data-role="admin"] .tab-item.active .tab-text {
  color: #FF8C42;
}
</style>
