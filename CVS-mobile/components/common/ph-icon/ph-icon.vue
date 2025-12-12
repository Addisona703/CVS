<template>
  <!-- H5端使用Phosphor SVG图标 -->
  <!-- #ifdef H5 -->
  <view class="ph-icon" :style="iconStyle">
    <component :is="iconComponent" :weight="weight" />
  </view>
  <!-- #endif -->
  
  <!-- APP端使用iconfont字体图标 -->
  <!-- #ifndef H5 -->
  <text class="iconfont" :class="iconClass" :style="iconFontStyle"></text>
  <!-- #endif -->
</template>

<script setup>
import { computed } from 'vue'

// #ifdef H5
import * as PhosphorIcons from '@phosphor-icons/vue'
// #endif

const props = defineProps({
  name: {
    type: String,
    required: true
  },
  size: {
    type: [Number, String],
    default: 24
  },
  color: {
    type: String,
    default: '#000'
  },
  weight: {
    type: String,
    default: 'regular',
    validator: (value) => ['thin', 'light', 'regular', 'bold', 'fill', 'duotone'].includes(value)
  }
})

// H5端：将name转换为PascalCase的组件名
// #ifdef H5
const iconComponent = computed(() => {
  const pascalName = props.name
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join('')
  return PhosphorIcons[`Ph${pascalName}`]
})

const iconStyle = computed(() => ({
  width: `${props.size}rpx`,
  height: `${props.size}rpx`,
  color: props.color,
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  flexShrink: 0
}))
// #endif

// APP端：使用iconfont字体
// #ifndef H5
const iconClass = computed(() => {
  // iconfont的类名格式：icon-name 或 icon-name-weight
  // 根据你的iconfont项目配置调整
  const weightSuffix = props.weight === 'regular' ? '' : `-${props.weight}`
  return `icon-${props.name}${weightSuffix}`
})

const iconFontStyle = computed(() => ({
  fontSize: `${props.size}rpx`,
  color: props.color,
  lineHeight: 1
}))
// #endif
</script>

<style scoped>
/* H5端样式 */
/* #ifdef H5 */
.ph-icon {
  flex-shrink: 0;
}

.ph-icon :deep(svg) {
  width: 100%;
  height: 100%;
}
/* #endif */

/* APP端样式 */
/* #ifndef H5 */
.iconfont {
  display: inline-block;
  line-height: 1;
  text-align: center;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
/* #endif */
</style>
