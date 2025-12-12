<template>
  <view class="chart-wrapper">
    <canvas :id="chartId" :canvas-id="chartId" class="chart-canvas" :style="{ height: height, width: '100%' }"></canvas>
  </view>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  option: {
    type: Object,
    required: true
  },
  height: {
    type: String,
    default: '300px'
  }
})

const chartId = ref(`chart-${Date.now()}-${Math.random().toString(36).substring(2, 11)}`)
let chartInstance = null

const initChart = () => {
  // #ifdef H5
  // H5环境下直接使用DOM元素
  nextTick(() => {
    const chartDom = document.getElementById(chartId.value)
    if (chartDom) {
      // 先设置宽度为100%
      chartDom.style.width = '100%'
      chartDom.style.display = 'block'
      
      // 延迟100ms等DOM更新后再初始化
      setTimeout(() => {
        const actualWidth = chartDom.offsetWidth
        if (actualWidth > 0) {
          chartInstance = echarts.init(chartDom, null, {
            width: actualWidth,
            height: parseInt(props.height)
          })
          chartInstance.setOption(props.option)
          
          // 监听窗口大小变化
          const resizeHandler = () => {
            if (chartInstance) {
              chartInstance.resize()
            }
          }
          window.addEventListener('resize', resizeHandler)
        }
      }, 100)
    }
  })
  // #endif

  // #ifndef H5
  // 小程序环境下使用canvas
  uni.createSelectorQuery()
    .select(`#${chartId.value}`)
    .fields({ node: true, size: true })
    .exec((res) => {
      if (res && res[0]) {
        const canvas = res[0].node

        canvas.width = res[0].width
        canvas.height = res[0].height

        chartInstance = echarts.init(canvas, null, {
          width: res[0].width,
          height: res[0].height,
          devicePixelRatio: uni.getSystemInfoSync().pixelRatio
        })

        chartInstance.setOption(props.option)
      }
    })
  // #endif
}

watch(() => props.option, (newOption) => {
  if (chartInstance && newOption) {
    chartInstance.setOption(newOption, true)
  }
}, { deep: true })

onMounted(() => {
  setTimeout(() => {
    initChart()
  }, 300)
})
</script>

<style scoped>
.chart-wrapper {
  width: 100%;
  min-width: 100%;
}

.chart-canvas {
  width: 100% !important;
  min-width: 100%;
  display: block;
}
</style>
